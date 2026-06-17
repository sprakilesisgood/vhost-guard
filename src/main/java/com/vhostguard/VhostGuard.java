package com.vhostguard;

import com.vhostguard.config.ModConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Paper entry point for Vhost Guard.
 * Loads the JSON config and rejects logins that did not use an allowed hostname.
 */
public final class VhostGuard extends JavaPlugin implements Listener {
    private ModConfig config;

    @Override
    public void onEnable() {
        config = ModConfig.load(getDataFolder().toPath(), getLogger());
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Vhost Guard loaded. Allowed hosts: " + config.getAllowedHosts());
    }

    /**
     * Paper exposes the hostname from the client's handshake during async pre-login.
     * Status pings do not fire this event, so the server list remains unaffected.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String host = event.getHostname();
        if (config.isAllowed(host)) {
            return;
        }

        String attemptedHost = host == null || host.isBlank() ? "unknown" : host;
        String message = config.getKickMessage().replace("%host%", attemptedHost);
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Component.text(message));
    }
}