package ARS.network.packet;

import ARS.ElyGuard;
import ARS.client.Client;
import ARS.client.Key;
import ARS.client.Role;
import ARS.fast.Constant;
import ARS.manager.ClientManager;
import ARS.manager.DatabaseManager;
import ARS.client.User;
import ARS.network.Server;
import ARS.network.packet.impl.*;
import ARS.resolve.ResolveLdc;
import ARS.security.AuthorizeTable;
import ARS.security.HmacTable;
import ARS.security.TokenizeTable;
import ARS.util.TimeUtil;
import com.google.gson.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class BaseListener extends ChannelInboundHandlerAdapter {
    private final ChannelGroup channels;
    private final ClientManager clientManager = ElyGuard.getInstance().getClient();
    private final DatabaseManager databaseManager = ElyGuard.getInstance().getDatabase();

    public BaseListener(ChannelGroup channels) {
        this.channels = channels;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        channels.add(ctx.channel());
        System.out.println("Client connected: " + ctx.channel().remoteAddress());
        Server.getInstance().sendPacket(new S0TokenPacket(ctx), ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channels.remove(ctx.channel());
        System.out.println("Client disconnected: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = msg.toString();
        System.out.println("Received message: " + message);

        if (message.startsWith("_VMAUTH")) {
            String[] parts = message.split(":");
            if (parts.length < 3) {
                System.out.println("Некорректный формат _VMAUTH");
                return;
            }
            String token = TokenizeTable.getToken(ctx);
            String clientName = parts[1];
            String hwid = parts[2];

            System.out.println("VMAUTH запрос получен.");
            System.out.println("Название клиента: " + clientName);
            System.out.println("HWID: " + hwid);

            Client client = null;
            for (Client c : clientManager.getClients()) {
                if (c.getName().equals(clientName)) {
                    client = c;
                    break;
                }
            }

            if (client != null) {
                boolean userFound = false;
                for (User user : client.getUsers()) {
                    if (user.getHwid().equals(hwid)) {
                        userFound = true;
                        System.out.println("Пользователь с HWID " + hwid + " найден: " + user.toString());

                        if (TimeUtil.isExpired(user.getSubTime())) {
                            System.out.println("Подписка просрочена для пользователя: " + user.getName());
                            databaseManager.removeUser(client, user);
                            Server.getInstance().sendPacket(new S0VMAuthPacket("SUB:FAIL"), ctx);  // SUB:FAIL
                        } else {
                            System.out.println("Подписка валидна для пользователя: " + user.getName());
                            Server.getInstance().sendPacket(new S0VMAuthPacket("SUB:OK"), ctx);  // SUB:OK
                        }
                        break;
                    }
                }

                if (!userFound) {
                    System.out.println("Пользователь с HWID " + hwid + " не найден.");
                    Server.getInstance().sendPacket(new S0VMAuthPacket("SUB:FAIL"), ctx);  // SUB:FAIL
                }
            } else {
                System.out.println("Клиент с именем " + clientName + " не найден.");
                Server.getInstance().sendPacket(new S0VMAuthPacket("SUB:FAIL"), ctx);  // SUB:FAIL
            }
            return;
        }



        JsonObject jsonObject = JsonParser.parseString(message).getAsJsonObject();
        String data = jsonObject.get("data").getAsString();

        JsonObject innerJson = JsonParser.parseString(data).getAsJsonObject();

        if (!innerJson.has("hmac")) {
            System.out.println("No HMAC found. Closing connection.");
            ctx.close();
            return;
        }

        String providedHmac = innerJson.get("hmac").getAsString();

        innerJson.remove("hmac");

        String computedData = innerJson.toString();

        String computedHmac;
        try {
            String hmacKey = HmacTable.getHmacKey(ctx);
            if (hmacKey == null) {
                System.out.println("No HMAC key found for this client. Closing connection.");
                ctx.close();
                return;
            }

            computedHmac = calculateHMAC(computedData, hmacKey);
        } catch (Exception e) {
            System.out.println("HMAC calculation failed: " + e.getMessage());
            ctx.close();
            return;
        }

        if (!providedHmac.equalsIgnoreCase(computedHmac)) {
            System.out.println("Invalid HMAC. Closing connection.");
            ctx.close();
            return;
        }

        if (!innerJson.has("token")) {
            System.out.println("No token found. Closing connection.");
            ctx.close();
            return;
        }

        String token = innerJson.get("token").getAsString();

        if (!TokenizeTable.hasToken(ctx) || !TokenizeTable.getToken(ctx).equals(token)) {
            System.out.println("Invalid token. Closing connection.");
            ctx.close();
            return;
        }

        String type = innerJson.get("type").getAsString();

        if ("AUTHORIZE".equals(type)) {
            String clientName = innerJson.get("client").getAsString();
            String hwid = innerJson.get("hwid").getAsString();

            System.out.println("Authorization request received.");
            System.out.println("Client Name: " + clientName);
            System.out.println("HWID: " + hwid);

            Client client = null;
            for (Client c : clientManager.getClients()) {
                if (c.getName().equals(clientName)) {
                    client = c;
                    break;
                }
            }

            if (client != null) {
                boolean userFound = false;
                for (User user : client.getUsers()) {
                    if (user.getHwid().equals(hwid)) {
                        userFound = true;
                        System.out.println("User with HWID " + hwid + " found: " + user.toString());

                        if (TimeUtil.isExpired(user.getSubTime())) {
                            System.out.println("Subscription expired for user: " + user.getName());
                            databaseManager.removeUser(client, user);
                        } else {
                            System.out.println("Subscription valid for user: " + user.getName());
                            String tokenKey = TokenizeTable.getToken(ctx);
                            Server.getInstance().sendPacket(new S0UserInfoPacket(user, tokenKey), ctx);
                        }
                        break;
                    }
                }

                if (!userFound) {
                    System.out.println("No user found with HWID: " + hwid);
                    AuthorizeTable.addAuthorizeTime(ctx, 30);
                    Server.getInstance().sendPacket(new S0RequestAuthorize(token), ctx);
                }
            } else {
                System.out.println("No client found with name: " + clientName);
            }
        } else if ("MEMORY".equals(type)) {
            String clientName = innerJson.get("client").getAsString();
            float memory = innerJson.get("value").getAsFloat();
            String hwid = innerJson.get("hwid").getAsString();

            Client client = clientManager.getClients().stream()
                    .filter(c -> c.getName().equals(clientName))
                    .findFirst()
                    .orElse(null);

            if (client != null) {
                for (User user : client.getUsers()) {
                    if (user.getHwid().equals(hwid)) {
                        user.setMemory(memory);
                        System.out.println("Updated memory for user " + user.getName() + " to " + memory + "MB");
                        Server.getInstance().sendPacket(new S0UpdateMemory(memory, token), ctx);
                        databaseManager.saveUsers(client);
                        break;
                    }
                }
            }
        } else if ("PATH".equals(type)) {
            String clientName = innerJson.get("client").getAsString();
            String path = innerJson.get("value").getAsString();
            String hwid = innerJson.get("hwid").getAsString();

            Client client = clientManager.getClients().stream()
                    .filter(c -> c.getName().equals(clientName))
                    .findFirst()
                    .orElse(null);

            if (client != null) {
                for (User user : client.getUsers()) {
                    if (user.getHwid().equals(hwid)) {
                        user.setPath(path);
                        System.out.println("Updated path for user " + user.getName() + " to " + path);
                        Server.getInstance().sendPacket(new S0UpdatePath(path, token), ctx);
                        databaseManager.saveUsers(client);
                        break;
                    }
                }
            }
        } else if ("USE_KEY".equals(type)) {
            if (!AuthorizeTable.hasAuthorizeTime(ctx)) {
                System.out.println("No authorization for IP: " + ctx.channel().remoteAddress());
                ctx.close();
                return;
            }
            String clientName = innerJson.get("client").getAsString();
            String hwid = innerJson.get("hwid").getAsString();
            String keyValue = innerJson.get("key").getAsString();

            System.out.println("Key request received.");
            System.out.println("Client Name: " + clientName);
            System.out.println("HWID: " + hwid);
            System.out.println("Key: " + keyValue);

            Client client = null;
            for (Client c : clientManager.getClients()) {
                if (c.getName().equals(clientName)) {
                    client = c;
                    break;
                }
            }

            if (client == null) {
                System.out.println("Client not found!");
                return;
            }

            Key key = null;
            for (Key k : client.getKeys()) {
                if (k.getKey().equals(keyValue)) {
                    key = k;
                    break;
                }
            }

            if (key == null) {
                System.out.println("Key not found!");
                return;
            }

            String generatedName = clientName + "_" + getRandomString(6);

            int uid = client.getUsers().size() + 1;
            String subTime = key.getSubTime();

            Role role = key.getRole();

            User newUser = new User(generatedName, uid, hwid, role, subTime, "C:/" + clientName, 1024.0f);
            client.addUser(newUser);
            AuthorizeTable.removeAuthorizeTime(ctx);

            databaseManager.removeKey(client, key);

            databaseManager.saveUsers(client);
            databaseManager.saveKeys(client);
            Server.getInstance().sendPacket(new S0AuthorizeStatus(token), ctx);

        } else if ("GET_STRING".equals(type)) {
            String clientName = innerJson.get("client").getAsString();
            String hash = innerJson.get("hash").getAsString();
            String data_type = innerJson.get("data_type").getAsString();
            int index = innerJson.get("index").getAsInt();

            if (data_type.equals("LDC")) {
                ResolveLdc.getLdcFromMap(token, ctx, clientManager, clientName, hash, index);
            }
        }
        else if ("VALIDATE".equals(type)) {
            String struct = innerJson.get("struct").getAsString();
            String clientName = innerJson.get("client").getAsString();

            JsonElement dataElement = jsonObject.get("data");
            JsonObject innerJsonParsed;
            if (dataElement.isJsonObject()) {
                innerJsonParsed = dataElement.getAsJsonObject();
            } else if (dataElement.isJsonPrimitive() && dataElement.getAsJsonPrimitive().isString()) {
                try {
                    innerJsonParsed = JsonParser.parseString(dataElement.getAsString()).getAsJsonObject();
                } catch (Exception e) {
                    System.out.println("Ошибка парсинга data: " + e.getMessage());
                    return;
                }
            } else {
                System.out.println("Поле data не содержит ожидаемый JSON объект.");
                return;
            }

            JsonObject container = innerJsonParsed.getAsJsonObject("container");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJson = gson.toJson(container);

            Client client = null;
            for (Client c : clientManager.getClients()) {
                if (c.getName().equals(clientName)) {
                    client = c;
                    break;
                }
            }
            if (client == null) {
                System.out.println("Клиент не найден: " + clientName);
                return;
            }

            if ("client".equals(struct)) {
                File clientFile = new File(Constant.dbPath + "/" + client.getName() + "/hashes/client.json");
                if (clientFile.exists()) {
                    try {
                        String fileContent = new String(Files.readAllBytes(clientFile.toPath()), StandardCharsets.UTF_8);
                        if (prettyJson.equals(fileContent)) {
                            System.out.println("JSON для client совпадают!");
                            Server.getInstance().sendPacket(new S1UpdateStatus("client", false, token), ctx);
                        } else {
                            System.out.println("JSON для client не совпадают.");
                            Server.getInstance().sendPacket(new S1UpdateStatus("client", true, token), ctx);
                            System.out.println(fileContent);
                            System.out.println(prettyJson);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("client.json не найден.");
                }
            } else if ("jvm".equals(struct)) {
                File jvmFile = new File(Constant.dbPath + "/" + client.getName() + "/hashes/jvm.json");
                if (jvmFile.exists()) {
                    try {
                        String fileContent = new String(Files.readAllBytes(jvmFile.toPath()), StandardCharsets.UTF_8);

                        JsonObject fileJson = gson.fromJson(fileContent, JsonObject.class);
                        String fileDllHash = fileJson.has("dll_hash") ? fileJson.get("dll_hash").getAsString() : null;

                        JsonObject prettyJsonObject = gson.fromJson(prettyJson, JsonObject.class);
                        String prettyJsonDllHash = prettyJsonObject.has("dll_hash") ? prettyJsonObject.get("dll_hash").getAsString() : null;

                        if (fileDllHash != null && prettyJsonDllHash != null && fileDllHash.equals(prettyJsonDllHash)) {
                            System.out.println("JSON для jvm совпадают!");
                            Server.getInstance().sendPacket(new S1UpdateStatus("jvm", false, token), ctx);
                        } else {
                            System.out.println("JSON для jvm не совпадают.");
                            Server.getInstance().sendPacket(new S1UpdateStatus("jvm", true, token), ctx);
                            System.out.println(fileContent);
                            System.out.println(prettyJson);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("jvm.json не найден.");
                }
            }
        }
    }
    private String getRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * characters.length());
            randomString.append(characters.charAt(randomIndex));
        }
        return randomString.toString();
    }

    private static String calculateHMAC(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hmacBytes);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
