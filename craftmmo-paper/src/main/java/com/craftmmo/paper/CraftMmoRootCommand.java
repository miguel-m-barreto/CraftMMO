package com.craftmmo.paper;

import java.util.Locale;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class CraftMmoRootCommand implements CommandExecutor {
    private final CraftMmoPlugin plugin;

    public CraftMmoRootCommand(CraftMmoPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String subcommand = args.length == 0 ? "help" : args[0].toLowerCase(Locale.ROOT);
        switch (subcommand) {
            case "help" -> {
                if (hasPermission(sender, "craftmmo.help")) {
                    sendHelp(sender);
                }
            }
            case "version" -> {
                if (hasPermission(sender, "craftmmo.version")) {
                    sender.sendMessage("CraftMMO " + plugin.getPluginMeta().getVersion());
                }
            }
            case "health" -> {
                if (hasPermission(sender, "craftmmo.health")) {
                    sendHealth(sender);
                }
            }
            default -> {
                sender.sendMessage("Unknown CraftMMO command. Use /craftmmo help.");
                return true;
            }
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("CraftMMO commands:");
        sender.sendMessage("/craftmmo help");
        sender.sendMessage("/craftmmo version");
        sender.sendMessage("/craftmmo health");
    }

    private void sendHealth(CommandSender sender) {
        PluginLifecycleState state = plugin.lifecycleState();
        sender.sendMessage("CraftMMO health: " + state);
        StartupFailure failure = plugin.startupFailure();
        if (state == PluginLifecycleState.FAILED && failure.category() != StartupFailureCategory.NONE) {
            sender.sendMessage("Startup failure category: " + failure.sanitizedMessage());
            if (sender.hasPermission("craftmmo.admin.health.details") && !failure.detail().isBlank()) {
                sender.sendMessage("Startup failure detail: " + failure.detail());
            }
        }
    }

    private static boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage("You do not have permission to use this CraftMMO command.");
        return false;
    }
}
