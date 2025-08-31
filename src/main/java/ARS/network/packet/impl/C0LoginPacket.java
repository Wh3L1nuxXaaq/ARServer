package ARS.network.packet.impl;

import ARS.network.packet.Packet;
import com.google.gson.Gson;

public class C0LoginPacket extends Packet {
    private static Gson GSON = new Gson();
    public C0LoginPacket(String data) {
        super(data);
    }
    @Override
    public String toString() {
        return GSON.toJson(this);
    }
}
