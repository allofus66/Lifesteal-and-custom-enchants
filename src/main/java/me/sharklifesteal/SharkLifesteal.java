package me.sharklifesteal;

import me.sharklifesteal.commands.CustomCommand;
import me.sharklifesteal.commands.EnchantCommand;
import me.sharklifesteal.commands.EnchantListCommand;
import me.sharklifesteal.commands.SharkLifestealCommand;
import me.sharklifesteal.commands.WithdrawCommand;
import me.sharklifesteal.enchants.EnchantManager;
import me.sharklifesteal.gui.CustomEnchantGUI;
import me.sharklifesteal.listeners.CombatListener;
import me.sharklifesteal.listeners.DeathListener;
import me.sharklifesteal.listeners.GuiListener;
import me.sharklifesteal.managers.LifestealManager;
import me.sharklifesteal.managers.WithdrawManager;
import me.sharklifesteal.utils.MessageUtil;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class SharkLifesteal extends JavaPlugin {

    private EnchantManager enchantManager;
    private LifestealManager lifestealManager;
    private WithdrawManager withdrawManager;
    private CustomEnchantGUI customEnchantGUI;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("enchants.yml", false);
        saveResource("messages.yml", false);

        reloadEverything();
        registerCommands();
        registerListeners();
        getLogger().info("SharkLifesteal & Enchants enabled.");
    }

    @Override
    public void onDisable() {
        if (withdrawManager != null) {
            withdrawManager.flushNow();
        }
    }

    public void reloadEverything() {
        reloadConfig();
        MessageUtil.load(this);
        this.enchantManager = new EnchantManager(this);
        this.lifestealManager = new LifestealManager(this);
        this.withdrawManager = new WithdrawManager(this);
        this.customEnchantGUI = new CustomEnchantGUI(this);
    }

    private void registerCommands() {
        registerCommand("enchant", new EnchantCommand(this));
        registerCommand("enchantlist", new EnchantListCommand(this));
        registerCommand("withdraw", new WithdrawCommand(this));
        registerCommand("custom", new CustomCommand(this));
        registerCommand("sharklifesteal", new SharkLifestealCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
    }

    private void registerCommand(String name, Object executor) {
        PluginCommand command = Objects.requireNonNull(getCommand(name), "Command missing: " + name);
        if (executor instanceof org.bukkit.command.CommandExecutor commandExecutor) {
            command.setExecutor(commandExecutor);
        }
        if (executor instanceof org.bukkit.command.TabCompleter tabCompleter) {
            command.setTabCompleter(tabCompleter);
        }
    }

    public EnchantManager getEnchantManager() {
        return enchantManager;
    }

    public LifestealManager getLifestealManager() {
        return lifestealManager;
    }

    public WithdrawManager getWithdrawManager() {
        return withdrawManager;
    }

    public CustomEnchantGUI getCustomEnchantGUI() {
        return customEnchantGUI;
    }
}
