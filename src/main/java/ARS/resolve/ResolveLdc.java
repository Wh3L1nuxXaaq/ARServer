package ARS.resolve;

import ARS.client.Client;
import ARS.manager.ClientManager;
import ARS.network.Server;
import ARS.network.packet.impl.S2ReturnLdc;
import ARS.util.AESUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Map;

public class ResolveLdc {
    public static void getLdcFromMap(String token, ChannelHandlerContext ctx, ClientManager clientManager, String clientName, String hash, int index) {
        Client client = clientManager.getClients().stream()
                .filter(c -> c.getName().equals(clientName))
                .findFirst()
                .orElse(null);

        if (client == null) {
            System.out.println("Клиент " + clientName + " не найден.");
            return;
        }

        List<String> mapData = client.getMapData();
        for (String entry : mapData) {
            String[] parts = entry.split(":");
            if (parts.length == 4) {
                // parts[0] - название, parts[1] - индекс, parts[2] - хэш, parts[3] - ключ
                int entryIndex;
                try {
                    entryIndex = Integer.parseInt(parts[1]);
                } catch (NumberFormatException e) {
                    continue;
                }
                String entryHash = parts[2];
                String cryptoKey = parts[3];
                String decryptedHash = AESUtil.aesDecrypt(entryHash, cryptoKey);
                if (entryIndex == index && entryHash.equals(hash)) {
                    System.out.println("Найдено соответствие: индекс " + index + ", хэш " + hash + ", ключ " + cryptoKey);
                    System.out.println("decrypted: " + decryptedHash);
                    Server.getInstance().sendPacket(new S2ReturnLdc(decryptedHash), ctx);
                    return;
                }
            }
        }

        System.out.println("Совпадений для индекса " + index + " и хэша " + hash + " не найдено.");
    }

}
