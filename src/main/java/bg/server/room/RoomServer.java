package bg.server.room;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import bg.server.dominos.model.GameType;
import bg.server.main.MainGameServer;
import bg.server.chat.ChatServer;
import bg.server.game.CHECKERS_GameServer;
import bg.server.game.CHESS_GameServer;
import bg.server.game.COINCHE_GameServer;
import bg.server.game.CONNECT4_GameServer;
import bg.server.game.DOMINOS_GameServer;
import bg.server.game.GameServer;
import bg.server.game.XO_GameServer;
import shared.Game;
import shared.RoomComm;
import shared.RoomMsg;
import shared.RoomPosition;

public class RoomServer {

    private final Map<Integer, Client> clients;
    private ServerSocket roomServer;
    private final ChatServer chatServer;
    private GameServer gameServer;
    private int playerID = 1;
    protected Game game;
    private final MainGameServer mainGameServer;
    private volatile boolean isPublic = false, stop_waiting = false, roomClosed = false;
    private final ReentrantLock place_mutex = new ReentrantLock();
    protected final ReentrantLock name_mutex = new ReentrantLock();
    private RoomPosition current_pos;
    protected RoomMsg game_started;
    private final Runnable waitRunnable;
    private Thread waitThread;
    private final int room_port;

    public RoomServer(Game game, MainGameServer mainGameServer) throws IOException {
        this.mainGameServer = mainGameServer;
        this.game = game;
        clients = new ConcurrentHashMap<>();
        roomServer = new ServerSocket(0);
        room_port = roomServer.getLocalPort();
        chatServer = new ChatServer();
        waitRunnable = () -> {
            try {
                int players_count = clients.size();
                while (players_count < game.players) {
                    Socket client_socket = roomServer.accept();
                    if (stop_waiting) {
                        return;
                    } else {
                        Client new_client = new Client(RoomServer.this, client_socket);
                        if (new_client.can_access()) {
                            place_mutex.lock();
                            new_client.position = getEmptyPosition();
                            new_client.id = playerID;
                            playerID++;
                            clients.put(new_client.id, new_client);
                            place_mutex.unlock();
                            new_client.handShake(chatServer.getPort(), players_count);
                            MeetNewClient(new_client);
                            chatServer.acceptNewclient(new_client.id);
                            new_client.start();
                        } else {
                            new_client.closeConnection();
                        }
                    }
                    players_count = clients.size();
                }
                try {
                    roomServer.close();
                } catch (IOException ignore) {
                }
            } catch (IOException e) {
                closeRoom();
            }
        };
        waitForPlayers(true);
    }

