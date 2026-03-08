package me.sharklifesteal.listeners;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.enchants.EnchantManager;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class CombatListener implements Listener {
    private final SharkLifesteal plugin;

    public CombatListener(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();
        if (!(damager instanceof Player player) || !(target instanceof LivingEntity livingTarget)) {
            return;
        }

        double damage = event.getFinalDamage();
        EnchantManager manager = plugin.getEnchantManager();

        int sharpnessPlus = manager.getLevel(player.getInventory().getItemInMainHand(), "sharpness_plus");
        if (sharpnessPlus > 0) {
            event.setDamage(event.getDamage() + sharpnessPlus * 1.25D);
        }

        int firebrand = manager.getLevel(player.getInventory().getItemInMainHand(), "firebrand");
        if (firebrand > 0 && roll(0.20D + firebrand * 0.05D)) {
            livingTarget.setFireTicks(Math.max(livingTarget.getFireTicks(), 40 + firebrand * 20));
        }

        int witherTouch = manager.getLevel(player.getInventory().getItemInMainHand(), "wither_touch");
        if (witherTouch > 0 && roll(0.15D + witherTouch * 0.04D)) {
            livingTarget.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40 + witherTouch * 20, Math.max(0, witherTouch - 1)));
        }

        plugin.getLifestealManager().handleHit(player, livingTarget, damage);

        if (plugin.getConfig().getBoolean("combat.particles", true)) {
            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1.0, 0), 4);
        }
    }

    private boolean roll(double chance) {
        return ThreadLocalRandom.current().nextDouble() <= chance;
    }
}
