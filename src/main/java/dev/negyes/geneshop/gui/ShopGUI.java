package dev.negyes.geneshop.gui;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.MenuButton;
import dev.negyes.geneshop.shop.Shop;
import dev.negyes.geneshop.shop.ShopEntry;
import dev.negyes.geneshop.util.ItemFactory;
import dev.negyes.geneshop.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * A shop GUI-k felepitese es megnyitasa (fomenu + tobboldalas shopok).
 * Az item ikonokon a %sell% ar mar a dinamikus (aktualis) arat mutatja.
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopGUI {

    private final GeNeShop plugin;

    public ShopGUI(GeNeShop plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------
    //  Fomenu
    // ---------------------------------------------------------------

    public void openMain(Player player) {
        var sm = plugin.getShopManager();
        ShopHolder holder = new ShopHolder(ShopHolder.Type.MAIN, null, 1);
        Inventory inv = Bukkit.createInventory(holder, sm.getMainSize(), sm.getMainTitle());
        holder.setInventory(inv);

        fill(inv, sm.getMainFill());

        for (MenuButton button : sm.getMenuButtons()) {
            int slot = button.getSlot();
            if (slot < 0 || slot >= inv.getSize()) {
                continue;
            }
            inv.setItem(slot, button.getIcon());
            holder.mapShop(slot, button.getTargetShopId());
        }

        player.openInventory(inv);
    }

    // ---------------------------------------------------------------
    //  Shop oldal
    // ---------------------------------------------------------------

    public void openShop(Player player, Shop shop, int page) {
        page = Math.max(1, Math.min(page, shop.getPages()));

        String title = Text.color(shop.getTitleTemplate().replace("%page%", String.valueOf(page)));
        ShopHolder holder = new ShopHolder(ShopHolder.Type.SHOP, shop.getId(), page);
        Inventory inv = Bukkit.createInventory(holder, shop.getSize(), title);
        holder.setInventory(inv);

        fill(inv, shop.getFillItem());

        for (ShopEntry entry : shop.getEntries()) {
            if (entry.getPage() != page) {
                continue;
            }
            int slot = entry.getSlot();
            if (slot < 0 || slot >= inv.getSize()) {
                continue;
            }
            if (entry.getKind() == ShopEntry.Kind.BALANCE) {
                inv.setItem(slot, buildBalance(player));
            } else {
                inv.setItem(slot, buildIcon(entry));
                holder.mapEntry(slot, entry);
            }
        }

        // Navigacio
        var sm = plugin.getShopManager();
        if (sm.getBackButton() != null && inBounds(sm.getBackSlot(), inv)) {
            inv.setItem(sm.getBackSlot(), sm.getBackButton());
            holder.mapNav(sm.getBackSlot(), ShopHolder.Nav.BACK);
        }
        if (page > 1 && sm.getPrevButton() != null && inBounds(sm.getPrevSlot(), inv)) {
            inv.setItem(sm.getPrevSlot(), sm.getPrevButton());
            holder.mapNav(sm.getPrevSlot(), ShopHolder.Nav.PREV);
        }
        if (page < shop.getPages() && sm.getNextButton() != null && inBounds(sm.getNextSlot(), inv)) {
            inv.setItem(sm.getNextSlot(), sm.getNextButton());
            holder.mapNav(sm.getNextSlot(), ShopHolder.Nav.NEXT);
        }

        player.openInventory(inv);
    }

    /** Egy nyitott shop oldal item ikonjainak helyben frissitese (eladas utan). */
    public void refresh(Player player, ShopHolder holder) {
        if (holder.getType() != ShopHolder.Type.SHOP || holder.getInventory() == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShop(holder.getShopId());
        if (shop == null) {
            return;
        }
        Inventory inv = holder.getInventory();
        int page = holder.getPage();
        for (ShopEntry entry : shop.getEntries()) {
            if (entry.getPage() != page) {
                continue;
            }
            int slot = entry.getSlot();
            if (slot < 0 || slot >= inv.getSize()) {
                continue;
            }
            if (entry.getKind() == ShopEntry.Kind.BALANCE) {
                inv.setItem(slot, buildBalance(player));
            } else {
                inv.setItem(slot, buildIcon(entry));
            }
        }
    }

    // ---------------------------------------------------------------
    //  Ikon epites
    // ---------------------------------------------------------------

    private ItemStack buildIcon(ShopEntry entry) {
        var sm = plugin.getShopManager();
        ItemStack icon = entry.getGiveStack().clone();
        icon.setAmount(Math.max(1, entry.getQuantity()));
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return icon;
        }
        meta.setDisplayName(entry.getDisplayName());

        Material material = entry.getMaterial();
        String buyStr = entry.isBuyable()
                ? plugin.getEconomyHook().format(entry.getBuyPrice())
                : sm.getUnbuyableText();

        String sellStr;
        if (entry.isSellable()) {
            double mult = plugin.getPriceManager().getMultiplier(material);
            sellStr = plugin.getEconomyHook().format(entry.getSellPrice() * mult);
        } else {
            sellStr = sm.getUnsellableText();
        }

        List<String> format = (entry.getKind() == ShopEntry.Kind.ENCHANTMENT)
                ? sm.getEnchantLoreFormat()
                : sm.getItemLoreFormat();

        List<String> lore = new ArrayList<>();
        for (String line : format) {
            if (line.contains("%sell%") && !entry.isSellable() && sm.isHideSellForUnsellable()) {
                continue;
            }
            if (line.contains("%buy%") && !entry.isBuyable() && sm.isHideBuyForUnbuyable()) {
                continue;
            }
            lore.add(Text.color(line.replace("%buy%", buyStr).replace("%sell%", sellStr)));
        }

        // GeNe Shop egyedi jelzes: ha az ar epp beesett.
        if (entry.isSellable()
                && plugin.getConfig().getBoolean("dynamic-pricing.show-discount-note", true)
                && plugin.getPriceManager().isDiscounted(material)) {
            lore.add(Text.color("#D3D3D3(Az ar most beesett, hamarosan visszaall.)"));
        }

        meta.setLore(lore);
        icon.setItemMeta(meta);
        return icon;
    }

    private ItemStack buildBalance(Player player) {
        var section = plugin.getShopManager().getBalanceItemSection();
        if (section == null) {
            return new ItemStack(Material.GOLD_INGOT);
        }
        ItemStack icon = ItemFactory.build(section, plugin.getLogger());
        String balance = plugin.getEconomyHook().format(plugin.getEconomyHook().balance(player));
        ItemMeta meta = icon.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                meta.setDisplayName(meta.getDisplayName().replace("%balance%", balance));
            }
            if (meta.hasLore() && meta.getLore() != null) {
                List<String> lore = new ArrayList<>();
                for (String line : meta.getLore()) {
                    lore.add(line.replace("%balance%", balance));
                }
                meta.setLore(lore);
            }
            icon.setItemMeta(meta);
        }
        return icon;
    }

    private void fill(Inventory inv, ItemStack fillItem) {
        if (fillItem == null) {
            return;
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, fillItem.clone());
        }
    }

    private boolean inBounds(int slot, Inventory inv) {
        return slot >= 0 && slot < inv.getSize();
    }
}
