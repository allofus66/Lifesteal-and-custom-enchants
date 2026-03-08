package me.sharklifesteal.utils;

import me.sharklifesteal.SharkLifesteal;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public final class MessageUtil {
    private static YamlConfiguration messages;

    private MessageUtil() {
    }

    public static void load(SharkLifesteal plugin) {
        messages = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "messages.yml"));
    }

    public static String get(String path) {
        String raw = messages == null ? null : messages.getString(path);
        return color(raw == null ? "&cMissing message: " + path : raw);
    }

    public static void send(CommandSender sender, String path, String... replacements) {
        String msg = get(path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            msg = msg.replace(replacements[i], replacements[i + 1]);
        }
        sender.sendMessage(msg);
    }

    public static void sendActionBar(Player player, String text) {
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(color(text));
        player.sendActionBar(component);
    }

    private static String color(String input) {
        return input.replace("&", "§");
    }
}
