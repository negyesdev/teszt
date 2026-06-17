package dev.negyes.geneshop.shop;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.config.Lang;
import dev.negyes.geneshop.util.ItemFactory;
import dev.negyes.geneshop.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Betolti a ShopGUI+ formatumu configot (config.yml + shops/*.yml) es kezeli
 * a vetel / eladas / mind-eladasa tranzakciokat. Az eladasi ar a GeNe Shop
 * egyedi dinamikus arazasat hasznalja (a logika valtozatlan).
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopManager {

    private final GeNeShop plugin;

    // Fomenu
    private String mainTitle = "Shop";
    private int mainSize = 54;
    private ItemStack mainFill;
    private boolean disableMainMenu = false;
    private final List<MenuButton> menuButtons = new ArrayList<>();

    // Shopok
    private final Map<String, Shop> shops = new LinkedHashMap<>();

    // Lore formatumok + placeholderek
    private List<String> itemLoreFormat = new ArrayList<>();
    private List<String> enchantLoreFormat = new ArrayList<>();
    private String unbuyableText = "Unbuyable";
    private String unsellableText = "Unsellable";
    private boolean hideBuyForUnbuyable = false;
    private boolean hideSellForUnsellable = false;

    // Nav gombok
    private ItemStack backButton;
    private int backSlot = 49;
    private ItemStack prevButton;
    private int prevSlot = 48;
    private ItemStack nextButton;
    private int nextSlot = 50;

    // Balance ikon forrasa
    private ConfigurationSection balanceItemSection;

    // Klikk akciok (ClickType nev -> "BUY"/"SELL"/"SELL_ALL"/"NONE")
    private final Map<String, String> clickActions = new HashMap<>();

    public ShopManager(GeNeShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        menuButtons.clear();
        shops.clear();
        clickActions.clear();

        FileConfiguration cfg = plugin.getConfig();

        this.mainTitle = Text.color(cfg.getString("shopMenuName", "Shop"));
        this.mainSize = normalizeSize(cfg.getInt("shopMenuSize", 54));
        this.disableMainMenu = cfg.getBoolean("disableMainMenu", false);
        this.mainFill = ItemFactory.build(cfg.getConfigurationSection("shopMenuFillItem"), plugin.getLogger());

        this.itemLoreFormat = cfg.getStringList("shopItemLoreFormat.item");
        this.enchantLoreFormat = cfg.getStringList("shopItemLoreFormat.enchantment");
        this.unbuyableText = cfg.getString("buyPriceForUnsellablePlaceholder", "Unbuyable");
        this.unsellableText = cfg.getString("sellPriceForUnsellablePlaceholder", "Unsellable");
        this.hideBuyForUnbuyable = cfg.getBoolean("hideBuyPriceForUnbuyable", false);
        this.hideSellForUnsellable = cfg.getBoolean("hideSellPriceForUnsellable", false);

        loadClickActions(cfg);
        loadButtons(cfg);
        this.balanceItemSection = cfg.getConfigurationSection("specialElements.balance.item");

        loadMenuAndShops(cfg);

        plugin.getLogger().info("Betoltve " + shops.size() + " shop, " + menuButtons.size() + " menu gomb.");
    }

    private void loadClickActions(FileConfiguration cfg) {
        ConfigurationSection sec = cfg.getConfigurationSection("clickActions");
        if (sec != null) {
            for (String key : sec.getKeys(false)) {
                clickActions.put(key.toUpperCase(Locale.ROOT), sec.getString(key, "NONE").toUpperCase(Locale.ROOT));
            }
        }
        clickActions.putIfAbsent("LEFT", "BUY");
        clickActions.putIfAbsent("RIGHT", "SELL");
        clickActions.putIfAbsent("SHIFT_RIGHT", "SELL_ALL");
        clickActions.putIfAbsent("MIDDLE", "SELL_ALL");
    }

    private void loadButtons(FileConfiguration cfg) {
        ConfigurationSection back = cfg.getConfigurationSection("buttons.goBack");
        if (back != null) {
            backButton = ItemFactory.build(back.getConfigurationSection("item"), plugin.getLogger());
            backSlot = back.getInt("slot", 49);
        }
        ConfigurationSection prev = cfg.getConfigurationSection("buttons.previousPage");
        if (prev != null) {
            prevButton = ItemFactory.build(prev.getConfigurationSection("item"), plugin.getLogger());
            prevSlot = prev.getInt("slot", 48);
        }
        ConfigurationSection next = cfg.getConfigurationSection("buttons.nextPage");
        if (next != null) {
            nextButton = ItemFactory.build(next.getConfigurationSection("item"), plugin.getLogger());
            nextSlot = next.getInt("slot", 50);
        }
    }

    private void loadMenuAndShops(FileConfiguration cfg) {
        ConfigurationSection menu = cfg.getConfigurationSection("shopMenuItems");
        if (menu == null) {
            plugin.getLogger().warning("Nincs 'shopMenuItems' a config.yml-ben!");
            return;
        }
        for (String key : menu.getKeys(false)) {
            ConfigurationSection entry = menu.getConfigurationSection(key);
            if (entry == null) {
                continue;
            }
            ItemStack icon = ItemFactory.build(entry.getConfigurationSection("item"), plugin.getLogger());
            String shopId = entry.getString("shop", key);
            int slot = entry.getInt("slot", -1);
            if (slot >= 0) {
                menuButtons.add(new MenuButton(icon, shopId, slot));
            }
            loadShopFile(shopId);
        }
    }

    private void loadShopFile(String id) {
        if (shops.containsKey(id)) {
            return;
        }
        File file = new File(plugin.getDataFolder(), "shops/" + id + ".yml");
        if (!file.exists()) {
            try {
                plugin.saveResource("shops/" + id + ".yml", false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Hianyzo shop fajl: shops/" + id + ".yml");
                return;
            }
        }
        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = data.getConfigurationSection(id);
        if (root == null) {
            plugin.getLogger().warning("Ervenytelen shop fajl (nincs '" + id + "' gyoker): " + file.getName());
            return;
        }

        String title = root.getString("name", id);
        int size = normalizeSize(root.getInt("size", 54));
        ItemStack fill = root.isConfigurationSection("fillItem")
                ? ItemFactory.build(root.getConfigurationSection("fillItem"), plugin.getLogger())
                : (mainFill == null ? null : mainFill.clone());

        Shop shop = new Shop(id, title, size, fill);

        ConfigurationSection items = root.getConfigurationSection("items");
        if (items != null) {
            for (String entryKey : items.getKeys(false)) {
                ConfigurationSection sec = items.getConfigurationSection(entryKey);
                if (sec == null) {
                    continue;
                }
                ShopEntry entry = parseEntry(sec);
                if (entry != null) {
                    shop.addEntry(entry);
                }
            }
        }
        shops.put(id, shop);
    }

    private ShopEntry parseEntry(ConfigurationSection sec) {
        String type = sec.getString("type", "item").toLowerCase(Locale.ROOT);
        int slot = sec.getInt("slot", -1);
        int page = sec.getInt("page", 1);
        if (slot < 0) {
            return null;
        }

        if (type.equals("special")) {
            if ("BALANCE".equalsIgnoreCase(sec.getString("special", ""))) {
                return new ShopEntry(ShopEntry.Kind.BALANCE, slot, page);
            }
            return null;
        }

        if (type.equals("enchantment")) {
            return parseEnchantment(sec, slot, page);
        }

        return parseItem(sec, slot, page);
    }

    private ShopEntry parseItem(ConfigurationSection sec, int slot, int page) {
        ConfigurationSection itemSec = sec.getConfigurationSection("item");
        if (itemSec == null) {
            return null;
        }
        ItemStack give = ItemFactory.build(itemSec, plugin.getLogger());

        ShopEntry entry = new ShopEntry(ShopEntry.Kind.ITEM, slot, page);
        entry.setGiveStack(give);
        entry.setBuyPrice(sec.getDouble("buyPrice", -1));
        if (sec.contains("sellPrice")) {
            entry.setSellPrice(sec.getDouble("sellPrice"));
        }

        String name = itemSec.getString("name");
        entry.setCustomName(name != null);
        String display = (name != null) ? Text.color(name) : Text.color("&f" + Text.pretty(give.getType()));
        entry.setDisplayName(display);
        entry.setPlainName((name != null) ? Text.strip(name) : Text.pretty(give.getType()));

        boolean sellable = entry.getSellPrice() != null && !hasSpecialMeta(give);
        entry.setSellable(sellable);
        return entry;
    }

    private ShopEntry parseEnchantment(ConfigurationSection sec, int slot, int page) {
        String enchName = sec.getString("enchantment", "");
        int level = sec.getInt("enchantmentLevel", 1);
        Enchantment ench = ItemFactory.resolveEnchantment(enchName);

        ConfigurationSection itemSec = sec.getConfigurationSection("item");
        ItemStack book = ItemFactory.build(itemSec, plugin.getLogger());
        if (ench != null && book.getItemMeta() instanceof EnchantmentStorageMeta esm) {
            esm.addStoredEnchant(ench, level, true);
            book.setItemMeta(esm);
        }

        ShopEntry entry = new ShopEntry(ShopEntry.Kind.ENCHANTMENT, slot, page);
        entry.setGiveStack(book);
        entry.setBuyPrice(sec.getDouble("buyPrice", -1));
        entry.setEnchantment(ench);
        entry.setEnchantLevel(level);

        String name = (itemSec != null) ? itemSec.getString("name") : null;
        entry.setCustomName(true);
        String display = (name != null) ? Text.color(name) : Text.color("&b" + enchName);
        entry.setDisplayName(display);
        entry.setPlainName((name != null) ? Text.strip(name) : enchName);
        entry.setSellable(false);
        return entry;
    }

    /** Igaz, ha az itemnek olyan metaja van, amitol nem "sima" (spawner/potion/konyv). */
    private boolean hasSpecialMeta(ItemStack stack) {
        Material type = stack.getType();
        if (type == Material.SPAWNER || type == Material.ENCHANTED_BOOK) {
            return true;
        }
        String n = type.name();
        return n.contains("POTION") || n.equals("TIPPED_ARROW");
    }

    // ---------------------------------------------------------------
    //  Tranzakciok
    // ---------------------------------------------------------------

    public void buy(Player player, ShopEntry entry, boolean fullStack) {
        if (!plugin.getEconomyHook().isReady()) {
            plugin.getLang().send(player, "MSG.ERROR");
            return;
        }
        if (!entry.isBuyable()) {
            plugin.getLang().send(player, "MSG.ITEM.CANNOTBUY");
            failSound(player);
            return;
        }

        int unit = entry.getQuantity();
        double unitPrice = entry.getBuyPrice();
        int maxStack = Math.max(1, entry.getGiveStack().getMaxStackSize());
        int groups = 1;
        if (fullStack) {
            groups = Math.max(1, maxStack / unit);
        }
        int amount = groups * unit;
        double total = groups * unitPrice;

        if (!hasSpace(player, entry.getGiveStack(), amount)) {
            plugin.getLang().send(player, "MSG.ITEM.FULLINVENTORY");
            failSound(player);
            return;
        }
        if (!plugin.getEconomyHook().has(player, total)) {
            plugin.getLang().sendItem(player, "MSG.ITEM.CANNOTAFFORD", itemComponent(entry),
                    "%price%", plugin.getEconomyHook().format(total),
                    "%amount%", String.valueOf(amount));
            failSound(player);
            return;
        }
        if (total > 0 && !plugin.getEconomyHook().withdraw(player, total)) {
            plugin.getLang().sendItem(player, "MSG.ITEM.CANNOTAFFORD", itemComponent(entry),
                    "%price%", plugin.getEconomyHook().format(total),
                    "%amount%", String.valueOf(amount));
            failSound(player);
            return;
        }

        giveItems(player, entry.getGiveStack(), amount);

        if (total <= 0 && plugin.getConfig().getBoolean("useDifferentMessagesForFreeItems", true)) {
            plugin.getLang().sendItem(player, "MSG.ITEM.BOUGHTFREE", itemComponent(entry),
                    "%amount%", String.valueOf(amount));
        } else {
            plugin.getLang().sendItem(player, "MSG.ITEM.BOUGHT", itemComponent(entry),
                    "%amount%", String.valueOf(amount),
                    "%price%", plugin.getEconomyHook().format(total));
        }
        playSound(player, "BUY_ITEM", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }

    public void sell(Player player, ShopEntry entry, boolean all) {
        if (!plugin.getEconomyHook().isReady()) {
            plugin.getLang().send(player, "MSG.ERROR");
            return;
        }
        if (!entry.isSellable()) {
            plugin.getLang().send(player, "MSG.ITEM.CANNOTSELL");
            failSound(player);
            return;
        }

        Material material = entry.getMaterial();
        int unit = entry.getQuantity();
        int owned = countPlain(player, material);
        if (owned <= 0) {
            plugin.getLang().sendItem(player, "MSG.ITEM.NOTENOUGH", itemComponent(entry),
                    "%amount%", String.valueOf(unit));
            failSound(player);
            return;
        }

        int toSell = all ? owned : Math.min(unit, owned);
        if (toSell <= 0) {
            plugin.getLang().sendItem(player, "MSG.ITEM.NOTENOUGH", itemComponent(entry),
                    "%amount%", String.valueOf(unit));
            failSound(player);
            return;
        }

        double perItemBase = entry.getSellPrice() / unit;
        double unitDynamic = plugin.getPriceManager().currentSellPrice(material, perItemBase);
        double total = unitDynamic * toSell;

        removePlain(player, material, toSell);
        if (total > 0) {
            plugin.getEconomyHook().deposit(player, total);
        }

        // Az eladas csokkenti az arat (egyedi dinamikus logika).
        plugin.getPriceManager().registerSale(material, toSell);

        boolean free = total <= 0 && plugin.getConfig().getBoolean("useDifferentMessagesForFreeItems", true);
        if (all) {
            plugin.getLang().sendItem(player, free ? "MSG.ITEM.SOLDALLFREE" : "MSG.ITEM.SOLDALL", itemComponent(entry),
                    "%amount%", String.valueOf(toSell),
                    "%price%", plugin.getEconomyHook().format(total));
        } else {
            plugin.getLang().sendItem(player, free ? "MSG.ITEM.SOLDFREE" : "MSG.ITEM.SOLD", itemComponent(entry),
                    "%amount%", String.valueOf(toSell),
                    "%price%", plugin.getEconomyHook().format(total));
        }
        playSound(player, all ? "SELL_ALL_ITEM" : "SELL_ITEM", Sound.ENTITY_EXPERIENCE_ORB_PICKUP);
    }

    /**
     * Az item neve komponensként az uzenetekhez. Sima itemeknel a kliens
     * nyelven (magyarul) jelenik meg; egyedi nevu itemeknel a megadott nev.
     */
    private Component itemComponent(ShopEntry entry) {
        if (entry.hasCustomName()) {
            return Lang.legacy(entry.getDisplayName());
        }
        return Component.translatable(entry.getMaterial().translationKey());
    }

    // ---------------------------------------------------------------
    //  Inventory segedek
    // ---------------------------------------------------------------

    private int countPlain(Player player, Material material) {
        int count = 0;
        for (ItemStack stack : player.getInventory().getStorageContents()) {
            if (stack != null && stack.getType() == material && !stack.hasItemMeta()) {
                count += stack.getAmount();
            }
        }
        return count;
    }

    private void removePlain(Player player, Material material, int amount) {
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

    private void giveItems(Player player, ItemStack template, int amount) {
        int max = Math.max(1, template.getMaxStackSize());
        int remaining = amount;
        while (remaining > 0) {
            int give = Math.min(max, remaining);
            ItemStack stack = template.clone();
            stack.setAmount(give);
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(stack);
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            remaining -= give;
        }
        player.updateInventory();
    }

    private boolean hasSpace(Player player, ItemStack template, int amount) {
        int capacity = 0;
        int max = Math.max(1, template.getMaxStackSize());
        for (ItemStack content : player.getInventory().getStorageContents()) {
            if (content == null || content.getType() == Material.AIR) {
                capacity += max;
            } else if (content.isSimilar(template)) {
                capacity += Math.max(0, content.getMaxStackSize() - content.getAmount());
            }
            if (capacity >= amount) {
                return true;
            }
        }
        return capacity >= amount;
    }

    // ---------------------------------------------------------------
    //  Hangok
    // ---------------------------------------------------------------

    private void playSound(Player player, String key, Sound fallback) {
        String name = plugin.getConfig().getString("sounds." + key, null);
        Sound sound = fallback;
        if (name != null) {
            try {
                sound = Sound.valueOf(name.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                sound = fallback;
            }
        }
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.7f, 1.3f);
        }
    }

    private void failSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.7f, 1.0f);
    }

    private int normalizeSize(int size) {
        if (size < 9) {
            return 9;
        }
        if (size > 54) {
            return 54;
        }
        return (size / 9) * 9;
    }

    // ---------------------------------------------------------------
    //  Getterek
    // ---------------------------------------------------------------

    public String getMainTitle() {
        return mainTitle;
    }

    public int getMainSize() {
        return mainSize;
    }

    public ItemStack getMainFill() {
        return mainFill;
    }

    public boolean isMainMenuDisabled() {
        return disableMainMenu;
    }

    public List<MenuButton> getMenuButtons() {
        return menuButtons;
    }

    public Shop getShop(String id) {
        return id == null ? null : shops.get(id);
    }

    public Map<String, Shop> getShops() {
        return shops;
    }

    public List<String> getItemLoreFormat() {
        return itemLoreFormat;
    }

    public List<String> getEnchantLoreFormat() {
        return enchantLoreFormat;
    }

    public String getUnbuyableText() {
        return unbuyableText;
    }

    public String getUnsellableText() {
        return unsellableText;
    }

    public boolean isHideBuyForUnbuyable() {
        return hideBuyForUnbuyable;
    }

    public boolean isHideSellForUnsellable() {
        return hideSellForUnsellable;
    }

    public ItemStack getBackButton() {
        return backButton;
    }

    public int getBackSlot() {
        return backSlot;
    }

    public ItemStack getPrevButton() {
        return prevButton;
    }

    public int getPrevSlot() {
        return prevSlot;
    }

    public ItemStack getNextButton() {
        return nextButton;
    }

    public int getNextSlot() {
        return nextSlot;
    }

    public ConfigurationSection getBalanceItemSection() {
        return balanceItemSection;
    }

    public Map<String, String> getClickActions() {
        return clickActions;
    }
}
