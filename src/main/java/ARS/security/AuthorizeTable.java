package ARS.security;

import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class AuthorizeTable {
    private static Map<String, Integer> authorizeTable = new HashMap<>();
    public static void addAuthorizeTime(ChannelHandlerContext ctx, int time) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        authorizeTable.put(ip, time);
    }
    public static void addAuthorizeTimeByIp(String ip, int time) {
        authorizeTable.put(ip, time);
    }
    public static Integer getAuthorizeTime(ChannelHandlerContext ctx) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        return authorizeTable.get(ip);
    }
    public static Integer getAuthorizeTimeByIp(String ip) {
        return authorizeTable.get(ip);
    }
    public static boolean hasAuthorizeTime(ChannelHandlerContext ctx) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        return authorizeTable.containsKey(ip);
    }
    public static void removeAuthorizeTime(ChannelHandlerContext ctx) {
        String ip = ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
        authorizeTable.remove(ip);
    }
    public static void removeAuthorizeTimeByIp(String ip) {
        authorizeTable.remove(ip);
    }
    public static void clearTable() {
        authorizeTable.clear();
    }
    public static void printAllAuthorizeTimes() {
        for (Map.Entry<String, Integer> entry : authorizeTable.entrySet()) {
            System.out.println("IP Address: " + entry.getKey() + ", Time: " + entry.getValue());
        }
    }
    public static Map<String, Integer> getAuthorizeTable() {
        return authorizeTable;
    }
}
