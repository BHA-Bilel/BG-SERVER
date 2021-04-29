package game;

import java.io.IOException;

import room.RoomServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import dominos.model.Deck;
import dominos.model.Domino;
import dominos.model.GameType;
import dominos.model.Position;

public class DOMINOS_GameServer extends GameServer {

    /*
     * give : playerID, firstToPlay, dominoes, playable, exchange : dominoes && draw
     */
    public final GameType GAME_TYPE;
    public ServerSocket blockServer, SpreadWinServer;
    private SpreadWinThread sw1, sw2, sw3, sw4;
    private PlayThread play1, play2, play3, play4;
    private BlockThread block;
    private int drawCount;
    private List<Domino> player1dominoes, player2dominoes, player3dominoes, player4dominoes;

    private Deck deck;

    public DOMINOS_GameServer(RoomServer room, GameType GAME_TYPE) throws IOException {
        super(room);
        this.GAME_TYPE = GAME_TYPE;
        deck = new Deck();
        deck.populate();
        deck.shuffle();
        spreadDominoes();
        this.SpreadWinServer = new ServerSocket(0);
        this.blockServer = new ServerSocket(0);
    }

    @Override
    public Object[] getPort() {
        return new Object[]{gameServer.getLocalPort(), SpreadWinServer.getLocalPort(), blockServer.getLocalPort()};
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
            blockServer.close();
            SpreadWinServer.close();
        } catch (IOException ignore) {
        }
    }

    @Override
    public void acceptConnection() {
        try {
            if (GAME_TYPE == GameType.TwovTwo) {

                room.NotifyNextPlayer();
                Socket w1 = SpreadWinServer.accept();
                sockets.add(w1);
                Socket p1 = gameServer.accept();
                sockets.add(p1);
                Socket b1 = blockServer.accept();
                sockets.add(b1);

                room.NotifyNextPlayer();
                Socket w2 = SpreadWinServer.accept();
                sockets.add(w2);
                Socket p2 = gameServer.accept();
                sockets.add(p2);
                Socket b2 = blockServer.accept();
                sockets.add(b2);

                room.NotifyNextPlayer();
                Socket w3 = SpreadWinServer.accept();
                sockets.add(w3);
                Socket p3 = gameServer.accept();
                sockets.add(p3);
                Socket b3 = blockServer.accept();
                sockets.add(b3);

                room.NotifyNextPlayer();
                Socket w4 = SpreadWinServer.accept();
                sockets.add(w4);
                Socket p4 = gameServer.accept();
                sockets.add(p4);
                Socket b4 = blockServer.accept();
                sockets.add(b4);

                sw1 = new SpreadWinThread(1, w1, w2, w3, w4);
                sw2 = new SpreadWinThread(2, w2, w1, w3, w4);
                sw3 = new SpreadWinThread(3, w3, w1, w2, w4);
                sw4 = new SpreadWinThread(4, w4, w1, w2, w3);

                play1 = new PlayThread(p1, p2, p3, p4);
                play2 = new PlayThread(p2, p1, p3, p4);
                play3 = new PlayThread(p3, p1, p2, p4);
                play4 = new PlayThread(p4, p1, p2, p3);

                block = new BlockThread(b1, b2, b3, b4);

            } else if (GAME_TYPE == GameType.ThreePlayers) {

                room.NotifyNextPlayer();
                Socket w1 = SpreadWinServer.accept();
                sockets.add(w1);
                Socket p1 = gameServer.accept();
                sockets.add(p1);
                Socket b1 = blockServer.accept();
                sockets.add(b1);

                room.NotifyNextPlayer();
                Socket w2 = SpreadWinServer.accept();
                sockets.add(w2);
                Socket p2 = gameServer.accept();
                sockets.add(p2);
                Socket b2 = blockServer.accept();
                sockets.add(b2);

                room.NotifyNextPlayer();
                Socket w3 = SpreadWinServer.accept();
                sockets.add(w3);
                Socket p3 = gameServer.accept();
                sockets.add(p3);
                Socket b3 = blockServer.accept();
                sockets.add(b3);

                sw1 = new SpreadWinThread(1, w1, w2, w3);
                sw2 = new SpreadWinThread(2, w2, w1, w3);
                sw3 = new SpreadWinThread(3, w3, w2, w1);

                play1 = new PlayThread(p1, p2, p3);
                play2 = new PlayThread(p2, p1, p3);
                play3 = new PlayThread(p3, p1, p2);

                block = new BlockThread(b1, b2, b3);

            } else {
                room.NotifyNextPlayer();
                Socket w1 = SpreadWinServer.accept();
                sockets.add(w1);
                Socket p1 = gameServer.accept();
                sockets.add(p1);
                Socket b1 = blockServer.accept();
                sockets.add(b1);

                room.NotifyNextPlayer();
                Socket w2 = SpreadWinServer.accept();
                sockets.add(w2);
                Socket p2 = gameServer.accept();
                sockets.add(p2);
                Socket b2 = blockServer.accept();
                sockets.add(b2);

                sw1 = new SpreadWinThread(1, w1, w2);
                sw2 = new SpreadWinThread(2, w2, w1);

                play1 = new PlayThread(p1, p2);
                play2 = new PlayThread(p2, p1);

                block = new BlockThread(b1, b2);
            }

        } catch (IOException ignore) {
        }
        sw1.start();
        sw2.start();
        play1.start();
        play2.start();
        block.start();
        if (GAME_TYPE != GameType.OnevOne) {
            sw3.start();
            play3.start();
            if (GAME_TYPE == GameType.TwovTwo) {
                sw4.start();
                play4.start();
            }
        }
        giveDominoes();
    }

    private Domino draw() {
        Domino selected = deck.getDominos().get(0);
        deck.remove(selected);
        return selected;
    }

    private int whoStart() {
        Domino six = new Domino(6, 6, Position.CENTER);
        for (Domino domino : player2dominoes) {
            if (domino.equals(six)) {
                return 2;
            }
        }
        if (GAME_TYPE != GameType.OnevOne) {
            for (Domino domino : player3dominoes) {
                if (domino.equals(six)) {
                    return 3;
                }
            }
            if (GAME_TYPE == GameType.TwovTwo) {
                for (Domino domino : player4dominoes) {
                    if (domino.equals(six)) {
                        return 4;
                    }
                }
            }
        }
        return 1; // if six want's found in 2 3 4 players dominos, it must be in player one dominos
    }

    private void spreadDominoes() {
        if (GAME_TYPE == GameType.TwovTwo) {
            player1dominoes = new ArrayList<>();
            player2dominoes = new ArrayList<>();
            player3dominoes = new ArrayList<>();
            player4dominoes = new ArrayList<>();
            int cpt = 0;
            while (!deck.getDominos().isEmpty()) {
                Domino selected = deck.getDominos().get(0);
                deck.remove(selected);
                if (cpt < 7) {
                    selected.setPosition(Position.BOTTOM);
                    player1dominoes.add(selected);
                } else if (cpt < 14) {
                    selected.setPosition(Position.RIGHT);
                    player2dominoes.add(selected);
                } else if (cpt < 21) {
                    selected.setPosition(Position.TOP);
                    player3dominoes.add(selected);
                } else {
                    selected.setPosition(Position.LEFT);
                    player4dominoes.add(selected);
                }
                cpt++;
            }
        } else if (GAME_TYPE == GameType.ThreePlayers) {
            player1dominoes = new ArrayList<>();
            player2dominoes = new ArrayList<>();
            player3dominoes = new ArrayList<>();
            int cpt = 0;
            while (deck.getDominos().size() > 7) {
                Domino selected = deck.getDominos().get(0);
                deck.remove(selected);
                if (cpt < 7) {
                    selected.setPosition(Position.BOTTOM);
                    player1dominoes.add(selected);
                } else if (cpt < 14) {
                    selected.setPosition(Position.RIGHT);
                    player2dominoes.add(selected);
                } else {
                    selected.setPosition(Position.TOP);
                    player3dominoes.add(selected);
                }
                cpt++;
            }
        } else { // ONE VS ONE
            player1dominoes = new ArrayList<>();
            player2dominoes = new ArrayList<>();
            int cpt = 0;
            while (deck.getDominos().size() > 14) {
                Domino selected = deck.getDominos().get(0);
                deck.remove(selected);
                if (cpt < 7) {
                    selected.setPosition(Position.BOTTOM);
                    player1dominoes.add(selected);
                } else {
                    selected.setPosition(Position.RIGHT);
                    player2dominoes.add(selected);
                }
                cpt++;
            }
        }
    }

    private void startNewGame() {
        deck = new Deck();
        deck.populate();
        deck.shuffle();
        spreadDominoes();
        giveDominoes();
    }

    private void giveDominoes() {
        sw1.giveDominos(player1dominoes);
        sw2.giveDominos(player2dominoes);
        if (GAME_TYPE != GameType.OnevOne) {
            sw3.giveDominos(player3dominoes);
            if (GAME_TYPE == GameType.TwovTwo) {
                sw4.giveDominos(player4dominoes);
            }
        }
    }

    private class PlayThread extends Thread {

        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private List<DataOutputStream> others;

        public PlayThread(Socket p1, Socket p2, Socket p3, Socket p4) {
            try {
                dataIn = new DataInputStream(p1.getInputStream());
                dataOut = new DataOutputStream(p1.getOutputStream());
                others = new ArrayList<>();
                others.add(new DataOutputStream(p2.getOutputStream()));
                others.add(new DataOutputStream(p3.getOutputStream()));
                others.add(new DataOutputStream(p4.getOutputStream()));
            } catch (IOException ignore) {
            }
        }

        public PlayThread(Socket p1, Socket p2, Socket p3) {
            try {
                dataIn = new DataInputStream(p1.getInputStream());
                dataOut = new DataOutputStream(p1.getOutputStream());
                others = new ArrayList<>();
                others.add(new DataOutputStream(p2.getOutputStream()));
                others.add(new DataOutputStream(p3.getOutputStream()));
            } catch (IOException ignore) {
            }

        }

        public PlayThread(Socket p1, Socket p2) {
            try {
                dataIn = new DataInputStream(p1.getInputStream());
                dataOut = new DataOutputStream(p1.getOutputStream());
                others = new ArrayList<>();
                others.add(new DataOutputStream(p2.getOutputStream()));
            } catch (IOException ignore) {
            }

        }

        @Override
        public void run() {
            try {
                while (true) {
                    int receive = dataIn.readInt(); // receiving an int and giving it to other players
                    if (receive == -1) { // draw value
                        Domino draw = draw();
                        dataOut.writeInt(draw.getLeftValue());
                        dataOut.writeInt(draw.getRightValue());
                    }
                    for (DataOutputStream dataout : others) {
                        dataout.writeInt(receive);
                    }
                }
            } catch (IOException ignore) {
            }
        }
    }

    private class BlockThread extends Thread {

        private List<DataInputStream> dataIn;
        private List<DataOutputStream> dataOut;

        public BlockThread(Socket b1, Socket b2, Socket b3, Socket b4) {
            try {
                dataIn = new ArrayList<>();
                dataIn.add(new DataInputStream(b1.getInputStream()));
                dataIn.add(new DataInputStream(b2.getInputStream()));
                dataIn.add(new DataInputStream(b3.getInputStream()));
                dataIn.add(new DataInputStream(b4.getInputStream()));

                dataOut = new ArrayList<>();
                dataOut.add(new DataOutputStream(b1.getOutputStream()));
                dataOut.add(new DataOutputStream(b2.getOutputStream()));
                dataOut.add(new DataOutputStream(b3.getOutputStream()));
                dataOut.add(new DataOutputStream(b4.getOutputStream()));
            } catch (IOException ignore) {
            }

        }

        public BlockThread(Socket b1, Socket b2, Socket b3) {
            try {
                dataIn = new ArrayList<>();
                dataIn.add(new DataInputStream(b1.getInputStream()));
                dataIn.add(new DataInputStream(b2.getInputStream()));
                dataIn.add(new DataInputStream(b3.getInputStream()));

                dataOut = new ArrayList<>();
                dataOut.add(new DataOutputStream(b1.getOutputStream()));
                dataOut.add(new DataOutputStream(b2.getOutputStream()));
                dataOut.add(new DataOutputStream(b3.getOutputStream()));
            } catch (IOException ignore) {
            }

        }

        public BlockThread(Socket b1, Socket b2) {
            try {
                dataIn = new ArrayList<>();
                dataIn.add(new DataInputStream(b1.getInputStream()));
                dataIn.add(new DataInputStream(b2.getInputStream()));

                dataOut = new ArrayList<>();
                dataOut.add(new DataOutputStream(b1.getOutputStream()));
                dataOut.add(new DataOutputStream(b2.getOutputStream()));
            } catch (IOException ignore) {
            }
        }

        class Block {
            private final int id;
            private final int value;

            public Block(int id, int value) {
                this.id = id;
                this.value = value;
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Hashtable<Integer, Integer> Blocks = new Hashtable<>();
                    for (DataInputStream in : dataIn) {
                        int id = in.readInt();
                        int value = in.readInt();
                        Blocks.put(id, value);
                    }
                    Block won;
                    if (GAME_TYPE == GameType.TwovTwo) {
                        int team1Value = Blocks.get(1) + Blocks.get(3);
                        int team2Value = Blocks.get(2) + Blocks.get(4);

                        int minteam1Value = Math.min(Blocks.get(1), Blocks.get(3));
                        int minteam2Value = Math.min(Blocks.get(2), Blocks.get(4));

                        if (minteam1Value < minteam2Value) {
                            if (minteam1Value == Blocks.get(1))
                                won = new Block(1, team2Value);
                            else
                                won = new Block(3, team2Value);
                        } else if (minteam2Value < minteam1Value) {
                            if (minteam1Value == Blocks.get(2))
                                won = new Block(2, team1Value);
                            else
                                won = new Block(4, team1Value);
                        } else {
                            drawCount = drawCount % 4 + 1;
                            won = new Block(drawCount, 0);
                        }
                    } else if (GAME_TYPE == GameType.ThreePlayers) {
                        int team1Value = Blocks.get(1);
                        int team2Value = Blocks.get(2);
                        int team3Value = Blocks.get(3);
                        int min = Math.min(team1Value, Math.min(team2Value, team3Value));
                        if (min == team1Value) {
                            if (min == team2Value || min == team3Value) { // EQUALITY
                                drawCount = drawCount % 3 + 1;
                                won = new Block(drawCount, 0);
                            } else { // team1 won
                                won = new Block(1, team2Value + team3Value);
                            }
                        } else if (min == team2Value) {
                            if (min == team3Value) { // EQUALITY
                                drawCount = drawCount % 3 + 1;
                                won = new Block(drawCount, 0);
                            } else { // team2 won
                                won = new Block(2, team1Value + team3Value);
                            }
                        } else { // team3 won !
                            won = new Block(3, team1Value + team2Value);
                        }
                    } else { // 1 VS 1
                        int team1Value = Blocks.get(1);
                        int team2Value = Blocks.get(2);
                        if (team1Value < team2Value) { // team 1 won
                            won = new Block(1, team2Value);
                        } else if (team2Value < team1Value) { // team2 won
                            won = new Block(2, team1Value);
                        } else { // EQUALITY
                            drawCount = drawCount % 2 + 1;
                            won = new Block(drawCount, 0);
                        }
                    }
                    for (DataOutputStream out : dataOut) {
                        out.writeInt(won.id);
                        out.writeInt(won.value);
                    }
                }
            } catch (IOException ignore) {
            }
        }
    }

    private class SpreadWinThread extends Thread {

        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private List<DataOutputStream> others;

        public SpreadWinThread(int id, Socket w1, Socket w2, Socket w3, Socket w4) {
            try {
                dataOut = new DataOutputStream(w1.getOutputStream());
                dataIn = new DataInputStream(w1.getInputStream());
                dataOut.writeInt(id); // giving id and who start
                dataOut.writeInt(whoStart());
                others = new ArrayList<>();
                others.add(new DataOutputStream(w2.getOutputStream()));
                others.add(new DataOutputStream(w4.getOutputStream()));
                others.add(new DataOutputStream(w3.getOutputStream()));
            } catch (IOException ignore) {
            }
        }

        public SpreadWinThread(int id, Socket w1, Socket w2, Socket w3) {
            try {
                dataOut = new DataOutputStream(w1.getOutputStream());
                dataOut.writeInt(id); // giving id and who start
                dataOut.writeInt(whoStart());
                dataIn = new DataInputStream(w1.getInputStream());
                others = new ArrayList<>();
                others.add(new DataOutputStream(w2.getOutputStream()));
                others.add(new DataOutputStream(w3.getOutputStream()));
            } catch (IOException ignore) {
            }
        }

        public SpreadWinThread(int id, Socket w1, Socket w2) {
            try {
                dataOut = new DataOutputStream(w1.getOutputStream());
                dataOut.writeInt(id); // giving id and who start
                dataOut.writeInt(whoStart());
                dataIn = new DataInputStream(w1.getInputStream());
                others = new ArrayList<>();
                others.add(new DataOutputStream(w2.getOutputStream()));
            } catch (IOException ignore) {
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int value = dataIn.readInt();
                    if (value == -1)
                        startNewGame();
                    else
                        for (DataOutputStream out : others) {
                            out.writeInt(value);
                        }
                }
            } catch (IOException ignore) {
            }
        }

        public void giveDominos(List<Domino> dominoes) {
            try {
                for (Domino domino : dominoes) {
                    dataOut.writeInt(domino.getLeftValue());
                    dataOut.writeInt(domino.getRightValue());
                }
            } catch (IOException ignore) {
            }
        }
    }
}
