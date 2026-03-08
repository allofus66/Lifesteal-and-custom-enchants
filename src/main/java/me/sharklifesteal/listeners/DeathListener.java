package me.sharklifesteal.listeners;

import me.sharklifesteal.SharkLifesteal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {
    private final SharkLifesteal plugin;

    public DeathListener(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player dead = event.getEntity();
        List<ItemStack> toKeep = new ArrayList<>();
        event.getDrops().removeIf(item -> {
            boolean soulbound = plugin.getEnchantManager().hasEnchant(item, "soulbound");
            if (soulbound) {
                toKeep.add(item.clone());
            }
            return soulbound;
        });
        toKeep.forEach(item -> dead.getInventory().addItem(item));

        Player killer = dead.getKiller();
        if (killer != null && killer.hasPermission("sharklifesteal.lifesteal")) {
            int rewardHearts = plugin.getConfig().getInt("lifesteal.kill_reward_hearts", 2);
            plugin.getWithdrawManager().addBonusHearts(killer.getUniqueId(), rewardHearts);
        }
    }

    @EventHandler
    public void onItemBreak(PlayerItemBreakEvent event) {
        ItemStack broken = event.getBrokenItem();
        if (broken.getItemMeta() != null) {
            broken.getItemMeta().getPersistentDataContainer().getKeys().forEach(key ->
                    broken.getItemMeta().getPersistentDataContainer().remove(key));
        }
    }
}
