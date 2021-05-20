package bg.server.main;

import bg.server.Main;
import bg.server.room.RoomServer;
import shared.Game;
import shared.MainRequest;
import shared.RoomInfo;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class MainGameServer extends Thread {
    private final Game game;
    private final ServerSocket server;
    public static final Semaphore limit = new Semaphore(100);
    private final Map<Integer, RoomServer> rooms;

    public MainGameServer(Game game) throws IOException {
        rooms = new ConcurrentHashMap<>();
        this.game = game;
        server = new ServerSocket(0);
        Main.serverMap.put(game, server.getLocalPort());
    }

    @Override
    public void run() {
        while (true) {
            Socket s = null;
            ObjectOutputStream objOut = null;
            ObjectInputStream objIn = null;
            try {
                s = server.accept();
                objOut = new ObjectOutputStream(s.getOutputStream());
                objIn = new ObjectInputStream(s.getInputStream());
                MainRequest request = MainRequest.values()[objIn.readInt()];
                if (request == MainRequest.HOST) {
                    int response = hostRoom();
                    objOut.writeInt(response);
                    objOut.flush();
                    objOut.close();
                    objIn.close();
                    s.close();
                } else if (request == MainRequest.JOIN) {
                    serveAvailableRooms(s, objIn, objOut);
                }
            } catch (IOException e) {
                try {
                    if (objIn != null) {
                        objIn.close();
                        objOut.close();
                        s.close();
                    }
                } catch (IOException ignore) {
                }
            }
        }
    }

    public void removeRoom(int roomPort) {
        rooms.remove(roomPort);
        limit.release();
    }

    private void serveAvailableRooms(Socket joinSocket, ObjectInputStream objIn, ObjectOutputStream objOut)
            throws IOException {
        JoinThread join = new JoinThread(joinSocket, objIn, objOut);
        join.start();
    }

    private int hostRoom() throws IOException {
        if (limit.tryAcquire()) {
            RoomServer new_room = new RoomServer(game, this);
            rooms.put(new_room.getPort(), new_room);
            return new_room.getPort();
        }
        // all rooms are used
        return -1;
    }

    private class JoinThread extends Thread {
        private Iterator<Map.Entry<Integer, RoomServer>> room_iterator;
        private final Socket joinSocket;
        private final ObjectInputStream objIn;
        private final ObjectOutputStream objOut;

        public JoinThread(Socket joinSocket, ObjectInputStream objIn, ObjectOutputStream objOut) {
            this.joinSocket = joinSocket;
            this.objOut = objOut;
            this.objIn = objIn;
        }

        private List<RoomInfo> featureMore() {
            if (room_iterator == null || !room_iterator.hasNext()) {
                room_iterator = rooms.entrySet().iterator();
            }
            List<RoomInfo> roomInfos = new ArrayList<>();
            while (room_iterator.hasNext() && roomInfos.size() < 5) {
                RoomServer room = room_iterator.next().getValue();
                if (room != null && room.isPublic() && !room.isFull() && !room.inGamePhase()) {
                    roomInfos.add(new RoomInfo(room.getPort(), room.getHostName(), room.getPlayersCount()));
                }
            }
            return roomInfos;
        }

        @Override
        public void run() {
            boolean get_more;
            try {
                do {
                    List<RoomInfo> feature = featureMore();
                    objOut.writeInt(feature.size());
                    for (RoomInfo info : feature) {
                        objOut.writeObject(info);
                    }
                    objOut.flush();
                    get_more = objIn.readBoolean();
                } while (get_more);
            } catch (IOException ignore) {
            } finally {
                try {
                    objOut.close();
                    objIn.close();
                    joinSocket.close();
                } catch (IOException ignore) {
                }
            }
        }
    }
}
