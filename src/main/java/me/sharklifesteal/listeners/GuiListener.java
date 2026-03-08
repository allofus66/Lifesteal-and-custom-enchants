package me.sharklifesteal.listeners;

import me.sharklifesteal.SharkLifesteal;
import me.sharklifesteal.enchants.CustomEnchant;
import me.sharklifesteal.utils.MessageUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Optional;

public class GuiListener implements Listener {
    private final SharkLifesteal plugin;

    public GuiListener(SharkLifesteal plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(me.sharklifesteal.gui.CustomEnchantGUI.TITLE)) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || !clicked.hasItemMeta()) {
            return;
        }
        ItemMeta meta = clicked.getItemMeta();
        String enchantId = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "gui_enchant"), PersistentDataType.STRING);
        if (enchantId == null) {
            return;
        }

        Optional<CustomEnchant> enchantOptional = plugin.getEnchantManager().byId(enchantId);
        if (enchantOptional.isEmpty()) {
            return;
        }
        CustomEnchant enchant = enchantOptional.get();

        if (!canPay(player, enchant)) {
            MessageUtil.send(player, "enchant_cannot_pay");
            return;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        int current = plugin.getEnchantManager().getLevel(hand, enchant.getKey());
        int next = Math.min(enchant.getMaxLevel(), current + 1);
        if (next == current) {
            MessageUtil.send(player, "enchant_max_level");
            return;
        }

        if (plugin.getEnchantManager().applyEnchant(hand, enchant, next)) {
            takeCost(player, enchant);
            MessageUtil.send(player, "enchant_applied", "{enchant}", enchant.getDisplayName(), "{level}", String.valueOf(next));
        } else {
            MessageUtil.send(player, "enchant_invalid_item");
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
}
