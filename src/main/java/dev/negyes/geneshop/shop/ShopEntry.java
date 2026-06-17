package dev.negyes.geneshop.shop;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

/**
 * Egy elem egy shop oldalon: vagy egy targy (ITEM), egy varazskonyv
 * (ENCHANTMENT), vagy a balance kijelzo (BALANCE).
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopEntry {

    public enum Kind {
        ITEM,
        ENCHANTMENT,
        BALANCE
    }

    private final Kind kind;
    private final int slot;
    private final int page;

    // ITEM / ENCHANTMENT
    private ItemStack giveStack;   // amit a jatekos kap vetelnel
    private String displayName;    // szinezett nev az ikonhoz/uzenetekhez
    private String plainName;      // szin nelkuli nev az uzenetekbe agyazva
    private boolean customName = false; // van-e configbol megadott nev
    private double buyPrice = -1;  // -1 = nem veheto
    private Double sellPrice = null; // null = nem elado
    private boolean sellable = false;

    // ENCHANTMENT
    private Enchantment enchantment;
    private int enchantLevel = 1;

    public ShopEntry(Kind kind, int slot, int page) {
        this.kind = kind;
        this.slot = slot;
        this.page = page;
    }

    public Kind getKind() {
        return kind;
    }

    public int getSlot() {
        return slot;
    }

    public int getPage() {
        return page;
    }

    public ItemStack getGiveStack() {
        return giveStack;
    }

    public void setGiveStack(ItemStack giveStack) {
        this.giveStack = giveStack;
    }

    public Material getMaterial() {
        return giveStack == null ? Material.AIR : giveStack.getType();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPlainName() {
        return plainName;
    }

    public void setPlainName(String plainName) {
        this.plainName = plainName;
    }

    public boolean hasCustomName() {
        return customName;
    }

    public void setCustomName(boolean customName) {
        this.customName = customName;
    }

    public double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public boolean isBuyable() {
        return buyPrice >= 0;
    }

    public Double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public boolean isSellable() {
        return sellable;
    }

    public void setSellable(boolean sellable) {
        this.sellable = sellable;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    public int getEnchantLevel() {
        return enchantLevel;
    }

    public void setEnchantLevel(int enchantLevel) {
        this.enchantLevel = enchantLevel;
    }

    /** Hany darab van egy "egyseg"-ben (a config quantity erteke). */
    public int getQuantity() {
        return giveStack == null ? 1 : Math.max(1, giveStack.getAmount());
    }
}
