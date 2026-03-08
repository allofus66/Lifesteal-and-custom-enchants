package me.sharklifesteal.managers;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.enchants.EnchantManager;
import me.sharklifesteal.utils.MessageUtil;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LifestealManager {
    private final SharkLifesteal plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public LifestealManager(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    public void handleHit(Player attacker, LivingEntity victim, double damage) {
        if (!plugin.getConfig().getBoolean("lifesteal.enabled", true) || damage <= 0) {
            return;
        }
        if (victim instanceof Player && !plugin.getConfig().getBoolean("lifesteal.pvp", true)) {
            return;
        }
        if (!(victim instanceof Player) && !plugin.getConfig().getBoolean("lifesteal.pve", true)) {
            return;
        }

        long cooldownMs = plugin.getConfig().getLong("lifesteal.cooldown_ms", 0L);
        if (cooldownMs > 0 && System.currentTimeMillis() < cooldowns.getOrDefault(attacker.getUniqueId(), 0L)) {
            return;
        }

        EnchantManager manager = plugin.getEnchantManager();
        int vampiricLevel = manager.getLevel(attacker.getInventory().getItemInMainHand(), "vampiric");
        int boostLevel = manager.getLevel(attacker.getInventory().getItemInMainHand(), "lifesteal_boost");

        double basePercent = plugin.getConfig().getDouble("lifesteal.percent_heal", 25.0D) / 100.0D;
        double enchantBonusPercent = (vampiricLevel * 0.05D) + (boostLevel * 0.08D);
        double heal = damage * (basePercent + enchantBonusPercent);

        double enchantMaxBonus = boostLevel * 2.0D;
        double maxPerHit = plugin.getConfig().getDouble("lifesteal.max_heal", 10.0D) + enchantMaxBonus;
        heal = Math.min(maxPerHit, heal);
        if (heal <= 0) {
            return;
        }

        double maxHealth = attacker.getAttribute(Attribute.MAX_HEALTH) != null
                ? attacker.getAttribute(Attribute.MAX_HEALTH).getValue() : 20.0D;
        attacker.setHealth(Math.min(maxHealth, attacker.getHealth() + heal));

        MessageUtil.sendActionBar(attacker, MessageUtil.get("lifesteal_heal")
                .replace("{amount}", String.format("%.1f", heal)));

        if (cooldownMs > 0) {
            cooldowns.put(attacker.getUniqueId(), System.currentTimeMillis() + cooldownMs);
        }
    }
}