    protected RoomPosition getEmptyPosition() {
        List<RoomPosition> avail_pos = new ArrayList<>(Arrays.asList(RoomPosition.values()));
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            Client c = IdClientEntry.getValue();
            avail_pos.remove(c.position);
        }
        return avail_pos.get(0);
    }

    public int getPort() {
        return room_port;
    }

    public void waitForPlayers(boolean initial_wait) {
        stop_waiting = false;
        if (waitThread == null || !waitThread.isAlive()) {
            try {
                if (!initial_wait) {
                    roomServer.close();
                    roomServer = new ServerSocket(room_port);
                }
                waitThread = new Thread(waitRunnable);
                waitThread.start();
            } catch (IOException e) {
                closeRoom();
            }
        }
    }

    @SuppressWarnings("resource")
    public void stopWaiting() {
        stop_waiting = true;
        try {
            new Socket("127.0.0.1", getPort());
        } catch (IOException ignore) {
        }
    }

    public void startGame() throws IOException {
        stopWaiting();
        Object[] adt_data;
        switch (game) {
            case XO:
                gameServer = new XO_GameServer(RoomServer.this);
                break;

            case CONNECT4:
                gameServer = new CONNECT4_GameServer(RoomServer.this);
                break;

            case CHECKERS:
                gameServer = new CHECKERS_GameServer(RoomServer.this);
                break;

            case CHESS:
                gameServer = new CHESS_GameServer(RoomServer.this);
                break;

            case COINCHE:
                gameServer = new COINCHE_GameServer(RoomServer.this);
                break;

            case DOMINOS:
                int players_count = clients.size();
                GameType game_type = players_count == 2 ? GameType.OnevOne
                        : players_count == 3 ? GameType.ThreePlayers : GameType.TwovTwo;
                gameServer = new DOMINOS_GameServer(RoomServer.this, game_type);
                break;

            default:
                break;
        }
        adt_data = gameServer.getPort();
        current_pos = RoomPosition.BOTTOM;
        game_started = new RoomMsg(RoomComm.GAME_STARTED, adt_data);
        gameServer.acceptConnection();
    }

    private void MeetNewClient(Client new_client) {
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            Client existing_client = IdClientEntry.getValue();
            if (new_client.id != existing_client.id)
                existing_client.meet(new_client);
        }
    }

    public synchronized void closeRoom() {
        if (!roomClosed) {
            for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
                IdClientEntry.getValue().closeConnection();
            }
            try {
                roomServer.close();
            } catch (IOException ignore) {
            }
            if (chatServer != null)
                chatServer.closeChat();
            if (gameServer != null)
                gameServer.closeGame();
            mainGameServer.removeRoom(getPort());
            roomClosed = true;
        }
    }

    public void diffuseClientMsg(Client client, RoomMsg msg) {
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            Client c = IdClientEntry.getValue();
            if (!c.equals(client))
                c.sendMsg(msg);
        }
    }

    public void clientLeft(Client left) {
        Client removed = clients.remove(left.id);
        if (removed != null) {
            if (clients.isEmpty()) {
                closeRoom();
            } else {
                left.closeConnection();
                RoomMsg msg = new RoomMsg(left.id, RoomComm.LEFT);
                diffuseMsg(msg);
                if (gameServer != null) {
                    endGame();
                }
                waitForPlayers(false);
            }
        }
    }

    public void clientKicked(int id) {
        Client kicked = null;
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            Client client = IdClientEntry.getValue();
            if (client.id == id) {
                kicked = client;
                break;
            }
        }
        if (kicked != null) {
            clients.remove(kicked.id);
            kicked.closeConnection();
            waitForPlayers(false);
        }
    }

    public int getUniqueName(int id, String nameToLowerCase, int duplicates) {
        String search = nameToLowerCase;
        if (duplicates > 0) {
            search += " " + duplicates;
        }
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            Client client = IdClientEntry.getValue();
            if (client.id != id && client.name.toLowerCase().equals(search)) {
                return getUniqueName(id, nameToLowerCase, duplicates + 1);
            }
        }
        return duplicates;
    }

    @Override
    public boolean equals(Object obj) {
        if (RoomServer.this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        RoomServer room = (RoomServer) obj;
        return room.getPort() == this.getPort();
    }

    public void diffuseMsg(RoomMsg msg) {
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            IdClientEntry.getValue().sendMsg(msg);
        }
    }

    public String getHostName() {
        Iterator<Entry<Integer, Client>> itr = clients.entrySet().iterator();
        Client host = null;
        while (itr.hasNext()) {
            Client potentialHost = itr.next().getValue();
            if (host == null || potentialHost.id < host.id) {
                host = potentialHost;
            }
        }
        return host != null ? host.name : null;
    }


    protected void request_team_up(RoomMsg msg) {
        Client team_up_with = clients.get((int) msg.adt_data[0]);
        if (team_up_with != null)
            team_up_with.sendMsg(msg);
    }

    protected void take_empty_place(Client client, Object[] adt_data) {
        RoomPosition new_pos = RoomPosition.values()[(int) adt_data[0]];
        place_mutex.lock();
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            Client c = IdClientEntry.getValue();
            if (c.position == new_pos) {
                place_mutex.unlock();
                return;
            }
        }
        client.position = new_pos;
        RoomMsg msg = new RoomMsg(client.id, RoomComm.TOOK_EMPTY_PLACE, adt_data);
        diffuseMsg(msg);
        place_mutex.unlock();
    }

    public void team_up(RoomMsg msg) {
        place_mutex.lock();

        // client who accepted team up request
        Client c1 = clients.get(msg.from);
        // client who requested team up
        Client c2 = clients.get((int) msg.adt_data[0]);

        Client c1_teammate = null;
        RoomPosition c1_teammate_pos = c1.position.teammate_with();
        for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
            Client client = IdClientEntry.getValue();
            if (client.position == c1_teammate_pos) {
                c1_teammate = client;
                break;
            }
        }
        RoomPosition c2_pos = c2.position;
        c2.position = c1_teammate_pos;
        if (c1_teammate != null)
            c1_teammate.position = c2_pos;

        Object[] adt_data = new Object[]{msg.adt_data[0], msg.from};
        msg = new RoomMsg(RoomComm.TEAMED_UP, adt_data);
        diffuseMsg(msg);
        place_mutex.unlock();
    }

    public void denied_team_up(RoomMsg msg) {
        Client rejected = clients.get((int) msg.adt_data[0]);
        if (rejected != null)
            rejected.sendMsg(msg);
    }

    public void NotifyNextPlayer() {
        Client curr_client = null;
        while (curr_client == null && current_pos != null) {
            for (Entry<Integer, Client> IdClientEntry : clients.entrySet()) {
                Client client = IdClientEntry.getValue();
                if (client.position == current_pos) {
                    curr_client = client;
                    break;
                }
            }
            current_pos = current_pos.nextPlayerToNotify();
        }
        if (curr_client != null)
            curr_client.sendMsg(game_started);
        else
            endGame();
    }

    public int getPlayersCount() {
        return clients.size();
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean isFull() {
        return clients.size() == game.players;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean inGamePhase() {
        return gameServer != null;
    }

    public synchronized void endGame() {
        if (gameServer != null) {
            RoomMsg msg = new RoomMsg(RoomComm.GAME_ENDED);
            diffuseMsg(msg);
            gameServer.closeGame();
            gameServer = null;
            waitForPlayers(false);
        }
    }

}
