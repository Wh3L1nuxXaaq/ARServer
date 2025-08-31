package ARS.network.packet.impl;

import ARS.network.packet.Packet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class S0UpdatePath extends Packet {
    private static Gson GSON = new Gson();

    public S0UpdatePath(String path, String tokenKey) {
        super(encryptJson(generateJson(path), tokenKey));
    }

    private static String generateJson(String path) {
        JsonObject memoryJson = new JsonObject();
        memoryJson.addProperty("type", "PATH");
        memoryJson.addProperty("value", path);

        JsonObject wrappedData = new JsonObject();
        wrappedData.addProperty("data", memoryJson.toString());

        return GSON.toJson(wrappedData);
    }

    private static String encryptJson(String json, String key) {
        byte[] jsonBytes = json.getBytes();
        byte[] keyBytes = key.getBytes();
        byte[] encrypted = new byte[jsonBytes.length];

        for (int i = 0; i < jsonBytes.length; i++) {
            encrypted[i] = (byte) (jsonBytes[i] ^ keyBytes[i % keyBytes.length]);
        }

        return new String(encrypted);
    }

    @Override
    public String toString() {
        return getData();
    }
}
