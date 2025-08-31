package ARS.network.packet;

import com.google.gson.Gson;

public class Packet {
    private static final Gson GSON = new Gson();
    private final String data;

    public Packet(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return GSON.toJson(this);
    }

    public byte[] toBytes() {
        String json = toString();
        return json.getBytes();
    }
}
