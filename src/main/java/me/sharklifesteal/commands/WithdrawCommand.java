package me.sharklifesteal.commands;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.managers.WithdrawManager;
import me.sharklifesteal.utils.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WithdrawCommand implements CommandExecutor {
    private final SharkLifesteal plugin;

    public WithdrawCommand(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        if (!player.hasPermission("sharklifesteal.withdraw")) {
            MessageUtil.send(player, "no_permission");
            return true;
        }
        if (args.length != 1) {
            MessageUtil.send(player, "withdraw_usage");
            return true;
        }

        int hearts;
        try {
            hearts = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            MessageUtil.send(player, "withdraw_usage");
            return true;
        }

        WithdrawManager.Result result = plugin.getWithdrawManager().withdraw(player, hearts);
        switch (result) {
            case SUCCESS -> MessageUtil.send(player, "withdraw_success", "{amount}", String.valueOf(hearts));
            case COOLDOWN -> MessageUtil.send(player, "withdraw_cooldown");
            case DAILY_LIMIT -> MessageUtil.send(player, "withdraw_daily_limit");
            case NOT_ENOUGH -> MessageUtil.send(player, "withdraw_not_enough");
            case MINIMUM_HEALTH -> MessageUtil.send(player, "withdraw_min_health");
            case DISABLED -> MessageUtil.send(player, "withdraw_disabled");
            default -> MessageUtil.send(player, "withdraw_invalid");
        }
        return true;
    }
}
