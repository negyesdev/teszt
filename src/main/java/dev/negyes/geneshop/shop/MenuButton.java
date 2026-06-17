package dev.negyes.geneshop.shop;

import org.bukkit.inventory.ItemStack;

/**
 * Egy gomb a fomenuben, ami egy shopot nyit meg.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class MenuButton {

    private final ItemStack icon;
    private final String targetShopId;
    private final int slot;

    public MenuButton(ItemStack icon, String targetShopId, int slot) {
        this.icon = icon;
        this.targetShopId = targetShopId;
        this.slot = slot;
    }

    public ItemStack getIcon() {
        return icon;
    }

    public String getTargetShopId() {
        return targetShopId;
    }

    public int getSlot() {
        return slot;
    }
}
