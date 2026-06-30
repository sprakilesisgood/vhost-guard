package com.vhostguard;

import com.vhostguard.config.ModConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class VhostGuard extends JavaPlugin implements Listener, CommandExecutor {
    private static final MiniMessage MM = MiniMessage.miniMessage();
    private ModConfig config;

    @Override
    public void onEnable() {
        config = ModConfig.load(getDataFolder().toPath(), getLogger());
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("vhostguard").setExecutor(this);
        getLogger().info("Loaded. Allowed hosts: " + config.getAllowedHosts());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            config = ModConfig.load(getDataFolder().toPath(), getLogger());
            sender.sendMessage(MM.deserialize("<green>Vhost Guard reloaded. Allowed hosts: " + config.getAllowedHosts()));
            return true;
        }
        sender.sendMessage(MM.deserialize("<yellow>Vhost Guard v" + getDescription().getVersion() + ". Usage: /vhostguard reload"));
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String host = event.getHostname();
        if (config.isAllowed(host)) {
            return;
        }

        String attemptedHost = host == null || host.isBlank() ? "unknown" : host;
        getLogger().warning("Blocked " + event.getName() + " connecting via: " + attemptedHost);
        String message = config.getKickMessage().replace("%host%", attemptedHost);
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, MM.deserialize(message));
    }
}
