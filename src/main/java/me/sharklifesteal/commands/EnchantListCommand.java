package me.sharklifesteal.commands;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.enchants.CustomEnchant;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnchantListCommand implements CommandExecutor {
    private final SharkLifesteal plugin;

    public EnchantListCommand(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§6Available Custom Enchants:");
        for (CustomEnchant enchant : plugin.getEnchantManager().getAll()) {
            sender.sendMessage("§e- " + enchant.getDisplayName() + " §7(" + enchant.getKey() + ") max " + enchant.getMaxLevel());
        }
        return true;
    }
}
