package ARS.manager;

import ARS.ElyGuard;
import ARS.client.Client;
import ARS.client.Key;
import ARS.client.Role;
import ARS.client.User;

import java.util.ArrayList;

public class ClientManager {
    public ArrayList<Client> clients = new ArrayList<>();

    public ArrayList<Client> getClients() {
        return clients;
    }

    public Client moonware = new Client("MoonWare", "1.12.2");
    public void loadClients() {
        clients.add(moonware);
        ElyGuard.getInstance().getDatabase().loadDatabase(clients);
    }
}
