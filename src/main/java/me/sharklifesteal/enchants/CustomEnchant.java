package me.sharklifesteal.enchants;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomEnchant {
    private final String key;
    private final String displayName;
    private final int maxLevel;
    private final double chance;
    private final double value;
    private final List<String> itemTypes;
    private final String rarity;
    private final CostType costType;
    private final int costAmount;
    private final Material costMaterial;

    public CustomEnchant(String key, ConfigurationSection section) {
        this.key = key;
        this.displayName = section.getString("display", key);
        this.maxLevel = Math.max(1, section.getInt("max_level", 1));
        this.chance = Math.max(0.0D, Math.min(1.0D, section.getDouble("chance", 1.0D)));
        this.value = section.getDouble("value", 0.0D);
        this.rarity = section.getString("rarity", "COMMON");
        this.itemTypes = new ArrayList<>(section.getStringList("item_types"));

        ConfigurationSection cost = section.getConfigurationSection("cost");
        if (cost != null) {
            this.costType = CostType.valueOf(cost.getString("type", "XP").toUpperCase(Locale.ROOT));
            this.costAmount = Math.max(0, cost.getInt("amount", 1));
            this.costMaterial = Material.matchMaterial(cost.getString("material", "LAPIS_LAZULI"));
        } else {
            this.costType = CostType.XP;
            this.costAmount = 1;
            this.costMaterial = Material.LAPIS_LAZULI;
        }
    }

    public boolean supports(Material material) {
        if (itemTypes.isEmpty()) {
            return true;
        }
        Set<String> names = itemTypes.stream().map(s -> s.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
        String matName = material.name().toUpperCase(Locale.ROOT);
        for (String type : names) {
            if (type.equals(matName) || matName.endsWith(type)) {
                return true;
            }
        }
        return false;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public int getMaxLevel() { return maxLevel; }
    public double getChance() { return chance; }
    public double getValue() { return value; }
    public String getRarity() { return rarity; }
    public CostType getCostType() { return costType; }
    public int getCostAmount() { return costAmount; }
    public Material getCostMaterial() { return costMaterial == null ? Material.LAPIS_LAZULI : costMaterial; }

    public enum CostType { XP, ITEM }
}
