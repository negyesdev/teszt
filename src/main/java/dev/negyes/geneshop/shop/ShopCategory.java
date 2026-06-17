package dev.negyes.geneshop.shop;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Egy shop kategoria (sajat ablakot kap a GUI-ban).
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopCategory {

    private final String id;
    private final String display;
    private final Material icon;
    private final int slot;
    private final List<ShopItem> items = new ArrayList<>();

    public ShopCategory(String id, String display, Material icon, int slot) {
        this.id = id;
        this.display = display;
        this.icon = icon;
        this.slot = slot;
    }

    public void addItem(ShopItem item) {
        items.add(item);
    }

    public String getId() {
        return id;
    }

    public String getDisplay() {
        return display;
    }

    public Material getIcon() {
        return icon;
    }

    public int getSlot() {
        return slot;
    }

    public List<ShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }
}
