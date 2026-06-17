package com.vhostguard.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON config for allowed hostnames and the kick message.
 * Gson handles serialization; validation and normalization happen after loading.
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE_NAME = "vhost-guard.json";

    // Default example host. Replace it with your actual domain.
    private List<String> allowedHosts = new ArrayList<>(List.of("mc.example.com"));
    private String kickMessage = "Connect via the allowed domain. (You tried: %host%)";

    public static ModConfig load(Path dataDirectory, Logger logger) {
        Path configPath = dataDirectory.resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configPath)) {
            ModConfig config = new ModConfig();
            config.validate();
            config.save(configPath, logger);
            return config;
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            ModConfig config = GSON.fromJson(reader, ModConfig.class);
            if (config == null) {
                config = new ModConfig();
            }
            config.validate();
            config.save(configPath, logger);
            return config;
        } catch (IOException | JsonSyntaxException e) {
            logger.log(Level.SEVERE, "Failed to load Vhost Guard config, using defaults.", e);
            ModConfig config = new ModConfig();
            config.validate();
            return config;
        }
    }

    private void save(Path configPath, Logger logger) {
        try {
            Files.createDirectories(configPath.getParent());
            try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save Vhost Guard config.", e);
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
            String normalizedHost = normalizeHost(host);
            if (normalizedHost != null) {
                normalized.add(normalizedHost);
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
        String normalized = normalizeHost(host);
        if (normalized == null) {
            return false;
        }

        if (allowedHosts.contains(normalized) || allowedHosts.contains("*")) {
            return true;
        }

        for (String allowed : allowedHosts) {
            if (allowed.startsWith("*.")) {
                // "*.example.com" matches "play.example.com" but not "example.com" itself.
                String suffix = allowed.substring(1); // .example.com
                if (normalized.endsWith(suffix)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static String normalizeHost(String host) {
        if (host == null || host.isBlank()) {
            return null;
        }

        String normalized = host.trim().toLowerCase(Locale.ROOT);

        // Some proxies/clients append extra handshake data after a NUL byte.
        int nullByteIndex = normalized.indexOf('\0');
        if (nullByteIndex >= 0) {
            normalized = normalized.substring(0, nullByteIndex);
        }

        // Strip bracketed IPv6 addresses and simple host:port suffixes.
        if (normalized.startsWith("[")) {
            int closingBracket = normalized.indexOf(']');
            if (closingBracket > 1) {
                normalized = normalized.substring(1, closingBracket);
            }
        } else {
            int lastColon = normalized.lastIndexOf(':');
            if (lastColon > -1 && normalized.indexOf(':') == lastColon && isPort(normalized.substring(lastColon + 1))) {
                normalized = normalized.substring(0, lastColon);
            }
        }

        if (normalized.endsWith(".")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized.isBlank() ? null : normalized;
    }

    private static boolean isPort(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
