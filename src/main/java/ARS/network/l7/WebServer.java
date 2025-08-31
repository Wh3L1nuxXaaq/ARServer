package ARS.network.l7;

import ARS.ElyGuard;
import ARS.client.Client;
import ARS.client.User;
import ARS.fast.Constant;
import ARS.manager.ClientManager;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WebServer {
    private final ClientManager clientManager = ElyGuard.getInstance().getClient();

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", exchange -> {
            String requestURI = exchange.getRequestURI().toString();
            Map<String, String> queryParams = parseQuery(exchange.getRequestURI().getQuery());

            if (!queryParams.containsKey("hwid")) {
                sendResponse(exchange, 400, "IDI NAXUI TVARINA");
                return;
            }

            String hwid = queryParams.get("hwid");
            String[] parts = requestURI.split("/");

            if (parts.length < 3) {
                sendResponse(exchange, 400, "IDK");
                return;
            }

            String clientName = parts[1];
            String fileName = parts[2].split("\\?")[0];
            Client client = clientManager.getClients().stream()
                    .filter(c -> c.getName().equals(clientName))
                    .findFirst()
                    .orElse(null);

            if (client == null) {
                sendResponse(exchange, 404, "client not found");
                return;
            }

            List<User> users = client.getUsers();
            boolean isAuthorized = users.stream().anyMatch(user -> user.getHwid().equals(hwid));

            if (!isAuthorized) {
                sendResponse(exchange, 403, "ACCESS DENIED");
                return;
            }

            File file = new File(Constant.dbPath + File.separator + clientName + File.separator + "files" + File.separator + fileName);
            System.out.println(file.getPath());
            if (!file.exists()) {
                sendResponse(exchange, 404, "file not found");
                return;
            }

            exchange.sendResponseHeaders(200, file.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(java.nio.file.Files.readAllBytes(file.toPath()));
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("WebServer запущен на порту 8081");
    }

    private Map<String, String> parseQuery(String query) {
        if (query == null) return Map.of();
        return java.util.Arrays.stream(query.split("&"))
                .map(param -> param.split("="))
                .filter(arr -> arr.length == 2)
                .collect(Collectors.toMap(arr -> arr[0], arr -> arr[1]));
    }

    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
