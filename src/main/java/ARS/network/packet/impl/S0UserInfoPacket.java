package ARS.network.packet.impl;

import ARS.client.User;
import ARS.network.packet.Packet;
import ARS.security.TokenizeTable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class S0UserInfoPacket extends Packet {
    private static Gson GSON = new Gson();

    public S0UserInfoPacket(User user, String tokenKey) {
        super(encryptJson(generateJson(user), tokenKey));
        System.out.println(encryptJson(generateJson(user), tokenKey));
    }

    private static String generateJson(User user) {
        JsonObject userJson = new JsonObject();
        userJson.addProperty("type", "USER_INFO");
        userJson.addProperty("name", user.getName());
        userJson.addProperty("uid", user.getUid());
        userJson.addProperty("hwid", user.getHwid());
        userJson.addProperty("role", user.getRole().toString());
        userJson.addProperty("subTime", user.getSubTime());
        userJson.addProperty("path", user.getPath());
        userJson.addProperty("memory", user.getMemory());

        JsonObject wrappedData = new JsonObject();
        wrappedData.addProperty("data", userJson.toString());

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
