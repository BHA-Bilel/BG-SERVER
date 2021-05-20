package bg.server.game;

import java.io.IOException;

import bg.server.room.RoomServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

import bg.server.coinche.model.Card;
import bg.server.coinche.model.Combination;
import bg.server.coinche.model.Deck;
import bg.server.coinche.model.Hand;
import bg.server.coinche.model.Position;
import bg.server.coinche.model.Suit;

public class COINCHE_GameServer extends GameServer {

    /*
     * give : playerID, dernier, cards, playable, exchange : cards
     */
    final CyclicBarrier gate = new CyclicBarrier(4);
    private static ServerSocket buyServer;
    private BuyThread buy1, buy2, buy3, buy4;
    private PlayThread play1, play2, play3, play4;
    private Hand hand1, hand2, hand3, hand4;
    private final Semaphore mutex = new Semaphore(1);

    private final int D;
    private Deck deck;

    public COINCHE_GameServer(RoomServer room) throws IOException {
        super(room);
        buyServer = new ServerSocket(0);
        D = new Random().nextInt(4);
    }

    @Override
    public Object[] getPort() {
        return new Object[]{gameServer.getLocalPort(), buyServer.getLocalPort()};
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
            buyServer.close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public void acceptConnection() {
        try {
            room.NotifyNextPlayer();
            Socket b1 = buyServer.accept();
            sockets.add(b1);
            Socket p1 = gameServer.accept();
            sockets.add(p1);

            room.NotifyNextPlayer();
            Socket b2 = buyServer.accept();
            sockets.add(b2);
            Socket p2 = gameServer.accept();
            sockets.add(p2);

            room.NotifyNextPlayer();
            Socket b3 = buyServer.accept();
            sockets.add(b3);
            Socket p3 = gameServer.accept();
            sockets.add(p3);

            room.NotifyNextPlayer();
            Socket b4 = buyServer.accept();
            sockets.add(b4);
            Socket p4 = gameServer.accept();
            sockets.add(p4);

            buy1 = new BuyThread(1, b1, b2, b3, b4);
            buy2 = new BuyThread(2, b2, b1, b3, b4);
            buy3 = new BuyThread(3, b3, b1, b2, b4);
            buy4 = new BuyThread(4, b4, b1, b2, b3);

            play1 = new PlayThread(1, p1, p2, p3, p4);
            play2 = new PlayThread(2, p2, p1, p3, p4);
            play3 = new PlayThread(3, p3, p1, p2, p4);
            play4 = new PlayThread(4, p4, p1, p2, p3);

        } catch (IOException ignore) {
        }
        buy1.start();
        buy2.start();
        buy3.start();
        buy4.start();
        play1.start();
        play2.start();
        play3.start();
        play4.start();
        startNewGame();
    }

    private void startNewGame() {
        deck = new Deck();
        deck.populate();
        deck.shuffle();
        spreadCards();
        buy1.giveCards(hand1);
        buy2.giveCards(hand2);
        buy3.giveCards(hand3);
        buy4.giveCards(hand4);
    }

    private void spreadCards() {
        ArrayList<Card> h1 = new ArrayList<>();
        ArrayList<Card> h2 = new ArrayList<>();
        ArrayList<Card> h3 = new ArrayList<>();
        ArrayList<Card> h4 = new ArrayList<>();
        int cpt = 0;
        while (!deck.getCards().isEmpty()) {
            Card selected = deck.getCards().get(0);
            deck.remove(selected);
            if (cpt < 8) {
                selected.setPosition(Position.BOTTOM);
                h1.add(selected);
            } else if (cpt < 16) {
                selected.setPosition(Position.RIGHT);
                h2.add(selected);
            } else if (cpt < 24) {
                selected.setPosition(Position.TOP);
                h3.add(selected);
            } else {
                selected.setPosition(Position.LEFT);
                h4.add(selected);
            }
            cpt++;
        }
        hand1 = new Hand(h1);
        hand2 = new Hand(h2);
        hand3 = new Hand(h3);
        hand4 = new Hand(h4);
        hand1.arrange();
        hand2.arrange();
        hand3.arrange();
        hand4.arrange();
    }

    private class BuyThread extends Thread {

        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private List<DataOutputStream> others;

        public BuyThread(int id, Socket yourSocket, Socket b1, Socket b2, Socket b3) {
            try {
                dataIn = new DataInputStream(yourSocket.getInputStream());
                dataOut = new DataOutputStream(yourSocket.getOutputStream());
                dataOut.writeInt(id); // giving id and D position (that was randomly selected)
                dataOut.writeInt(D);
                others = new ArrayList<>();
                others.add(new DataOutputStream(b1.getOutputStream()));
                others.add(new DataOutputStream(b2.getOutputStream()));
                others.add(new DataOutputStream(b3.getOutputStream()));
            } catch (IOException ignore) {
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int received = dataIn.readInt();
                    if (received == -1) { // no one bought
                        startNewGame();
                    } else {
                        for (DataOutputStream dataout : others) {
                            dataout.writeInt(received);
                        }
                    }
                }
            } catch (IOException ignore) {
            }
        }

        private void giveCards(Hand hand) {
            try {
                for (Card card : hand.getCards()) {
                    dataOut.writeInt(card.getSuit().getIndex());
                    dataOut.writeInt(card.getRank().getIndex());
                }
            } catch (IOException ignore) {
            }
        }
    }

