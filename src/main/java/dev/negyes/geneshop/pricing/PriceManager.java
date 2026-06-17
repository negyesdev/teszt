package dev.negyes.geneshop.pricing;

import dev.negyes.geneshop.GeNeShop;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A dinamikus eladasi arak kezelese.
 *
 * Mukodes:
 *  - Minden material-hoz tartozik egy "multiplier" (szorzo) a
 *    [minMultiplier .. 1.0] tartomanyban, valamint egy idobelyeg.
 *  - Eladaskor a szorzo csokken (drop-per-sale * darabszam).
 *  - A szorzo folyamatosan, linearisan visszanovekszik 1.0-ra. A teljes
 *    visszaallas a legaljarol pontosan recover-minutes percig tart.
 *  - A visszaallast NEM idozitett feladat szamolja, hanem lustan, az ar
 *    lekereesekor (elapsed-time alapjan). Igy szerver-ujrainditas utan is
 *    pontos marad, es nincs folyamatos terheles.
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class PriceManager {

    private final GeNeShop plugin;

    private final Map<Material, Double> multipliers = new HashMap<>();
    private final Map<Material, Long> timestamps = new HashMap<>();

    private boolean enabled;
    private double dropPerSale;
    private double minMultiplier;
    private double recoverPerSecond;

    private File dataFile;

    public PriceManager(GeNeShop plugin) {
        this.plugin = plugin;
    }

    /** Beolvassa a konfigot es betolti a mentett arallapotot. */
    public void load() {
        FileConfiguration cfg = plugin.getConfig();
        this.enabled = cfg.getBoolean("dynamic-pricing.enabled", true);
        this.dropPerSale = cfg.getDouble("dynamic-pricing.drop-per-sale", 0.0008);
        this.minMultiplier = clamp(cfg.getDouble("dynamic-pricing.min-multiplier", 0.40), 0.01, 1.0);

        double recoverMinutes = cfg.getDouble("dynamic-pricing.recover-minutes", 150);
        if (recoverMinutes < 1) {
            recoverMinutes = 1;
        }
        // A teljes (1.0 - min) tartomany recoverMinutes perc alatt all helyre.
        this.recoverPerSecond = (1.0 - minMultiplier) / (recoverMinutes * 60.0);

        this.multipliers.clear();
        this.timestamps.clear();
        loadData();
    }

    /**
     * Visszaadja egy material aktualis eladasi ar-szorzojat [min..1.0].
     * A hivas mellekhataskent "normalizalja" az allapotot (visszaszamolja
     * az eltelt ido alatti felepuelest), igy mindig pontos.
     */
    public synchronized double getMultiplier(Material material) {
        if (!enabled) {
            return 1.0;
        }
        Double stored = multipliers.get(material);
        if (stored == null || stored >= 1.0) {
            multipliers.remove(material);
            timestamps.remove(material);
            return 1.0;
        }
        long now = System.currentTimeMillis();
        long last = timestamps.getOrDefault(material, now);
        double elapsedSeconds = Math.max(0, (now - last) / 1000.0);

        double recovered = stored + recoverPerSecond * elapsedSeconds;
        if (recovered >= 1.0) {
            multipliers.remove(material);
            timestamps.remove(material);
            return 1.0;
        }
        // Eltaroljuk a normalizalt allapotot.
        multipliers.put(material, recovered);
        timestamps.put(material, now);
        return recovered;
    }

    /** Az adott material aktualis (dinamikus) eladasi egysegara. */
    public double currentSellPrice(Material material, double baseSell) {
        if (baseSell < 0) {
            return baseSell;
        }
        return baseSell * getMultiplier(material);
    }

    /**
     * Rogziti, hogy eladtak az itembol -> csokkenti a szorzot.
     * @param amount eladott darabszam
     */
    public synchronized void registerSale(Material material, int amount) {
        if (!enabled || amount <= 0) {
            return;
        }
        double current = getMultiplier(material); // normalizal + visszaadja
        double next = current - dropPerSale * amount;
        if (next < minMultiplier) {
            next = minMultiplier;
        }
        multipliers.put(material, next);
        timestamps.put(material, System.currentTimeMillis());
    }

    /** true, ha az item ara jelenleg az alap alatt van. */
    public boolean isDiscounted(Material material) {
        return enabled && getMultiplier(material) < 0.999;
    }

    public boolean isEnabled() {
        return enabled;
    }

    // ---------------------------------------------------------------
    //  Mentes / betoltes
    // ---------------------------------------------------------------

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "prices.yml");
        if (!dataFile.exists()) {
            return;
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        if (!data.isConfigurationSection("prices")) {
            return;
        }
        for (String key : data.getConfigurationSection("prices").getKeys(false)) {
            Material material = Material.matchMaterial(key);
            if (material == null) {
                continue;
            }
            double mult = data.getDouble("prices." + key + ".multiplier", 1.0);
            long ts = data.getLong("prices." + key + ".timestamp", System.currentTimeMillis());
            if (mult < 1.0) {
                multipliers.put(material, clamp(mult, minMultiplier, 1.0));
                timestamps.put(material, ts);
            }
        }
    }

    /** Lemezre menti a jelenlegi (nem alap) arallapotokat. */
    public synchronized void save() {
        if (dataFile == null) {
            dataFile = new File(plugin.getDataFolder(), "prices.yml");
        }
        YamlConfiguration data = new YamlConfiguration();
        for (Map.Entry<Material, Double> entry : multipliers.entrySet()) {
            Material material = entry.getKey();
            double mult = entry.getValue();
            if (mult >= 1.0) {
                continue;
            }
            String path = "prices." + material.name();
            data.set(path + ".multiplier", mult);
            data.set(path + ".timestamp", timestamps.getOrDefault(material, System.currentTimeMillis()));
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Nem sikerult menteni a prices.yml-t: " + e.getMessage());
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
