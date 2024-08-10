package com.custommod.tacfactionmod.data.wrapper;

import com.custommod.tacfactionmod.TacFactionClaim;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class ClaimStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveClaimsToFile(Map<String, TacFactionClaim.ClaimData> claims, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(claims, writer);
        } catch (IOException e) {
            TacFactionClaim.LOGGER.error("Failed to save claims", e);
        }
    }

    public static void loadClaimsFromFile(File file) {
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                Type type = new TypeToken<Map<String, TacFactionClaim.ClaimData>>() {}.getType();
                Map<String, TacFactionClaim.ClaimData> loadedClaims = GSON.fromJson(reader, type);
                TacFactionClaim.activeClaims.clear();
                TacFactionClaim.activeClaims.putAll(loadedClaims);
            } catch (IOException e) {
                TacFactionClaim.LOGGER.error("Failed to load claims", e);
            }
        }
    }
}
