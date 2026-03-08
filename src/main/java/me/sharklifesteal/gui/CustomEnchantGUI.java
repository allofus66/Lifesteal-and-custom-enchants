package me.sharklifesteal.gui;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.enchants.CustomEnchant;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CustomEnchantGUI {
    public static final String TITLE = "§8Custom Enchants";
    private final SharkLifesteal plugin;

    public CustomEnchantGUI(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        List<CustomEnchant> enchants = new ArrayList<>(plugin.getEnchantManager().getAll());
        int size = Math.max(27, ((enchants.size() / 9) + 1) * 9);
        Inventory inventory = Bukkit.createInventory(null, Math.min(size, 54), TITLE);

        int slot = 0;
        for (CustomEnchant enchant : enchants) {
            if (slot >= inventory.getSize()) {
                break;
            }
            inventory.setItem(slot++, icon(enchant));
        }
        player.openInventory(inventory);
    }

    private ItemStack icon(CustomEnchant enchant) {
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        meta.setDisplayName("§6" + enchant.getDisplayName() + " §7[" + enchant.getRarity() + "]");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = new ArrayList<>();
        lore.add("§7Max Level: §e" + enchant.getMaxLevel());
        lore.add("§7Chance: §e" + (int) (enchant.getChance() * 100) + "%");
        lore.add("§7Cost: §e" + enchant.getCostAmount() + " " + enchant.getCostType().name());
        lore.add("§8Click to apply on held item");
        meta.setLore(lore);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "gui_enchant"), PersistentDataType.STRING, enchant.getKey());
        item.setItemMeta(meta);
        return item;
    }
}
