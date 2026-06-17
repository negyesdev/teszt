package dev.negyes.geneshop.config;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * A messages.yml betoltese es uzenetkuldes.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class Messages {

    private final GeNeShop plugin;
    private FileConfiguration config;
    private String prefix = "";

    public Messages(GeNeShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        prefix = Text.color(config.getString("prefix", ""));
    }

    /** Nyers (prefix nelkuli, szinezett) uzenet a kulcsbol. */
    public String raw(String key) {
        return Text.color(config.getString(key, "&c[hianyzo uzenet: " + key + "]"));
    }

    /** Uzenet a kulcs alapjan, %a%->b% behelyettesitessel. */
    public String get(String key, String... replacements) {
        String message = config.getString(key, "&c[hianyzo uzenet: " + key + "]");
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return Text.color(message);
    }

    /** Prefixelt uzenet kuldese. */
    public void send(CommandSender to, String key, String... replacements) {
        to.sendMessage(prefix + get(key, replacements));
    }
}
