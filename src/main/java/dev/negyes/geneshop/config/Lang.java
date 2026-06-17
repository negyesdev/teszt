package dev.negyes.geneshop.config;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

/**
 * A lang.yml betoltese (ShopGUI+ formatum) es uzenetkuldes.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class Lang {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();

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

    /**
     * Prefixelt uzenet kuldese, ahol a %item% helyere egy komponens kerul
     * (igy az item neve a kliens nyelven, magyarul jelenik meg).
     */
    public void sendItem(CommandSender to, String path, Component item, String... replacements) {
        String full = prefix + get(path, replacements);
        String[] parts = full.split("%item%", -1);
        Component message = Component.empty();
        for (int i = 0; i < parts.length; i++) {
            message = message.append(LEGACY.deserialize(parts[i]));
            if (i < parts.length - 1) {
                message = message.append(item);
            }
        }
        to.sendMessage(message);
    }

    /** Legacy (szinkodos) szovegbol komponens. */
    public static Component legacy(String input) {
        return LEGACY.deserialize(input == null ? "" : input);
    }

    public String getPrefix() {
        return prefix;
    }
}
