package bg.server;

import bg.server.main.MainGameServer;
import shared.Game;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static ServerSocket mainServer;
    public static final Map<Game, Integer> serverMap = new HashMap<>();

    public static void main(String[] args) {
        Thread mainThread = new Thread(() -> {
            try {
                mainServer = new ServerSocket(0);
                System.out.println("MAIN SERVER STARTED");
                System.out.println("IP: " + get_public_ip());
                System.out.println("PORT: " + mainServer.getLocalPort());
                while (true) {
                    try (Socket s = mainServer.accept();
                         DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
                         DataInputStream datajIn = new DataInputStream(s.getInputStream())) {
                        Game game = Game.values()[datajIn.readInt()];
                        Integer port = serverMap.get(game);
                        if (port == null) port = -1;
                        dataOut.writeInt(port);
                        dataOut.flush();
                    } catch (IOException ignore) {
                    }
                }
            } catch (IOException e) {
                System.err.println("MAIN SERVER FAIL");
                System.exit(2);
            }
        });
        for (Game game : Game.values()) {
            try {
                MainGameServer gameThread = new MainGameServer(game);
                gameThread.start();
            } catch (IOException e) {
                System.err.println(game + " SERVER FAIL");
                System.exit(3);
            }
        }
        mainThread.start();
    }

    private static String get_public_ip() throws IOException {
        return new BufferedReader(new InputStreamReader(
                new URL("http://checkip.amazonaws.com").openStream())).readLine();
    }

}
