package dev.negyes.geneshop.gui;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.ShopCategory;
import dev.negyes.geneshop.shop.ShopItem;
import dev.negyes.geneshop.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * A shop GUI-k felepitese (fomenu + kategoria oldalak).
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopGUI {

    private static final int CATEGORY_SIZE = 54;
    private static final int ITEMS_PER_PAGE = 45;

    private final GeNeShop plugin;

    public ShopGUI(GeNeShop plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------
    //  Fomenu
    // ---------------------------------------------------------------

    public void openMain(Player player) {
        String title = Text.color(plugin.getConfig().getString("settings.main-title", "&8» &b&lGeNe Shop"));
        ShopHolder holder = new ShopHolder(ShopHolder.Type.MAIN, null, 0);
        Inventory inv = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inv);

        ItemStack filler = filler();
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }

        int autoSlot = 10;
        for (ShopCategory category : plugin.getShopManager().getCategories()) {
            int slot = category.getSlot();
            if (slot < 0 || slot >= inv.getSize()) {
                slot = autoSlot++;
            }
            List<String> lore = new ArrayList<>();
            lore.add(Text.color("&7Itemek szama: &f" + category.getItems().size()));
            lore.add("");
            lore.add(Text.color("&eKattints a megnyitashoz!"));

            inv.setItem(slot, makeItem(category.getIcon(), category.getDisplay(), lore));
            holder.mapCategory(slot, category.getId());
        }

        player.openInventory(inv);
    }

    // ---------------------------------------------------------------
    //  Kategoria oldal
    // ---------------------------------------------------------------

    public void openCategory(Player player, ShopCategory category, int page) {
        List<ShopItem> items = category.getItems();
        int totalPages = Math.max(1, (int) Math.ceil(items.size() / (double) ITEMS_PER_PAGE));
        page = Math.max(0, Math.min(page, totalPages - 1));

        String title = category.getDisplay() + Text.color(" &8(" + (page + 1) + "/" + totalPages + ")");
        ShopHolder holder = new ShopHolder(ShopHolder.Type.CATEGORY, category.getId(), page);
        Inventory inv = Bukkit.createInventory(holder, CATEGORY_SIZE, title);
        holder.setInventory(inv);

        // Itemek
        int start = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int index = start + i;
            if (index >= items.size()) {
                break;
            }
            ShopItem shopItem = items.get(index);
            inv.setItem(i, buildShopIcon(shopItem));
            holder.mapItem(i, shopItem);
        }

        // Also navigacios sor
        ItemStack filler = filler();
        for (int i = ITEMS_PER_PAGE; i < CATEGORY_SIZE; i++) {
            inv.setItem(i, filler);
        }

        inv.setItem(45, navItem(Material.ARROW, "&aVissza a fomenube"));
        holder.mapNav(45, NavAction.BACK);

        if (page > 0) {
            inv.setItem(48, navItem(Material.SPECTRAL_ARROW, "&eElozo oldal"));
            holder.mapNav(48, NavAction.PREV_PAGE);
        }

        inv.setItem(49, infoItem());
        holder.mapNav(49, NavAction.INFO);

        if (page < totalPages - 1) {
            inv.setItem(50, navItem(Material.SPECTRAL_ARROW, "&eKovetkezo oldal"));
            holder.mapNav(50, NavAction.NEXT_PAGE);
        }

        inv.setItem(53, navItem(Material.BARRIER, "&cBezaras"));
        holder.mapNav(53, NavAction.CLOSE);

        player.openInventory(inv);
    }

    /** Egy nyitott kategoria-oldal item ikonjainak helyben frissitese (pl. eladas utan). */
    public void refreshItems(ShopHolder holder) {
        if (holder.getType() != ShopHolder.Type.CATEGORY) {
            return;
        }
        ShopCategory category = plugin.getShopManager().getCategory(holder.getCategoryId());
        if (category == null || holder.getInventory() == null) {
            return;
        }
        Inventory inv = holder.getInventory();
        List<ShopItem> items = category.getItems();
        int start = holder.getPage() * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            int index = start + i;
            if (index >= items.size()) {
                break;
            }
            inv.setItem(i, buildShopIcon(items.get(index)));
        }
    }

    // ---------------------------------------------------------------
    //  Item ikonok
    // ---------------------------------------------------------------

    private ItemStack buildShopIcon(ShopItem item) {
        Material material = item.getMaterial();
        List<String> lore = new ArrayList<>();
        lore.add(Text.color("&8&m                          "));

        if (item.isBuyable()) {
            lore.add(Text.color("&aVetel: &f" + plugin.getShopManager().formatPrice(item.getBuyPrice()) + " &7/ db"));
        } else {
            lore.add(Text.color("&aVetel: &c nem veheto"));
        }

        if (item.isSellable()) {
            double current = plugin.getPriceManager().currentSellPrice(material, item.getBaseSell());
            String sellLine = "&cEladas: &f" + plugin.getShopManager().formatPrice(current) + " &7/ db";
            lore.add(Text.color(sellLine));
            if (plugin.getPriceManager().isDiscounted(material)) {
                lore.add(Text.color("&8(alap: " + plugin.getShopManager().formatPrice(item.getBaseSell()) + ")"));
                lore.add(plugin.getMessages().raw("price-dropped"));
            }
        } else {
            lore.add(Text.color("&cEladas: &c nem elado"));
        }

        lore.add(Text.color("&8&m                          "));
        lore.add(Text.color("&eBal klikk: &7vasarlas 1 db"));
        lore.add(Text.color("&eShift + bal: &7vasarlas 1 stack"));
        lore.add(Text.color("&6Jobb klikk: &7eladas 1 db"));
        lore.add(Text.color("&6Shift + jobb: &7eladas mind"));

        return makeItem(material, "&f&l" + Text.pretty(material), lore);
    }

    // ---------------------------------------------------------------
    //  Segedek
    // ---------------------------------------------------------------

    private ItemStack makeItem(Material material, String name, List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    private ItemStack navItem(Material material, String name) {
        return makeItem(material, name, new ArrayList<>());
    }

    private ItemStack infoItem() {
        List<String> lore = new ArrayList<>();
        lore.add(Text.color("&7Udvozol a &b&lGeNe Shop&7!"));
        lore.add("");
        if (plugin.getPriceManager().isEnabled()) {
            lore.add(Text.color("&7Dinamikus arazas: &aBE"));
            lore.add(Text.color("&7Ha sokat adnak el egy itembol,"));
            lore.add(Text.color("&7annak ara picit beesik, majd"));
            lore.add(Text.color("&7nehany ora alatt visszaall."));
        } else {
            lore.add(Text.color("&7Dinamikus arazas: &cKI"));
        }
        lore.add("");
        lore.add(Text.color("&8Keszitette: negyes Gerii06"));
        return makeItem(Material.NETHER_STAR, "&b&lInformacio", lore);
    }

    private ItemStack filler() {
        ItemStack stack = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(" ");
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
