package ARS;

import ARS.event.UpdateEvent;
import ARS.manager.ClientManager;
import ARS.manager.DatabaseManager;
import ARS.network.Server;
import ARS.network.l7.WebServer;

import java.io.IOException;

public class ElyGuard {
    private static ElyGuard instance;

    public static ElyGuard getInstance() {
        if (instance == null) {
            instance = new ElyGuard();
        }
        return instance;
    }

    private ClientManager client = new ClientManager();
    private DatabaseManager database = new DatabaseManager();
    private Server server = Server.getInstance();

    public Server getServer() {
        return server;
    }
    public ClientManager getClient() {
        return client;
    }
    public DatabaseManager getDatabase() {
        return database;
    }

    public void run0() {
        UpdateEvent.update();
        client.loadClients();
        new Thread(() -> {
            WebServer web = new WebServer();
            try {
                web.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(()-> {
            try {
                server.start(8080);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
