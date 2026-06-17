package dev.negyes.geneshop.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Szoveg-segedfuggvenyek: hex (#RRGGBB) + legacy (&) szinek, nevek szepitese.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public final class Text {

    private static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");

    private Text() {
    }

    /** Atalakitja a #RRGGBB hex es a &-kodos szineket valos szinekke. */
    public static String color(String input) {
        if (input == null) {
            return "";
        }
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String hex = matcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    /** Eltavolitja a szineket (uzenetekbe agyazott nevekhez). */
    public static String strip(String input) {
        return ChatColor.stripColor(color(input));
    }

    /** Szepen olvashatova teszi egy Material nevet: GRASS_BLOCK -> "Grass Block". */
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
