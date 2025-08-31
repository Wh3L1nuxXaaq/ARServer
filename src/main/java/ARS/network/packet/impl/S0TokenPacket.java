package ARS.network.packet.impl;

import ARS.network.packet.Packet;
import ARS.security.TokenizeTable;
import ARS.security.HmacTable;
import ARS.util.TokenUtil;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;

import java.util.HashMap;
import java.util.Map;

public class S0TokenPacket extends Packet {
    private static Gson GSON = new Gson();

    public S0TokenPacket(ChannelHandlerContext ctx) {
        super(generateJson(ctx));
    }

    private static String generateJson(ChannelHandlerContext ctx) {
        String token = TokenUtil.generateToken();

        String hmacKey = TokenUtil.generateToken();

        TokenizeTable.addToken(ctx, token);
        HmacTable.addHmacKey(ctx, hmacKey);

        Map<String, String> data = new HashMap<>();
        data.put("type", "TOKEN");
        data.put("token", token);
        data.put("hmac_key", hmacKey);

        System.out.println(hmacKey);
        Map<String, String> wrappedData = new HashMap<>();
        wrappedData.put("data", GSON.toJson(data));

        return GSON.toJson(wrappedData);
    }

    @Override
    public String toString() {
        return getData();
    }
}
