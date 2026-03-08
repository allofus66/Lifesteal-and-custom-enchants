package me.sharklifesteal.commands;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class SharkLifestealCommand implements CommandExecutor, TabCompleter {
    private final SharkLifesteal plugin;

    public SharkLifestealCommand(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sharklifesteal.admin")) {
            MessageUtil.send(sender, "no_permission");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage("§6/sharklifesteal reload|give|list");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadEverything();
                MessageUtil.send(sender, "reload_success");
            }
            case "list" -> plugin.getEnchantManager().getAll().forEach(enchant ->
                    sender.sendMessage("§e" + enchant.getKey() + " §7max:" + enchant.getMaxLevel()));
            case "give" -> {
                if (args.length < 3) {
                    sender.sendMessage("§c/sharklifesteal give <player> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    MessageUtil.send(sender, "player_not_found");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[2]);
                } catch (NumberFormatException ex) {
                    sender.sendMessage("§cAmount must be a number.");
                    return true;
                }
                target.getInventory().addItem(plugin.getWithdrawManager().buildHeartCrystal(amount));
                sender.sendMessage("§aGiven Heart Crystal to " + target.getName());
            }
            default -> sender.sendMessage("§6/sharklifesteal reload|give|list");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "give", "list");
        }
        return List.of();
    }
}
