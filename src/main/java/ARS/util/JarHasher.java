package ARS.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class JarHasher {

    public static void main(String[] args) {
        try {
            String jarPath = "C:/piska/client.jar";
            String hmacKey = "3lyguard_resolvator";

            File jarFile = new File(jarPath);
            if (!jarFile.exists()) {
                System.out.println("JAR file does not exist at: " + jarPath);
                return;
            }

            byte[] jarData = Files.readAllBytes(jarFile.toPath());

            String jarHash = computeHMACSHA256(jarData, hmacKey);

            System.out.println("Хеш JAR-файла: " + jarHash);

            JsonObject jsonOutput = new JsonObject();
            jsonOutput.addProperty("jar_hash", jarHash);

            try (FileWriter writer = new FileWriter("G:/ElyGuard21/hashes.json")) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(jsonOutput, writer);
                System.out.println("Hashes saved to G:/ElyGuard21/hashes.json");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String computeHMACSHA256(byte[] data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);

        byte[] hashBytes = sha256_HMAC.doFinal(data);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
