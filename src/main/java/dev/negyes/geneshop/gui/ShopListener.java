package dev.negyes.geneshop.gui;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.ShopCategory;
import dev.negyes.geneshop.shop.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * A shop GUI klikkjeinek feldolgozasa.
 *
 * FONTOS: minden, a shop ablakban tortent klikket/drag-et leallitunk
 * (cancel), igy a jatekos SEMMIT nem tud kivenni vagy berakni - csak a
 * vetel/eladas logika fut le. Ettol "nem lehet kivenni belole semmit".
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopListener implements Listener {

    private final GeNeShop plugin;

    public ShopListener(GeNeShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof ShopHolder holder)) {
            return;
        }
        // A shop ablakban (es onnan ki/be) minden klikket letiltunk.
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory clicked = event.getClickedInventory();
        // Csak a felso (shop) inventory kattintasaira reagalunk.
        if (clicked == null || !clicked.equals(event.getView().getTopInventory())) {
            return;
        }

        int slot = event.getSlot();

        if (holder.getType() == ShopHolder.Type.MAIN) {
            handleMainClick(player, holder, slot);
        } else {
            handleCategoryClick(player, holder, slot, event);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ShopHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMainClick(Player player, ShopHolder holder, int slot) {
        String categoryId = holder.categoryAt(slot);
        if (categoryId == null) {
            return;
        }
        ShopCategory category = plugin.getShopManager().getCategory(categoryId);
        if (category != null) {
            plugin.getShopGUI().openCategory(player, category, 0);
        }
    }

    private void handleCategoryClick(Player player, ShopHolder holder, int slot, InventoryClickEvent event) {
        NavAction nav = holder.navAt(slot);
        if (nav != null) {
            handleNav(player, holder, nav);
            return;
        }

        ShopItem item = holder.itemAt(slot);
        if (item == null) {
            return;
        }

        switch (event.getClick()) {
            case LEFT -> plugin.getShopManager().buy(player, item, 1);
            case SHIFT_LEFT -> plugin.getShopManager().buy(player, item, item.getMaterial().getMaxStackSize());
            case RIGHT -> plugin.getShopManager().sell(player, item, 1);
            case SHIFT_RIGHT -> plugin.getShopManager().sell(player, item, -1);
            default -> {
                return;
            }
        }
        // Az arak valtozhattak (eladas) -> frissitjuk a megjelenitest.
        plugin.getShopGUI().refreshItems(holder);
    }

    private void handleNav(Player player, ShopHolder holder, NavAction action) {
        ShopCategory category = plugin.getShopManager().getCategory(holder.getCategoryId());
        switch (action) {
            case BACK -> plugin.getShopGUI().openMain(player);
            case CLOSE -> player.closeInventory();
            case PREV_PAGE -> {
                if (category != null) {
                    plugin.getShopGUI().openCategory(player, category, holder.getPage() - 1);
                }
            }
            case NEXT_PAGE -> {
                if (category != null) {
                    plugin.getShopGUI().openCategory(player, category, holder.getPage() + 1);
                }
            }
            case INFO -> {
                // csak dekoracio
            }
        }
    }
}
