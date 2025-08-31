package ARS.client;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Client {
    private String name;
    private boolean techWorks;
    private String version;
    private List<User> users;
    private List<Key> keys;
    private Hash hashStorage;
    private List<String> mapData;
    public Client(String name, String version) {
        this.name = name;
        this.version = version;
        this.users = new ArrayList<>();
        this.keys = new ArrayList<>();
        this.hashStorage = new Hash();
    }

    public String getName() {
        return name;
    }

    public boolean isTechWorks() {
        return techWorks;
    }

    public String getVersion() {
        return version;
    }

    public List<User> getUsers() {
        return users;
    }
    public List<String> getMapData() {
        return mapData;
    }
    public void setMap(List<String> map) {
        this.mapData = map;
    }
    public List<Key> getKeys() {
        return keys;
    }

    public Hash getHashStorage() {
        return hashStorage;
    }

    public void addUser(User user) {
        this.users.add(user);
    }

    public void addKey(Key key) {
        this.keys.add(key);
    }

    public void addHash(String type, File file) {
        this.hashStorage.addEntry(type, file);
    }

    public void removeHash(File file) {
        this.hashStorage.removeEntry(file);
    }

    public void printHashes() {
        this.hashStorage.printEntries();
    }
}
