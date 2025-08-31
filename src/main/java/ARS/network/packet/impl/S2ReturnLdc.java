package ARS.network.packet.impl;

import ARS.network.packet.Packet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class S2ReturnLdc extends Packet {
    private static Gson GSON = new Gson();

    public S2ReturnLdc(String ldcReturn) {
        super(generateJson(ldcReturn));
    }

    private static String generateJson(String ldcReturn) {
        JsonObject json = new JsonObject();
        json.addProperty("data_type", "LDC");
        json.addProperty("type", "RETURN");
        json.addProperty("value", ldcReturn);

        JsonObject wrappedData = new JsonObject();
        wrappedData.addProperty("data", json.toString());

        return GSON.toJson(wrappedData);
    }

    @Override
    public String toString() {
        return getData();
    }
}
