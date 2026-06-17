package dev.negyes.geneshop.shop;

import org.bukkit.Material;

/**
 * Egy eladhato/megveheto item a shopban.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopItem {

    private final Material material;
    private final double buyPrice;   // -1 = nem veheto
    private final double baseSell;   // -1 = nem elado
    private final String categoryId;

    public ShopItem(Material material, double buyPrice, double baseSell, String categoryId) {
        this.material = material;
        this.buyPrice = buyPrice;
        this.baseSell = baseSell;
        this.categoryId = categoryId;
    }

    public Material getMaterial() {
        return material;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    /** Az alap (maximalis) eladasi ar - a dinamikus szorzo nelkul. */
    public double getBaseSell() {
        return baseSell;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public boolean isBuyable() {
        return buyPrice >= 0;
    }

    public boolean isSellable() {
        return baseSell >= 0;
    }
}
