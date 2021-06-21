package bg.server.game;

import bg.server.coinche.model.*;
import bg.server.room.RoomServer;
import shared.RoomPosition;
import shared.coinche.CoincheComm;
import shared.coinche.CoincheMsg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.*;

public class COINCHE_GameServer extends GameServer {

    private final Map<Integer, GameServerClient> clients;
    private final Map<Integer, Hand> hands;
    private final Deck deck;

    public COINCHE_GameServer(RoomServer room) throws IOException {
        super(room);
        deck = new Deck();
        clients = new HashMap<>();
        hands = new HashMap<>();
    }

    @Override
    public void acceptConnection() {
        try {
            int id = 1;
            while (id <= 4) {
                room.NotifyNextPlayer();
                Socket socket = gameServer.accept();
                sockets.add(socket);
                GameServerClient client = new GameServerClient(id, socket);
                clients.put(id, client);
                id++;
            }
        } catch (IOException ignore) {
        }
        int dealer = new Random().nextInt(4);
        clients.forEach((id, client) -> client.handShakeRun(dealer));
        startNewGame();
    }

    private void startNewGame() {
        hands.clear();
        deck.prepare();
        spreadCards();
        giveCards();
    }

    private void spreadCards() {
        int id = 1, i = 0;
        for (RoomPosition position : RoomPosition.values()) {
            ArrayList<Card> player_cards = new ArrayList<>();
            for (int j = 0; j < 8; j++) {
                Card selected = deck.getCards().get(i);
                selected.setPosition(position);
                player_cards.add(selected);
                i++;
            }
            hands.put(id, new Hand(player_cards));
            id++;
        }
    }

    private void giveCards() {
        clients.forEach((id, client) ->
                client.send_msg(new CoincheMsg(
                        CoincheComm.NEW_GAME, Card.to_array(hands.get(id)))));
    }

    private void diffuse_msg(CoincheMsg msg) {
        clients.forEach((id, client) -> client.send_msg(msg));
    }

    private void send_others(int id, CoincheMsg msg) throws IOException {
        clients.entrySet().stream().filter((entry) -> entry.getKey() != id).
                forEach((entry) -> entry.getValue().send_msg(msg));
    }

    private class GameServerClient extends Thread {

        private ObjectInputStream objIn;
        private ObjectOutputStream objOut;
        private int id;

        public GameServerClient(int id, Socket socket) {
            try {
                this.id = id;
                objOut = new ObjectOutputStream(socket.getOutputStream());
                objIn = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ignore) {
            }
        }

        private void handShakeRun(int dealer) {
            try {
                objOut.writeInt(id); // giving id and dealer position (that was randomly selected)
                objOut.writeInt(dealer);
                objOut.flush();
                start();
            } catch (IOException ignore) {
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    CoincheMsg msg = (CoincheMsg) objIn.readObject();
                    CoincheComm msg_comm = CoincheComm.values()[msg.comm];
                    switch (msg_comm) {
                        case GAME_END: {
                            startNewGame();
                            break;
                        }
                        case TRUMP: {
                            giveDeclarations(check_declarations((int) msg.adt_data[0]));
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

        public boolean check_declarations(int trump) {
            Suit trump_suit = Suit.values()[trump];
            for (Map.Entry<Integer, Hand> entry : hands.entrySet()) entry.getValue().check(trump_suit);
            ArrayList<List<Combination>> highestDecs = new ArrayList<>();
            hands.forEach((id, hand) -> highestDecs.add(hand.getDeclarations()));
            Combination highestComb = null;
            for (List<Combination> Decs : highestDecs) {
                if (Decs == null) continue;
                if (highestComb == null) highestComb = Decs.get(0);
                else {
                    boolean higher_type = highestComb.getType().ordinal() > Decs.get(0).getType().ordinal();
                    boolean same_type = highestComb.getType().equals(Decs.get(0).getType());
                    boolean higher_rank = highestComb.getRank().ordinal() < Decs.get(0).getRank().ordinal();
                    boolean same_rank = highestComb.getRank().equals(Decs.get(0).getRank());
                    boolean isTrump = Decs.get(0).getSuit().equals(trump_suit);
                    if (higher_type || same_type && higher_rank || same_type && same_rank && isTrump) {
                        highestComb = Decs.get(0);
                    }
                }
            }
            if (highestComb != null) {
                boolean team1_high = highestComb.getPosition() == RoomPosition.BOTTOM || highestComb.getPosition() == RoomPosition.TOP;
                hands.entrySet().stream().filter(entry -> team1_high == (entry.getKey() % 2 == 0)).
                        forEach(entry -> entry.getValue().removeDeclarations());
            }
            return highestComb != null;
        }

        private void giveDeclarations(boolean thereIS) {
            if (thereIS) {
                hands.forEach((id, hand) -> {
                    if (hand.have_declarations()) diffuse_msg(new CoincheMsg(CoincheComm.DECLARATIONS,
                            Combination.to_array(hand.getDeclarations())));
                });
            }
            diffuse_msg(new CoincheMsg(CoincheComm.DECLARATIONS));
        }

        public void send_msg(CoincheMsg msg) {
            try {
                objOut.writeObject(msg);
                objOut.flush();
            } catch (IOException ignore) {
            }
        }
    }
}
