package ARS.network.packet.impl;

import ARS.network.packet.Packet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class S1UpdateStatus extends Packet {
    private static Gson GSON = new Gson();

    public S1UpdateStatus(String struct, boolean status, String tokenKey) {
        super(encryptJson(generateJson(struct, status), tokenKey));
    }

    private static String generateJson(String struct, boolean status) {
        JsonObject updateStatJson = new JsonObject();
        updateStatJson.addProperty("type", "NEED_UPDATE");
        updateStatJson.addProperty("struct", struct);
        updateStatJson.addProperty("value", status);

        JsonObject wrappedData = new JsonObject();
        wrappedData.addProperty("data", updateStatJson.toString());

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
