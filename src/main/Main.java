package main;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import shared.Game;

public class Main {
    private static int PORT;
    private static ServerSocket mainServer;
    protected static final Map<Game, Integer> serverMap = new HashMap<>();

    public static void main(String[] args) {
        Properties prop = new Properties();
        try (InputStream ip = Main.class.getResourceAsStream("/config.properties")) {
            prop.load(ip);
            PORT = Integer.parseInt(prop.getProperty("PORT"));
        } catch (IOException e) {
            System.err.println("Couldn't read properties file!");
            System.exit(1);
        }
        Thread mainThread = new Thread(() -> {
            try {
                mainServer = new ServerSocket(PORT);
                while (true) {
                    try (Socket s = mainServer.accept();
                         DataOutputStream dataOut = new DataOutputStream(s.getOutputStream());
                         DataInputStream datajIn = new DataInputStream(s.getInputStream())) {
                        Game game = Game.values()[datajIn.readInt()];
                        int port = serverMap.get(game);
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

}
