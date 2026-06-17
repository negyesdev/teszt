package dev.negyes.geneshop.shop;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.util.Text;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Betolti a kategoriakat es kezeli a vetel/eladas tranzakciokat.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopManager {

    private final GeNeShop plugin;
    private final Map<String, ShopCategory> categories = new LinkedHashMap<>();

    private String currencySymbol = "$";
    private int priceDecimals = 2;
    private boolean sounds = true;

    public ShopManager(GeNeShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        categories.clear();
        this.currencySymbol = plugin.getConfig().getString("settings.currency-symbol", "$");
        this.priceDecimals = plugin.getConfig().getInt("settings.price-decimals", 2);
        this.sounds = plugin.getConfig().getBoolean("settings.sounds", true);

        ConfigurationSection root = plugin.getConfig().getConfigurationSection("categories");
        if (root == null) {
            plugin.getLogger().warning("Nincs 'categories' szekcio a config.yml-ben!");
            return;
        }

        for (String id : root.getKeys(false)) {
            ConfigurationSection sec = root.getConfigurationSection(id);
            if (sec == null) {
                continue;
            }
            String display = Text.color(sec.getString("display", id));
            Material icon = parseMaterial(sec.getString("icon", "CHEST"), Material.CHEST);
            int slot = sec.getInt("slot", -1);

            ShopCategory category = new ShopCategory(id, display, icon, slot);

            ConfigurationSection itemsSec = sec.getConfigurationSection("items");
            if (itemsSec != null) {
                for (String matName : itemsSec.getKeys(false)) {
                    Material material = Material.matchMaterial(matName);
                    if (material == null || !material.isItem()) {
                        plugin.getLogger().warning("Ervenytelen item a(z) '" + id + "' kategoriaban: " + matName);
                        continue;
                    }
                    double buy = itemsSec.getDouble(matName + ".buy", -1);
                    double sell = itemsSec.getDouble(matName + ".sell", -1);
                    category.addItem(new ShopItem(material, buy, sell, id));
                }
            }
            categories.put(id.toLowerCase(Locale.ROOT), category);
        }
        plugin.getLogger().info("Betoltve " + categories.size() + " kategoria.");
    }

    public Collection<ShopCategory> getCategories() {
        return categories.values();
    }

    public ShopCategory getCategory(String id) {
        return id == null ? null : categories.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean areSoundsEnabled() {
        return sounds;
    }

    // ---------------------------------------------------------------
    //  Ar formazas
    // ---------------------------------------------------------------

    public String formatPrice(double price) {
        return currencySymbol + String.format(Locale.US, "%,." + priceDecimals + "f", price);
    }

    // ---------------------------------------------------------------
    //  Tranzakciok
    // ---------------------------------------------------------------

    /** Megveteti a jatekossal az itemet. */
    public void buy(Player player, ShopItem item, int amount) {
        if (!plugin.getEconomyHook().isReady()) {
            plugin.getMessages().send(player, "no-economy");
            return;
        }
        if (!item.isBuyable()) {
            plugin.getMessages().send(player, "not-buyable");
            return;
        }
        amount = Math.max(1, amount);
        double total = item.getBuyPrice() * amount;

        if (!plugin.getEconomyHook().has(player, total)) {
            plugin.getMessages().send(player, "not-enough-money", "%price%", formatPrice(total));
            failSound(player);
            return;
        }

        // Eloszor megnezzuk, befer-e (a maradekot eldobnank, ezt nem akarjuk).
        ItemStack stack = new ItemStack(item.getMaterial(), amount);
        if (!hasInventorySpace(player, stack)) {
            plugin.getMessages().send(player, "inventory-full");
            failSound(player);
            return;
        }

        if (!plugin.getEconomyHook().withdraw(player, total)) {
            plugin.getMessages().send(player, "not-enough-money", "%price%", formatPrice(total));
            failSound(player);
            return;
        }

        giveItems(player, item.getMaterial(), amount);
        plugin.getMessages().send(player, "buy-success",
                "%amount%", String.valueOf(amount),
                "%item%", Text.pretty(item.getMaterial()),
                "%price%", formatPrice(total));
        successSound(player);
    }

    /** Eladatja a jatekos itemeit. amount = -1 eseten az osszeset. */
    public void sell(Player player, ShopItem item, int amount) {
        if (!plugin.getEconomyHook().isReady()) {
            plugin.getMessages().send(player, "no-economy");
            return;
        }
        if (!item.isSellable()) {
            plugin.getMessages().send(player, "not-sellable");
            return;
        }

        int owned = countItems(player, item.getMaterial());
        if (owned <= 0) {
            plugin.getMessages().send(player, "not-enough-items");
            failSound(player);
            return;
        }

        int toSell = (amount < 0) ? owned : Math.min(amount, owned);
        if (toSell <= 0) {
            plugin.getMessages().send(player, "not-enough-items");
            failSound(player);
            return;
        }

        // Az aktualis (dinamikus) egysegar, majd a teljes osszeg.
        double unit = plugin.getPriceManager().currentSellPrice(item.getMaterial(), item.getBaseSell());
        double total = unit * toSell;

        removeItems(player, item.getMaterial(), toSell);
        plugin.getEconomyHook().deposit(player, total);

        // Eladas utan esik az ar.
        plugin.getPriceManager().registerSale(item.getMaterial(), toSell);

        plugin.getMessages().send(player, "sell-success",
                "%amount%", String.valueOf(toSell),
                "%item%", Text.pretty(item.getMaterial()),
                "%price%", formatPrice(total));
        successSound(player);
    }

    // ---------------------------------------------------------------
    //  Inventory segedek
    // ---------------------------------------------------------------

    public int countItems(Player player, Material material) {
        // Csak "tiszta" (nem nevezett, nem enchantelt stb.) itemeket szamolunk,
        // hogy a ritka/varazsolt targyak ne menjenek el alapron.
        int count = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == material && !stack.hasItemMeta()) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack stack = contents[i];
            if (stack == null || stack.getType() != material || stack.hasItemMeta()) {
                continue;
            }
            int take = Math.min(remaining, stack.getAmount());
            stack.setAmount(stack.getAmount() - take);
            remaining -= take;
            if (stack.getAmount() <= 0) {
                contents[i] = null;
            }
        }
        player.getInventory().setStorageContents(contents);
        player.updateInventory();
    }

    private void giveItems(Player player, Material material, int amount) {
        int max = material.getMaxStackSize();
        int remaining = amount;
        while (remaining > 0) {
            int give = Math.min(max, remaining);
            ItemStack stack = new ItemStack(material, give);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
            // Ha valami maradna (elvileg a hasInventorySpace miatt nem), eldobjuk a labhoz.
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            remaining -= give;
        }
        player.updateInventory();
    }

    /** Megnezi, befer-e a stack a jatekos inventoryjaba (eldobas nelkul). */
    private boolean hasInventorySpace(Player player, ItemStack stack) {
        int capacity = 0;
        int max = stack.getMaxStackSize();
        for (ItemStack content : player.getInventory().getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                capacity += max;
            } else if (content.getType() == stack.getType() && !content.hasItemMeta()) {
                capacity += Math.max(0, content.getMaxStackSize() - content.getAmount());
            }
            if (capacity >= stack.getAmount()) {
                return true;
            }
        }
        return capacity >= stack.getAmount();
    }

    // ---------------------------------------------------------------
    //  Hangok
    // ---------------------------------------------------------------

    private void successSound(Player player) {
        if (sounds) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.4f);
        }
    }

    private void failSound(Player player) {
        if (sounds) {
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
        }
    }

    private Material parseMaterial(String name, Material fallback) {
        Material material = Material.matchMaterial(name == null ? "" : name);
        return material == null ? fallback : material;
    }
}
