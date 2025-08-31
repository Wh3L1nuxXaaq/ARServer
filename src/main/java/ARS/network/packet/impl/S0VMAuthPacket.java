package ARS.network.packet.impl;

import ARS.network.packet.Packet;

public class S0VMAuthPacket extends Packet {
    public S0VMAuthPacket(String status) {
        super(status);
    }

    @Override
    public String toString() {
        return getData();
    }
}
