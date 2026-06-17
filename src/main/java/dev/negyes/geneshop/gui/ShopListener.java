package dev.negyes.geneshop.gui;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.Shop;
import dev.negyes.geneshop.shop.ShopEntry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

/**
 * A shop GUI klikkjeinek feldolgozasa.
 *
 * FONTOS: minden, a shop ablakban tortent klikket/drag-et leallitunk (cancel),
 * igy a jatekos SEMMIT nem tud kivenni vagy berakni - csak a vetel/eladas fut.
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
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked == null || !clicked.equals(event.getView().getTopInventory())) {
            return;
        }

        int slot = event.getSlot();
        if (holder.getType() == ShopHolder.Type.MAIN) {
            handleMain(player, holder, slot);
        } else {
            handleShop(player, holder, slot, event.getClick());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof ShopHolder) {
            event.setCancelled(true);
        }
    }

    private void handleMain(Player player, ShopHolder holder, int slot) {
        String shopId = holder.shopAt(slot);
        if (shopId == null) {
            return;
        }
        Shop shop = plugin.getShopManager().getShop(shopId);
        if (shop == null) {
            plugin.getLang().send(player, "MSG.INVALIDSHOP", "%shop%", shopId);
            return;
        }
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
        plugin.getShopGUI().openShop(player, shop, 1);
    }

    private void handleShop(Player player, ShopHolder holder, int slot, ClickType click) {
        ShopHolder.Nav nav = holder.navAt(slot);
        if (nav != null) {
            Shop shop = plugin.getShopManager().getShop(holder.getShopId());
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.6f, 1.2f);
            switch (nav) {
                case BACK -> plugin.getShopGUI().openMain(player);
                case PREV -> {
                    if (shop != null) {
                        plugin.getShopGUI().openShop(player, shop, holder.getPage() - 1);
                    }
                }
                case NEXT -> {
                    if (shop != null) {
                        plugin.getShopGUI().openShop(player, shop, holder.getPage() + 1);
                    }
                }
            }
            return;
        }

        ShopEntry entry = holder.entryAt(slot);
        if (entry == null) {
            return;
        }

        // Klikk -> akcio a config clickActions alapjan.
        var actions = plugin.getShopManager().getClickActions();
        String action = actions.get(click.name());
        boolean fullStack = false;
        if (action == null && click == ClickType.SHIFT_LEFT) {
            action = actions.get("LEFT");
            fullStack = true;
        }
        if (action == null && click == ClickType.SHIFT_RIGHT) {
            action = actions.get("RIGHT");
        }
        if (action == null) {
            return;
        }

        switch (action) {
            case "BUY" -> plugin.getShopManager().buy(player, entry, fullStack);
            case "SELL" -> plugin.getShopManager().sell(player, entry, false);
            case "SELL_ALL" -> plugin.getShopManager().sell(player, entry, true);
            default -> {
                return;
            }
        }
        // Az arak valtozhattak (eladas) -> frissitjuk a megjelenitest.
        plugin.getShopGUI().refresh(player, holder);
    }
}
