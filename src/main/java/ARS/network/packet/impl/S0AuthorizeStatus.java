package ARS.network.packet.impl;

import ARS.network.packet.Packet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class S0AuthorizeStatus extends Packet {
    private static Gson GSON = new Gson();

    public S0AuthorizeStatus(String tokenKey) {
        super(encryptJson(generateJson(), tokenKey));
    }

    private static String generateJson() {
        JsonObject statusJson = new JsonObject();
        statusJson.addProperty("type", "USE_KEY");
        statusJson.addProperty("status", "SUCCESS");

        JsonObject wrappedData = new JsonObject();
        wrappedData.addProperty("data", statusJson.toString());

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
