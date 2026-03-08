package me.sharklifesteal.enchants;

import me.sharklifesteal.SharkLifesteal;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class EnchantManager {

    private final SharkLifesteal plugin;
    private final Map<String, CustomEnchant> enchants = new HashMap<>();
    private final NamespacedKey keyPrefix;

    public EnchantManager(SharkLifesteal plugin) {
        this.plugin = plugin;
        this.keyPrefix = new NamespacedKey(plugin, "ce");
        load();
    }

    public void load() {
        enchants.clear();
        File file = new File(plugin.getDataFolder(), "enchants.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("enchants");
        if (section == null) {
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection enchantSection = section.getConfigurationSection(id);
            if (enchantSection == null) {
                continue;
            }
            enchants.put(id.toLowerCase(Locale.ROOT), new CustomEnchant(id.toLowerCase(Locale.ROOT), enchantSection));
        }
    }

    public Collection<CustomEnchant> getAll() {
        return Collections.unmodifiableCollection(enchants.values());
    }

    public Optional<CustomEnchant> byId(String id) {
        return Optional.ofNullable(enchants.get(id.toLowerCase(Locale.ROOT)));
    }

    public int getLevel(ItemStack item, String enchantKey) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, keyPrefix.getKey() + "_" + enchantKey.toLowerCase(Locale.ROOT));
        Integer level = meta.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
        return level == null ? 0 : level;
    }

    public boolean hasEnchant(ItemStack item, String enchantKey) {
        return getLevel(item, enchantKey) > 0;
    }

    public boolean applyEnchant(ItemStack item, CustomEnchant enchant, int level) {
        if (item == null || item.getType() == Material.AIR || !enchant.supports(item.getType())) {
            return false;
        }
        if (isBlacklisted(item.getType())) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        int bounded = Math.max(1, Math.min(enchant.getMaxLevel(), level));
        NamespacedKey key = new NamespacedKey(plugin, keyPrefix.getKey() + "_" + enchant.getKey());
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.INTEGER, bounded);
        item.setItemMeta(meta);
        return true;
    }

    public boolean isBlacklisted(Material material) {
        return plugin.getConfig().getStringList("enchants.blacklisted_items").stream()
                .map(String::toUpperCase)
                .anyMatch(name -> name.equals(material.name()));
    }
}
