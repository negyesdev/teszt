package dev.negyes.geneshop.shop;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Egy shop kategoria (sajat, akar tobboldalas ablak).
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class Shop {

    private final String id;
    private final String titleTemplate; // tartalmazhat %page%-et
    private final int size;
    private final ItemStack fillItem;
    private final List<ShopEntry> entries = new ArrayList<>();
    private int pages = 1;

    public Shop(String id, String titleTemplate, int size, ItemStack fillItem) {
        this.id = id;
        this.titleTemplate = titleTemplate;
        this.size = size;
        this.fillItem = fillItem;
    }

    public void addEntry(ShopEntry entry) {
        entries.add(entry);
        if (entry.getPage() > pages) {
            pages = entry.getPage();
        }
    }

    public String getId() {
        return id;
    }

    public String getTitleTemplate() {
        return titleTemplate;
    }

    public int getSize() {
        return size;
    }

    public ItemStack getFillItem() {
        return fillItem;
    }

    public List<ShopEntry> getEntries() {
        return entries;
    }

    public int getPages() {
        return pages;
    }
}
