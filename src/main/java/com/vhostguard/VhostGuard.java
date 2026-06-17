package com.vhostguard;

import com.vhostguard.config.ModConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server-side entry point for Vhost Guard.
 * Nothing fancy here: load the config and let the mixin do the kicking.
 */
public class VhostGuard implements DedicatedServerModInitializer {
    public static final String MOD_ID = "vhost-guard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ModConfig config;

    @Override
    public void onInitializeServer() {
        config = ModConfig.load();
        LOGGER.info("Vhost Guard loaded. Allowed hosts: {}", config.getAllowedHosts());
    }

    /**
     * Lazy fallback: if the mixin somehow fires before init, load config on demand.
     * In practice this should not happen, but it costs nothing to be safe.
     */
    public static ModConfig getConfig() {
        if (config == null) {
            config = ModConfig.load();
        }
        return config;
    }
}
