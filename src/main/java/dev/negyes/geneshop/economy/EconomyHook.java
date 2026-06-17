package dev.negyes.geneshop.economy;

import dev.negyes.geneshop.GeNeShop;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault gazdasagi rendszer beptlese.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class EconomyHook {

    private final GeNeShop plugin;
    private Economy economy;

    public EconomyHook(GeNeShop plugin) {
        this.plugin = plugin;
    }

    /** Megprobalja bekotni a Vault gazdasagot. true, ha sikerult. */
    public boolean setup() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp =
                plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public boolean isReady() {
        return economy != null;
    }

    public boolean has(OfflinePlayer player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        return economy != null && economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean deposit(OfflinePlayer player, double amount) {
        return economy != null && economy.depositPlayer(player, amount).transactionSuccess();
    }

    public double balance(OfflinePlayer player) {
        return economy == null ? 0.0 : economy.getBalance(player);
    }

    /** A penzosszeg formazasa a gazdasagi plugin stilusaban (pl. "$1,250"). */
    public String format(double amount) {
        if (economy == null) {
            return String.format(java.util.Locale.US, "$%,.2f", amount);
        }
        try {
            return economy.format(amount);
        } catch (Exception e) {
            return String.format(java.util.Locale.US, "$%,.2f", amount);
        }
    }
}
