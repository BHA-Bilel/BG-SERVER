package bg.server.game;

import bg.server.dominos.model.Deck;
import bg.server.dominos.model.Domino;
import bg.server.dominos.model.GameType;
import bg.server.room.RoomServer;
import shared.domino.DominoComm;
import shared.domino.DominoMsg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DOMINOS_GameServer extends GameServer {

    public final GameType GAME_TYPE;
    private final Map<Integer, GameServerClient> clients;
    private final Map<Integer, List<Domino>> dominoes;
    private int drawCount;
    private final Deck deck;

    public DOMINOS_GameServer(RoomServer room, GameType GAME_TYPE) throws IOException {
        super(room);
        this.GAME_TYPE = GAME_TYPE;
        clients = new HashMap<>();
        dominoes = new HashMap<>();
        deck = new Deck();
    }

    @Override
    public void closeGame() {
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
        }
        try {
            gameServer.close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public void acceptConnection() {
        try {
            int id = 1;
            while (id <= GAME_TYPE.players) {
                room.NotifyNextPlayer();
                Socket socket = gameServer.accept();
                sockets.add(socket);
                GameServerClient client = new GameServerClient(id, socket);
                clients.put(id, client);
                id++;
            }
        } catch (IOException ignore) {
        }
        deck.prepare();
        spreadDominoes();
        int who = whoStart();
        clients.forEach((id, client) -> client.handShakeRun(who));
        giveDominoes();
    }

    private void startNewGame() {
        deck.prepare();
        spreadDominoes();
        giveDominoes();
    }

    private void giveDominoes() {
        clients.forEach((id, client) ->
                client.send_msg(new DominoMsg(DominoComm.NEW_GAME, Domino.to_array(dominoes.get(id))))
        );
    }

    private Domino draw() {
        Domino selected = deck.getDominos().get(0);
        deck.remove(selected);
        return selected;
    }

    private int whoStart() {
        Domino six = new Domino(6, 6);
        for (Map.Entry<Integer, List<Domino>> entry : dominoes.entrySet())
            if (entry.getValue().contains(six)) return entry.getKey();
        return 1; // if six is nowhere to be found, first player starts
    }

    private void spreadDominoes() {
        int id = 1;
        while (id <= GAME_TYPE.players) {
            List<Domino> player_dominoes = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Domino selected = deck.getDominos().get(0);
                deck.remove(selected);
                player_dominoes.add(selected);
            }
            dominoes.put(id, player_dominoes);
            id++;
        }
    }

    private int removeDomino(int left, int right) {
        Domino played = new Domino(left, right);
        boolean found = dominoes.get(1).remove(played);
        if (found) {
            if (!dominoes.get(1).isEmpty()) return -1;
            switch (GAME_TYPE) {
                case TwovTwo:
                    return getValue(dominoes.get(2)) + getValue(dominoes.get(4));
                case OnevOne:
                    return getValue(dominoes.get(2));
                case ThreePlayers:
                    return getValue(dominoes.get(2)) + getValue(dominoes.get(3));
            }
        }
        found = dominoes.get(2).remove(played);
        if (found) {
            if (!dominoes.get(2).isEmpty()) return -1;
            switch (GAME_TYPE) {
                case TwovTwo:
                case ThreePlayers:
                    return getValue(dominoes.get(1)) + getValue(dominoes.get(3));
                case OnevOne:
                    return getValue(dominoes.get(1));
            }
        }
        found = dominoes.get(3).remove(played);
        if (found) {
            if (!dominoes.get(3).isEmpty()) return -1;
            switch (GAME_TYPE) {
                case TwovTwo:
                    return getValue(dominoes.get(2)) + getValue(dominoes.get(4));
                case ThreePlayers:
                    return getValue(dominoes.get(1)) + getValue(dominoes.get(2));
            }
        }
        dominoes.get(4).remove(played); // automatically remove from 4th player as we didn't find it in all the others
        if (!dominoes.get(4).isEmpty()) return -1; // round's not over yet
        return getValue(dominoes.get(1)) + getValue(dominoes.get(3)); // only two v two has 4 players
    }

    private void diffuse_msg(DominoMsg msg) {
        clients.forEach((id, client) -> client.send_msg(msg));
    }

    private void send_others(int id, DominoMsg msg) throws IOException {
        clients.entrySet().stream().filter((entry) -> entry.getKey() != id).
                forEach((entry) -> entry.getValue().send_msg(msg));
    }

    private void blocked() {
        int p1_value = getValue(dominoes.get(1));
        int p2_value = getValue(dominoes.get(2));
        int id = -1, value = -1;
        switch (GAME_TYPE) {
            case TwovTwo: {
                int p3_value = getValue(dominoes.get(3));
                int p4_value = getValue(dominoes.get(4));

                int minteam1Value = Math.min(p1_value, p3_value);
                int minteam2Value = Math.min(p2_value, p4_value);

                if (minteam1Value < minteam2Value) {
                    value = p2_value + p4_value;
                    if (minteam1Value == p1_value) id = 1;
                    else id = 3;
                } else if (minteam2Value < minteam1Value) {
                    value = p1_value + p3_value;
                    if (minteam2Value == p2_value) id = 2;
                    else id = 4;
                } else { // EQUALITY
                    drawCount = drawCount % 4 + 1;
                    id = drawCount;
                    value = 0;
                }
                break;
            }
            case ThreePlayers: {
                int p3_value = getValue(dominoes.get(3));
                int min = Math.min(p1_value, Math.min(p2_value, p3_value));
                if (min == p1_value) {
                    if (min == p2_value || min == p3_value) { // EQUALITY
                        drawCount = drawCount % 3 + 1;
                        id = drawCount;
                        value = 0;
                    } else { // player 1 won
                        id = 1;
                        value = p2_value + p3_value;
                    }
                } else if (min == p2_value) {
                    if (min == p3_value) { // EQUALITY
                        drawCount = drawCount % 3 + 1;
                        id = drawCount;
                        value = 0;
                    } else { // player 2 won
                        id = 2;
                        value = p1_value + p3_value;
                    }
                } else { // player 3 won
                    id = 3;
                    value = p1_value + p2_value;
                }
                break;
            }
            case OnevOne: {
                if (p1_value < p2_value) { // player 1 won
                    id = 1;
                    value = p2_value;
                } else if (p2_value < p1_value) { // player 2 won
                    id = 2;
                    value = p1_value;
                } else { // EQUALITY
                    drawCount = drawCount % 2 + 1;
                    id = drawCount;
                    value = 0;
                }
                break;
            }
        }
        diffuse_msg(new DominoMsg(DominoComm.BLOCK_RESULT, new Object[]{id, value}));
    }

    private int getValue(List<Domino> dominoes) {
        int cpt = 0;
        for (Domino domino : dominoes) {
            cpt += domino.getLeftValue() + domino.getRightValue();
        }
        return cpt;
    }

    private class GameServerClient extends Thread {

        private ObjectInputStream objIn;
        private ObjectOutputStream objOut;
        private int id;

        public GameServerClient(int id, Socket p1) {
            try {
                this.id = id;
                objOut = new ObjectOutputStream(p1.getOutputStream());
                objIn = new ObjectInputStream(p1.getInputStream());
            } catch (IOException ignore) {
            }
        }

        private void handShakeRun(int who) {
            try {
                objOut.writeInt(id); // giving id and who start
                objOut.writeInt(who);
                objOut.flush();
                start();
            } catch (IOException ignore) {
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    DominoMsg msg = (DominoMsg) objIn.readObject();
                    DominoComm msg_comm = DominoComm.values()[msg.comm];
                    switch (msg_comm) {
                        case PLAYED_LEFT:
                        case PLAYED_RIGHT: {
                            send_others(id, msg);
                            int points = removeDomino((int) msg.adt_data[0], (int) msg.adt_data[1]);
                            if (points >= 0) {
                                diffuse_msg(new DominoMsg(DominoComm.GAME_END, new Object[]{points}));
                                startNewGame();
                            }
                            break;
                        }
                        case BLOCKED: {
                            blocked();
                            startNewGame();
                            break;
                        }
                        case DRAW: {
                            Domino draw = draw();
                            dominoes.get(id).add(draw);
                            send_msg(new DominoMsg(msg_comm, draw.to_array()));
                            send_others(id, msg);
                            break;
                        }
                        default: {
                            send_others(id, msg);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException ignore) {
            }
        }

        public void send_msg(DominoMsg msg) {
            try {
                objOut.writeObject(msg);
                objOut.flush();
            } catch (IOException ignore) {
            }
        }
    }
}