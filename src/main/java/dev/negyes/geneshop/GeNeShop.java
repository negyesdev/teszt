package dev.negyes.geneshop;

import dev.negyes.geneshop.command.AdminCommand;
import dev.negyes.geneshop.command.ShopCommand;
import dev.negyes.geneshop.config.Lang;
import dev.negyes.geneshop.economy.EconomyHook;
import dev.negyes.geneshop.gui.ShopGUI;
import dev.negyes.geneshop.gui.ShopListener;
import dev.negyes.geneshop.pricing.PriceManager;
import dev.negyes.geneshop.shop.ShopManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * GeNe Shop - ShopGUI+ stilusu, dinamikus arazasu GUI shop (Minecraft 1.21.8).
 *
 * Keszitette: negyes Gerii06
 */
public class GeNeShop extends JavaPlugin {

    private Lang lang;
    private EconomyHook economyHook;
    private PriceManager priceManager;
    private ShopManager shopManager;
    private ShopGUI shopGUI;

    private BukkitTask saveTask;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.lang = new Lang(this);
        this.economyHook = new EconomyHook(this);
        this.priceManager = new PriceManager(this);
        this.shopManager = new ShopManager(this);
        this.shopGUI = new ShopGUI(this);

        lang.load();
        priceManager.load();
        shopManager.load();

        if (!economyHook.setup()) {
            getLogger().warning("============================================");
            getLogger().warning(" Nem talaltam Vault + gazdasagi plugint!");
            getLogger().warning(" A vetel/eladas nem fog mukodni, amig nincs.");
            getLogger().warning("============================================");
        } else {
            getLogger().info("Gazdasagi rendszer sikeresen bekotve (Vault).");
        }

        getServer().getPluginManager().registerEvents(new ShopListener(this), this);

        registerCommand("shop", new ShopCommand(this));
        registerCommand("geneshop", new AdminCommand(this));

        startSaveTask();

        getLogger().info("GeNe Shop bekapcsolva. Keszitette: negyes Gerii06");
    }

    @Override
    public void onDisable() {
        if (saveTask != null) {
            saveTask.cancel();
            saveTask = null;
        }
        if (priceManager != null) {
            priceManager.save();
        }
        getLogger().info("GeNe Shop kikapcsolva. Az arak elmentve.");
    }

    /** Ujratolt mindent (config, uzenetek, shopok, arak). */
    public void reloadAll() {
        if (priceManager != null) {
            priceManager.save();
        }
        reloadConfig();
        lang.load();
        priceManager.load();
        shopManager.load();
        startSaveTask();
    }

    private void startSaveTask() {
        if (saveTask != null) {
            saveTask.cancel();
        }
        long intervalSeconds = Math.max(30, getConfig().getLong("dynamic-pricing.save-interval-seconds", 120));
        long ticks = intervalSeconds * 20L;
        saveTask = getServer().getScheduler().runTaskTimer(this, () -> priceManager.save(), ticks, ticks);
    }

    private void registerCommand(String name, Object handler) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            getLogger().warning("Nincs regisztralva a(z) '" + name + "' parancs a plugin.yml-ben!");
            return;
        }
        if (handler instanceof org.bukkit.command.CommandExecutor executor) {
            command.setExecutor(executor);
        }
        if (handler instanceof org.bukkit.command.TabCompleter completer) {
            command.setTabCompleter(completer);
        }
    }

    public Lang getLang() {
        return lang;
    }

    public EconomyHook getEconomyHook() {
        return economyHook;
    }

    public PriceManager getPriceManager() {
        return priceManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public ShopGUI getShopGUI() {
        return shopGUI;
    }
}
