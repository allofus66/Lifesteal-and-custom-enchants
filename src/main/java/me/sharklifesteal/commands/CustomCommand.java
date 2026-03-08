package me.sharklifesteal.commands;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CustomCommand implements CommandExecutor {
    private final SharkLifesteal plugin;

    public CustomCommand(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("sharklifesteal.custom")) {
            MessageUtil.send(player, "no_permission");
            return true;
        }
        if (!plugin.getConfig().getBoolean("enchants.GUI_enabled", true)) {
            MessageUtil.send(player, "gui_disabled");
            return true;
        }
        plugin.getCustomEnchantGUI().open(player);
        return true;
    }
}
