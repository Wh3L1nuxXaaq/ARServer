package ARS.manager;

import ARS.client.Client;
import ARS.client.Key;
import ARS.client.User;
import ARS.fast.Constant;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    public void loadDatabase(List<Client> clients) {
        File dbFolder = new File(Constant.dbPath);
        if (!dbFolder.exists()) {
            dbFolder.mkdirs();
        }

        for (Client client : clients) {
            File clientFolder = new File(Constant.dbPath, client.getName());
            if (!clientFolder.exists()) {
                clientFolder.mkdirs();
            }

            loadClientData(client);
        }
    }

    private void loadClientData(Client client) {
        File clientDir = new File(Constant.dbPath, client.getName());

        File clientFile = new File(clientDir, "client.json");
        if (!clientFile.exists()) {
            saveClientData(client);
        }

        File usersFile = new File(clientDir, "users.json");
        if (!usersFile.exists()) {
            saveUsers(client);
        } else {
            client.getUsers().addAll(loadUsers(usersFile));
        }

        File keysFile = new File(clientDir, "keys.json");
        if (!keysFile.exists()) {
            saveKeys(client);
        } else {
            client.getKeys().addAll(loadKeys(keysFile));
            System.out.println(client.getKeys().get(0));
        }

        loadHashFiles(client, clientDir);
        loadMapData(client, clientDir);

    }

    private void loadHashFiles(Client client, File clientDir) {
        File hashesDir = new File(clientDir, "hashes");
        if (!hashesDir.exists()) {
            hashesDir.mkdirs();
        }

        File[] hashFiles = hashesDir.listFiles();
        if (hashFiles != null) {
            for (File hashFile : hashFiles) {
                String type = getTypeFromFileName(hashFile);
                client.addHash(type, hashFile);
            }
            client.getHashStorage().printEntries();
        }
    }
    private void loadMapData(Client client, File clientDir) {
        File mapFile = new File(clientDir, "/maps/client.map");
        if (mapFile.exists()) {
            client.setMap(loadMap(mapFile));
        }
    }

    private List<String> loadMap(File file) {
        List<String> list = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    list.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String getTypeFromFileName(File file) {
        String fileName = file.getName().toLowerCase();
        if (fileName.contains("client")) {
            return "client";
        } else if (fileName.contains("jvm")) {
            return "jvm";
        } else if (fileName.contains("launcher")) {
            return "launcher";
        }
        return "unknown";
    }


    private void saveClientData(Client client) {
        try (FileWriter writer = new FileWriter(Constant.dbPath + "/" + client.getName() + "/client.json")) {
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addUser(Client client, User user) {
        client.addUser(user);
        saveUsers(client);
    }

    public void addKey(Client client, Key key) {
        client.addKey(key);
        saveKeys(client);
    }

    public void removeUser(Client client, User user) {
        client.getUsers().remove(user);
        saveUsers(client);
    }

    public void removeKey(Client client, Key key) {
        client.getKeys().remove(key);
        saveKeys(client);
    }

    public void saveUsers(Client client) {
        File usersFile = new File(Constant.dbPath + "/" + client.getName() + "/users.json");
        try {
            if (!usersFile.exists()) {
                usersFile.createNewFile();
            }
            try (FileWriter writer = new FileWriter(usersFile)) {
                gson.toJson(client.getUsers(), writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveKeys(Client client) {
        File keysFile = new File(Constant.dbPath + "/" + client.getName() + "/keys.json");
        try {
            if (!keysFile.exists()) {
                keysFile.createNewFile();
            }
            try (FileWriter writer = new FileWriter(keysFile)) {
                gson.toJson(client.getKeys(), writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<User> loadUsers(File file) {
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<User>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<Key> loadKeys(File file) {
        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Key>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
}
