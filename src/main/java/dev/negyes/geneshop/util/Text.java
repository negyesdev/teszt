package dev.negyes.geneshop.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * Apro szoveg-segedfuggvenyek.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public final class Text {

    private Text() {
    }

    /** Atalakitja a &-kodos szineket valos szinekke. */
    public static String color(String input) {
        if (input == null) {
            return "";
        }
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    /**
     * Szepen olvashatova teszi egy Material nevet.
     * Pl. GRASS_BLOCK -> "Grass Block"
     */
    public static String pretty(Material material) {
        String[] parts = material.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            sb.append(Character.toUpperCase(part.charAt(0)))
              .append(part.substring(1))
              .append(' ');
        }
        return sb.toString().trim();
    }
}
