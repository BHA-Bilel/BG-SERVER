package room;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.Game;
import shared.RoomComm;
import shared.RoomMsg;
import shared.RoomPosition;

public class Client extends Thread {
    protected int id;
    protected String name;
    protected RoomPosition position;

    private final Socket socket;
    private final ObjectInputStream objIn;
    private final ObjectOutputStream objOut;
    private final RoomServer room;

    public Client(RoomServer room, Socket socket) throws IOException {
        this.room = room;
        this.socket = socket;
        objOut = new ObjectOutputStream(socket.getOutputStream());
        objIn = new ObjectInputStream(socket.getInputStream());
    }

    public boolean can_access() {
        boolean deny_access = false;
        try {
            boolean featuring_join = objIn.readBoolean();
            if (!featuring_join) {
                Game game = Game.values()[objIn.readInt()];
                deny_access = room.game != game;
            } else {
                deny_access = !room.isPublic();
            }
            objOut.writeBoolean(deny_access);
            objOut.flush();
        } catch (IOException e) {
            room.clientLeft(this);
        }
        return !deny_access;
    }

    public void handShake(int chatPort, int players_count) {
        try {
            objOut.writeInt(id);
            objOut.writeInt(chatPort);
            objOut.writeInt(players_count);
            objOut.writeBoolean(room.isPublic());
            objOut.writeInt(position.ordinal());
            objOut.flush();
            name = objIn.readUTF();
            room.name_mutex.lock();
            int dup = room.getUniqueName(id, name.toLowerCase(), 0);
            if (dup > 0)
                name += " " + dup;
            room.name_mutex.unlock();

            objOut.writeUTF(name);
            objOut.flush();
        } catch (IOException e) {
            room.clientLeft(this);
        }
    }

    public void meet(Client newClient) {
        RoomMsg msg = new RoomMsg(newClient.id, RoomComm.JOINED, new Object[]{newClient.name, newClient.position.ordinal()});
        sendMsg(msg);
        msg = new RoomMsg(id, RoomComm.JOINED, new Object[]{name, position.ordinal()});
        newClient.sendMsg(msg);
    }

    public void sendMsg(RoomMsg msg) {
        try {
            objOut.writeObject(msg);
            objOut.flush();
        } catch (IOException e) {
            room.clientLeft(this);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                RoomMsg msg = (RoomMsg) objIn.readObject();
                RoomMsg resp;
                switch (RoomComm.values()[msg.comm]) {
                    case GO_PRIVATE -> {
                        room.setPublic(false);
                        resp = new RoomMsg(id, RoomComm.GONE_PRIVATE);
                        room.diffuseMsg(resp);
                    }
                    case GO_PUBLIC -> {
                        room.setPublic(true);
                        resp = new RoomMsg(id, RoomComm.GONE_PUBLIC);
                        room.diffuseMsg(resp);
                    }
                    case START_GAME -> {
                        resp = new RoomMsg(RoomComm.GAME_STARTING);
                        room.diffuseMsg(resp);
                        room.startGame();
                    }
                    case REQUEST_CHANGE_NAME -> {
                        room.name_mutex.lock();
                        int dup = room.getUniqueName(id, ((String) msg.adt_data[0]).toLowerCase(), 0);
                        if (dup > 0)
                            name = msg.adt_data[0] + " " + dup;
                        else
                            name = (String) msg.adt_data[0];
                        resp = new RoomMsg(id, RoomComm.CHANGED_NAME, new Object[]{name});
                        room.diffuseMsg(resp);
                        room.name_mutex.unlock();
                    }
                    case REQUEST_KICK -> {
                        resp = new RoomMsg((int) msg.adt_data[0], RoomComm.KICKED);
                        room.diffuseMsg(resp);
                        room.clientKicked((int) msg.adt_data[0]);
                    }
                    case END_GAME -> room.endGame();
                    case REQUEST_TEAM_UP -> room.request_team_up(msg);
                    case TAKE_EMPTY_PLACE -> room.take_empty_place(this, msg.adt_data);
                    case ACCEPT_TEAM_UP -> room.team_up(msg);
                    case DENY_TEAM_UP -> room.denied_team_up(msg);
                    default -> room.diffuseClientMsg(this, msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            room.clientLeft(this);
        }
    }

    private volatile boolean closed = false;

    public synchronized void closeConnection() {
        if (!closed) {
            try {
                objIn.close();
                objOut.close();
                socket.close();
            } catch (IOException ignore) {
            }
            closed = true;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        Client client = (Client) obj;
        return client.id == this.id;
    }

}
