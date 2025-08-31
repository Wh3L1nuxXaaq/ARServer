package ARS.network.packet;

public interface PacketListener {
    void send(Packet packet);
    void receive(Packet packet);
}