    private class PlayThread extends Thread {

        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private List<DataOutputStream> others;
        private final int id;

        public PlayThread(int id, Socket yourSocket, Socket p1, Socket p2, Socket p3) {
            this.id = id;
            try {
                dataIn = new DataInputStream(yourSocket.getInputStream());
                dataOut = new DataOutputStream(yourSocket.getOutputStream());
                others = new ArrayList<>();
                others.add(new DataOutputStream(p1.getOutputStream()));
                others.add(new DataOutputStream(p2.getOutputStream()));
                others.add(new DataOutputStream(p3.getOutputStream()));
            } catch (IOException ignore) {
            }
        }

        private void giveDeclarations(List<Combination> declarations) {
            try {
                if (declarations != null)
                    for (Combination comb : declarations) {
                        dataOut.writeInt(comb.getCards().get(0).getSuit().getIndex());
                        dataOut.writeInt(comb.getRank().getIndex());
                        dataOut.writeInt(comb.getType().getOrder());
                    }
                dataOut.writeInt(-1);
                dataOut.writeInt(-1);
                dataOut.writeInt(-1);
            } catch (IOException ignore) {
            }
        }

        public void check(int trump) {
            hand1.check(Suit.get(trump));
            hand2.check(Suit.get(trump));
            hand3.check(Suit.get(trump));
            hand4.check(Suit.get(trump));

            ArrayList<List<Combination>> highestDecs = new ArrayList<>();
            highestDecs.add(hand1.getDeclarations());
            highestDecs.add(hand2.getDeclarations());
            highestDecs.add(hand3.getDeclarations());
            highestDecs.add(hand4.getDeclarations());
            Combination highestComb = null;
            for (List<Combination> Decs : highestDecs) {
                if (Decs != null)
                    if (highestComb == null)
                        highestComb = Decs.get(0);
                    else {
                        boolean highestOrder = highestComb.getType().getOrder() < Decs.get(0).getType().getOrder();
                        boolean sameOrder = highestComb.getType().getOrder() == Decs.get(0).getType().getOrder();
                        boolean highestRank = highestComb.getRank().getIndex() < Decs.get(0).getRank().getIndex();
                        boolean sameRank = highestComb.getRank().getIndex() == Decs.get(0).getRank().getIndex();
                        boolean isTrump = Decs.get(0).getCards().get(0).getSuit() == Suit.get(trump);
                        if (highestOrder || sameOrder && highestRank || sameOrder && sameRank && isTrump) {
                            highestComb = Decs.get(0);
                        }
                    }
            }
            if (highestComb != null) {
                if (highestComb.getPosition() == Position.BOTTOM || highestComb.getPosition() == Position.TOP) {
                    hand2.removeDeclarations();
                    hand4.removeDeclarations();
                } else {
                    hand1.removeDeclarations();
                    hand3.removeDeclarations();
                }
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int trump = dataIn.readInt();
                    if (trump < 4) { // getting trump index to carry on (must not be sa or ta)
                        if (id == 1)
                            check(trump);
                        gate.await();
                        mutex.acquire();
                        giveDeclarations(hand1.getDeclarations());
                        giveDeclarations(hand2.getDeclarations());
                        giveDeclarations(hand3.getDeclarations());
                        giveDeclarations(hand4.getDeclarations());
                        mutex.release();
                        gate.reset();
                    }
                    for (int i = 0; i < 16; i++) {
                        int receive = dataIn.readInt(); // receiving an int and giving it to other players
                        for (DataOutputStream dataout : others) {
                            dataout.writeInt(receive);
                        }
                    }
                    dataIn.readInt();
                    gate.await();
                    if (id == 1)
                        startNewGame();
                    gate.reset();
                }
            } catch (IOException | InterruptedException | BrokenBarrierException ignore) {
            }
        }
    }
}
