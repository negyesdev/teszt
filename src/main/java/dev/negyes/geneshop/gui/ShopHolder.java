package dev.negyes.geneshop.gui;

import dev.negyes.geneshop.shop.ShopItem;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

/**
 * Sajat InventoryHolder, ami egyertelmuen azonositja a GeNe Shop ablakokat,
 * es eltarolja, hogy melyik slotban mi talalhato. Ezert nem kell a
 * megjelenitett item-re hagyatkozni a klikk feldolgozasakor (biztonsagosabb).
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopHolder implements InventoryHolder {

    public enum Type {
        MAIN,
        CATEGORY
    }

    private final Type type;
    private final String categoryId;
    private final int page;

    private final Map<Integer, ShopItem> slotItems = new HashMap<>();
    private final Map<Integer, String> slotCategories = new HashMap<>();
    private final Map<Integer, NavAction> slotNav = new HashMap<>();

    private Inventory inventory;

    public ShopHolder(Type type, String categoryId, int page) {
        this.type = type;
        this.categoryId = categoryId;
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

    public String getCategoryId() {
        return categoryId;
    }

    public int getPage() {
        return page;
    }

    public void mapItem(int slot, ShopItem item) {
        slotItems.put(slot, item);
    }

    public void mapCategory(int slot, String id) {
        slotCategories.put(slot, id);
    }

    public void mapNav(int slot, NavAction action) {
        slotNav.put(slot, action);
    }

    public ShopItem itemAt(int slot) {
        return slotItems.get(slot);
    }

    public String categoryAt(int slot) {
        return slotCategories.get(slot);
    }

    public NavAction navAt(int slot) {
        return slotNav.get(slot);
    }
}
