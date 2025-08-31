package ARS.security;

import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class HmacTable {
    private static Map<ChannelHandlerContext, String> hmacTable = new HashMap<>();
    public static void addHmacKey(ChannelHandlerContext ctx, String hmacKey) {
        hmacTable.put(ctx, hmacKey);
    }
    public static String getHmacKey(ChannelHandlerContext ctx) {
        return hmacTable.get(ctx);
    }
    public static boolean hasHmacKey(ChannelHandlerContext ctx) {
        return hmacTable.containsKey(ctx);
    }
    public static void removeHmacKey(ChannelHandlerContext ctx) {
        hmacTable.remove(ctx);
    }
    public static void clearTable() {
        hmacTable.clear();
    }
    public static void printAllHmacKeys() {
        for (Map.Entry<ChannelHandlerContext, String> entry : hmacTable.entrySet()) {
            System.out.println("Channel: " + entry.getKey() + ", HMAC Key: " + entry.getValue());
        }
    }
}
