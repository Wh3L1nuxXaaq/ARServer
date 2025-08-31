package ARS.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class DllHasher {

    public static void main(String[] args) {
        try {
            String dllPath = "C:/piska/jvm/bin/server/jvm.dll";
            String hmacKey = "3lyguard_resolvator";

            File dllFile = new File(dllPath);
            if (!dllFile.exists()) {
                System.out.println("DLL file does not exist at: " + dllPath);
                return;
            }

            byte[] dllData = Files.readAllBytes(dllFile.toPath());
            String dllHash = computeHMACSHA256(dllData, hmacKey);

            System.out.println("Хеш DLL-файла: " + dllHash);

            JsonObject jsonOutput = new JsonObject();
            jsonOutput.addProperty("dll_hash", dllHash);

            try (FileWriter writer = new FileWriter("D:/ElyGuard21/hashes.json")) {
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
