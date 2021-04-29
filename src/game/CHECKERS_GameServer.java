package game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import room.RoomServer;

public class CHECKERS_GameServer extends GameServer {

    public CHECKERS_GameServer(RoomServer room) throws IOException {
        super(room);
    }

    @Override
    public void acceptConnection() {
        try {
            ServerSideConnection player1;
            ServerSideConnection player2;

            room.NotifyNextPlayer();
            Socket c1 = gameServer.accept();
            sockets.add(c1);

            room.NotifyNextPlayer();
            Socket c2 = gameServer.accept();
            sockets.add(c2);

            player1 = new ServerSideConnection(2, new DataInputStream(c1.getInputStream()),
                    new DataOutputStream(c2.getOutputStream()));
            player2 = new ServerSideConnection(1, new DataInputStream(c2.getInputStream()),
                    new DataOutputStream(c1.getOutputStream()));
            Thread p1 = new Thread(player1);
            Thread p2 = new Thread(player2);
            p1.start();
            p2.start();
            player2.dataOut.writeBoolean(true);
            player2.dataOut.flush();
        } catch (IOException ignore) {
        }
    }

    private static class ServerSideConnection implements Runnable {

        private final DataInputStream dataIn;
        private final DataOutputStream dataOut;

        public ServerSideConnection(int otherPlayerID, DataInputStream myIn, DataOutputStream opOut) {
            this.dataIn = myIn;
            dataOut = opOut;
            try {
                dataOut.writeInt(otherPlayerID);
                dataOut.flush();
            } catch (IOException ignore) {
            }
        }

        @Override
        public void run() {
            try {
                while (true) {
                    int pieceX = dataIn.readInt();
                    int pieceY = dataIn.readInt();
                    int toX = dataIn.readInt();
                    int toY = dataIn.readInt();
                    boolean isAttackMove = dataIn.readBoolean();
                    sendCoor(pieceX, pieceY, toX, toY, isAttackMove);
                }
            } catch (IOException ignore) {
            }
        }

        private void sendCoor(int pieceX, int pieceY, int toX, int toY, boolean isAttackMove) {
            try {
                dataOut.writeInt(pieceX);
                dataOut.writeInt(pieceY);
                dataOut.writeInt(toX);
                dataOut.writeInt(toY);
                dataOut.writeBoolean(isAttackMove);
                dataOut.flush();
            } catch (IOException ignore) {
            }
        }
    }
}
