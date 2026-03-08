package me.sharklifesteal.commands;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.enchants.CustomEnchant;
import me.sharklifesteal.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnchantCommand implements CommandExecutor, TabCompleter {
    private final SharkLifesteal plugin;

    public EnchantCommand(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("sharklifesteal.enchant")) {
            MessageUtil.send(player, "no_permission");
            return true;
        }
        if (args.length < 1) {
            MessageUtil.send(player, "enchant_usage");
            return true;
        }

        plugin.getEnchantManager().byId(args[0]).ifPresentOrElse(enchant -> {
            int level = args.length >= 2 ? parseLevel(args[1]) : 1;
            if (!canPay(player, enchant)) {
                MessageUtil.send(player, "enchant_cannot_pay");
                return;
            }
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (plugin.getEnchantManager().applyEnchant(hand, enchant, level)) {
                takeCost(player, enchant);
                MessageUtil.send(player, "enchant_applied", "{enchant}", enchant.getDisplayName(), "{level}", String.valueOf(level));
            } else {
                MessageUtil.send(player, "enchant_invalid_item");
            }
        }, () -> MessageUtil.send(player, "enchant_not_found"));

        return true;
    }

    private int parseLevel(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private boolean canPay(Player player, CustomEnchant enchant) {
        return switch (enchant.getCostType()) {
            case XP -> player.getLevel() >= enchant.getCostAmount();
            case ITEM -> player.getInventory().containsAtLeast(new ItemStack(enchant.getCostMaterial()), enchant.getCostAmount());
        };
    }

    private void takeCost(Player player, CustomEnchant enchant) {
        switch (enchant.getCostType()) {
            case XP -> player.setLevel(Math.max(0, player.getLevel() - enchant.getCostAmount()));
            case ITEM -> player.getInventory().removeItem(new ItemStack(enchant.getCostMaterial(), enchant.getCostAmount()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return plugin.getEnchantManager().getAll().stream().map(CustomEnchant::getKey).toList();
        }
        if (args.length == 2) {
            return List.of("1", "2", "3", "4", "5");
        }
        return new ArrayList<>();
    }
}
