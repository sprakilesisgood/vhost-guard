package com.vhostguard.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.vhostguard.VhostGuard;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Simple JSON config for allowed hostnames and the kick message.
 * Gson handles serialization; we just validate and normalize after loading.
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("vhost-guard.json");

    // Default example host. Replace it with your actual domain.
    private List<String> allowedHosts = new ArrayList<>(List.of("mc.example.com"));
    private String kickMessage = "Connect via the allowed domain. (You tried: %host%)";

    public static ModConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            // No config yet? Create one with the defaults so the user can edit it.
            ModConfig config = new ModConfig();
            config.save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            ModConfig config = GSON.fromJson(reader, ModConfig.class);
            if (config == null) {
                // Empty file fallback. Better defaults than a crash.
                config = new ModConfig();
            }
            config.validate();
            config.save();
            return config;
        } catch (IOException | JsonSyntaxException e) {
            VhostGuard.LOGGER.error("Failed to load Vhost Guard config, using defaults.", e);
            return new ModConfig();
        }
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            VhostGuard.LOGGER.error("Failed to save Vhost Guard config.", e);
        }
    }

    /**
     * Normalize hostnames to lowercase and make sure the kick message is never empty.
     */
    public void validate() {
        if (allowedHosts == null) {
            allowedHosts = new ArrayList<>();
        }

        List<String> normalized = new ArrayList<>();
        for (String host : allowedHosts) {
            if (host != null && !host.isBlank()) {
                normalized.add(host.toLowerCase(Locale.ROOT));
            }
        }
        allowedHosts = normalized;

        if (kickMessage == null || kickMessage.isBlank()) {
            kickMessage = "Connect via the allowed domain. (You tried: %host%)";
        }
    }

    public List<String> getAllowedHosts() {
        return List.copyOf(allowedHosts);
    }

    public String getKickMessage() {
        return kickMessage;
    }

    /**
     * Check if a host is on the allow-list. Supports exact matches, *.subdomain wildcards,
     * and a bare '*' wildcard that lets everything through (mostly for testing).
     */
    public boolean isAllowed(String host) {
        if (host == null) {
            return false;
        }

        String normalized = host.toLowerCase(Locale.ROOT);
        if (allowedHosts.contains(normalized) || allowedHosts.contains("*")) {
            return true;
        }

        for (String allowed : allowedHosts) {
            if (allowed.startsWith("*.")) {
                // "*.example.com" should match "play.example.com" but not "example.com" itself.
                String suffix = allowed.substring(1); // .example.com
                if (normalized.endsWith(suffix)) {
                    return true;
                }
            }
        }

        return false;
    }
}
