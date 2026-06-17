package dev.negyes.geneshop.gui;

import dev.negyes.geneshop.shop.ShopEntry;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Sajat InventoryHolder, ami azonositja a GeNe Shop ablakokat es eltarolja
 * a slotok jelenteset (shop gomb / item / navigacio).
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopHolder implements InventoryHolder {

    public enum Type {
        MAIN,
        SHOP
    }

    public enum Nav {
        BACK,
        PREV,
        NEXT
    }

    private final Type type;
    private final String shopId;
    private final int page;

    private final Map<Integer, String> slotShop = new HashMap<>();
    private final Map<Integer, ShopEntry> slotEntry = new HashMap<>();
    private final Map<Integer, Nav> slotNav = new HashMap<>();

    private Inventory inventory;

    public ShopHolder(Type type, String shopId, int page) {
        this.type = type;
        this.shopId = shopId;
        this.page = page;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Type getType() {
        return type;
    }

    public String getShopId() {
        return shopId;
    }

    public int getPage() {
        return page;
    }

    public void mapShop(int slot, String id) {
        slotShop.put(slot, id);
    }

    public void mapEntry(int slot, ShopEntry entry) {
        slotEntry.put(slot, entry);
    }

    public void mapNav(int slot, Nav nav) {
        slotNav.put(slot, nav);
    }

    public String shopAt(int slot) {
        return slotShop.get(slot);
    }

    public ShopEntry entryAt(int slot) {
        return slotEntry.get(slot);
    }

    public Nav navAt(int slot) {
        return slotNav.get(slot);
    }
}
