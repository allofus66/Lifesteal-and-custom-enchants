package me.sharklifesteal.managers;

import me.sharklifesteal.SharkLifesteal;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WithdrawManager {
    private final SharkLifesteal plugin;
    private final File dataFile;
    private YamlConfiguration data;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public WithdrawManager(SharkLifesteal plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "players.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public int getBonusHearts(UUID uuid) {
        return data.getInt(path(uuid, "bonus_hearts"), 0);
    }

    public void addBonusHearts(UUID uuid, int amount) {
        if (amount <= 0) {
            return;
        }
        data.set(path(uuid, "bonus_hearts"), getBonusHearts(uuid) + amount);
        saveAsync();
    }

    public Result withdraw(Player player, int hearts) {
        if (!plugin.getConfig().getBoolean("withdraw.enabled", true)) {
            return Result.DISABLED;
        }
        if (hearts <= 0) {
            return Result.INVALID;
        }
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long cooldownUntil = cooldowns.getOrDefault(uuid, 0L);
        if (now < cooldownUntil) {
            return Result.COOLDOWN;
        }

        int maxPerDay = plugin.getConfig().getInt("withdraw.max_per_day", 20);
        int usedToday = getDailyUsed(uuid);
        if (usedToday + hearts > maxPerDay) {
            return Result.DAILY_LIMIT;
        }

        int available = getBonusHearts(uuid);
        if (hearts > available) {
            return Result.NOT_ENOUGH;
        }

        double reduceHealth = hearts * 2.0D;
        if (player.getAttribute(Attribute.MAX_HEALTH) == null) {
            return Result.ERROR;
        }
        double currentMax = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        if (currentMax - reduceHealth < 2.0D) {
            return Result.MINIMUM_HEALTH;
        }

        data.set(path(uuid, "bonus_hearts"), available - hearts);
        setDailyUsed(uuid, usedToday + hearts);
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(currentMax - reduceHealth);
        player.getInventory().addItem(buildHeartCrystal(hearts));

        long cooldown = plugin.getConfig().getLong("withdraw.cooldown", 300L) * 1000L;
        cooldowns.put(uuid, now + cooldown);
        saveAsync();
        return Result.SUCCESS;
    }

    public ItemStack buildHeartCrystal(int amount) {
        Material material = Material.matchMaterial(plugin.getConfig().getString("withdraw.item", "NETHER_STAR"));
        if (material == null) {
            material = Material.NETHER_STAR;
        }
        ItemStack crystal = new ItemStack(material);
        ItemMeta meta = crystal.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cHeart Crystal");
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "heart_crystal"), PersistentDataType.INTEGER, amount);
            crystal.setItemMeta(meta);
        }
        return crystal;
    }

    private int getDailyUsed(UUID uuid) {
        String today = LocalDate.now().toString();
        String storedDay = data.getString(path(uuid, "daily_date"), today);
        if (!today.equals(storedDay)) {
            data.set(path(uuid, "daily_date"), today);
            data.set(path(uuid, "daily_used"), 0);
            return 0;
        }
        return data.getInt(path(uuid, "daily_used"), 0);
    }

    private void setDailyUsed(UUID uuid, int amount) {
        data.set(path(uuid, "daily_date"), LocalDate.now().toString());
        data.set(path(uuid, "daily_used"), amount);
    }

    private String path(UUID uuid, String key) {
        return "players." + uuid + "." + key;
    }

    public void saveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                data.save(dataFile);
            } catch (IOException ex) {
                plugin.getLogger().warning("Could not save players.yml: " + ex.getMessage());
            }
        });
    }

    public void flushNow() {
        try {
            data.save(dataFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Could not save players.yml on shutdown: " + ex.getMessage());
        }
    }

    public enum Result {
        SUCCESS, DISABLED, INVALID, COOLDOWN, DAILY_LIMIT, NOT_ENOUGH, MINIMUM_HEALTH, ERROR
    }
}
