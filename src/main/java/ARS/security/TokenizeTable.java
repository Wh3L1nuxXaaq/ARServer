package ARS.security;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class TokenizeTable {
    static Map<String, String> tokenTable = new HashMap<>();

    public static void addToken(ChannelHandlerContext ctx, String token) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        tokenTable.put(ip, token);
    }
    public static String getToken(ChannelHandlerContext ctx) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        return tokenTable.get(ip);
    }
    public static boolean hasToken(ChannelHandlerContext ctx) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        return tokenTable.containsKey(ip);
    }
    public static void removeToken(ChannelHandlerContext ctx) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        tokenTable.remove(ip);
    }
    public static void clearTable() {
        tokenTable.clear();
    }
    public static void printAllTokens() {
        for (Map.Entry<String, String> entry : tokenTable.entrySet()) {
            System.out.println("IP Address: " + entry.getKey() + ", Token: " + entry.getValue());
        }
    }
}
