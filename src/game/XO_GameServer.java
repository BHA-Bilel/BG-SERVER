package game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import room.RoomServer;

public class XO_GameServer extends GameServer {

    public XO_GameServer(RoomServer room) throws IOException {
        super(room);
    }

    @Override
    public void acceptConnection() {
        try {
            ServerSideConnection player1;
            ServerSideConnection player2;

            room.NotifyNextPlayer();
            Socket c1 = gameServer.accept();

            room.NotifyNextPlayer();
            Socket c2 = gameServer.accept();

            sockets.add(c1);
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
                    sendCoor(pieceX, pieceY);
                }
            } catch (IOException ignore) {
            }
        }

        private void sendCoor(int x, int y) {
            try {
                dataOut.writeInt(x);
                dataOut.writeInt(y);
                dataOut.flush();
            } catch (IOException ignore) {
            }
        }
    }

}
