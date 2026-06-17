package dev.negyes.geneshop.config;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.util.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * A lang.yml betoltese (ShopGUI+ formatum) es uzenetkuldes.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class Lang {

    private final GeNeShop plugin;
    private FileConfiguration config;
    private String prefix = "";

    public Lang(GeNeShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "lang.yml");
        if (!file.exists()) {
            plugin.saveResource("lang.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(file);
        prefix = Text.color(config.getString("PREFIX", ""));
    }

    /** Uzenet a kulcsbol (pl. "MSG.ITEM.BOUGHT"), %a%->b% behelyettesitessel, szinezve. */
    public String get(String path, String... replacements) {
        String message = config.getString(path);
        if (message == null) {
            return "§c[hianyzo uzenet: " + path + "]";
        }
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return Text.color(message);
    }

    /** Prefixelt uzenet kuldese. */
    public void send(CommandSender to, String path, String... replacements) {
        to.sendMessage(prefix + get(path, replacements));
    }

    public String getPrefix() {
        return prefix;
    }
}
