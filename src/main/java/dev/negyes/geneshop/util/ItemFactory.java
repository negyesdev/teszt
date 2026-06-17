package dev.negyes.geneshop.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionType;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ItemStack-ek epitese a config szekciokbol (ShopGUI+ formatum).
 * Tartalmazza a regi nevek -> 1.21.8 leftekepezeseket is.
 *
 * GeNe Shop - keszitette: negyes Gerii06
 */
public final class ItemFactory {

    private static final Pattern SKIN_URL = Pattern.compile("\"url\"\\s*:\\s*\"(.*?)\"");

    /** Regi (ShopGUI+) enchantment nevek -> vanilla kulcsok. */
    private static final Map<String, String> ENCHANT_KEYS = new HashMap<>();
    /** Regi EntityType nevek -> 1.21 nevek. */
    private static final Map<String, String> ENTITY_ALIASES = new HashMap<>();
    /** Regi potion nevek -> 1.21 PotionType alap. */
    private static final Map<String, String> POTION_BASE = new HashMap<>();

    static {
        ENCHANT_KEYS.put("PROTECTION", "protection");
        ENCHANT_KEYS.put("PROTECTION_FIRE", "fire_protection");
        ENCHANT_KEYS.put("PROTECTION_FALL", "feather_falling");
        ENCHANT_KEYS.put("BLAST_PROTECTION", "blast_protection");
        ENCHANT_KEYS.put("PROJECTILE_PROTECTION", "projectile_protection");
        ENCHANT_KEYS.put("RESPIRATION", "respiration");
        ENCHANT_KEYS.put("AQUA_AFFINITY", "aqua_affinity");
        ENCHANT_KEYS.put("THORNS", "thorns");
        ENCHANT_KEYS.put("DEPTH_STRIDER", "depth_strider");
        ENCHANT_KEYS.put("FROST_WALKER", "frost_walker");
        ENCHANT_KEYS.put("SHARPNESS", "sharpness");
        ENCHANT_KEYS.put("SMITE", "smite");
        ENCHANT_KEYS.put("BANE_OF_ARTHROPODS", "bane_of_arthropods");
        ENCHANT_KEYS.put("KNOCKBACK", "knockback");
        ENCHANT_KEYS.put("FIRE_ASPECT", "fire_aspect");
        ENCHANT_KEYS.put("LOOTING", "looting");
        ENCHANT_KEYS.put("SWEEPING_EDGE", "sweeping_edge");
        ENCHANT_KEYS.put("EFFICIENCY", "efficiency");
        ENCHANT_KEYS.put("SILK_TOUCH", "silk_touch");
        ENCHANT_KEYS.put("UNBREAKING", "unbreaking");
        ENCHANT_KEYS.put("FORTUNE", "fortune");
        ENCHANT_KEYS.put("POWER", "power");
        ENCHANT_KEYS.put("PUNCH", "punch");
        ENCHANT_KEYS.put("FLAME", "flame");
        ENCHANT_KEYS.put("INFINITY", "infinity");
        ENCHANT_KEYS.put("LUCK_OF_THE_SEA", "luck_of_the_sea");
        ENCHANT_KEYS.put("LURE", "lure");
        ENCHANT_KEYS.put("MENDING", "mending");
        ENCHANT_KEYS.put("IMPALING", "impaling");
        ENCHANT_KEYS.put("RIPTIDE", "riptide");
        ENCHANT_KEYS.put("QUICK_CHARGE", "quick_charge");
        ENCHANT_KEYS.put("PIERCING", "piercing");

        ENTITY_ALIASES.put("MUSHROOM_COW", "MOOSHROOM");
        ENTITY_ALIASES.put("SNOWMAN", "SNOW_GOLEM");
        ENTITY_ALIASES.put("ZOMBIE_PIGMAN", "ZOMBIFIED_PIGLIN");

        POTION_BASE.put("REGEN", "REGENERATION");
        POTION_BASE.put("SPEED", "SWIFTNESS");
        POTION_BASE.put("STRENGTH", "STRENGTH");
        POTION_BASE.put("INSTANT_HEAL", "HEALING");
        POTION_BASE.put("INSTANT_DAMAGE", "HARMING");
        POTION_BASE.put("JUMP", "LEAPING");
        POTION_BASE.put("SLOWNESS", "SLOWNESS");
        POTION_BASE.put("SLOW_FALLING", "SLOW_FALLING");
        POTION_BASE.put("FIRE_RESISTANCE", "FIRE_RESISTANCE");
        POTION_BASE.put("INVISIBILITY", "INVISIBILITY");
        POTION_BASE.put("NIGHT_VISION", "NIGHT_VISION");
        POTION_BASE.put("POISON", "POISON");
        POTION_BASE.put("WATER_BREATHING", "WATER_BREATHING");
        POTION_BASE.put("WEAKNESS", "WEAKNESS");
    }

    private ItemFactory() {
    }

    /**
     * Felepit egy ItemStack-et egy "item" config szekciobol.
     * Tamogatja: material, quantity, name, lore, flags, skin, mob, potion.
     */
    public static ItemStack build(ConfigurationSection section, Logger logger) {
        if (section == null) {
            return new ItemStack(Material.STONE);
        }
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null || !material.isItem()) {
            if (logger != null) {
                logger.warning("Ervenytelen material: " + section.getString("material"));
            }
            material = Material.STONE;
        }
        int amount = section.getInt("quantity", section.getInt("amount", 1));
        if (amount < 1) {
            amount = 1;
        }
        ItemStack stack = new ItemStack(material, amount);
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return stack;
        }

        if (section.contains("name")) {
            meta.setDisplayName(Text.color(section.getString("name")));
        }
        if (section.contains("lore")) {
            List<String> lore = new ArrayList<>();
            for (String line : section.getStringList("lore")) {
                lore.add(Text.color(line));
            }
            meta.setLore(lore);
        }
        for (String flag : section.getStringList("flags")) {
            try {
                meta.addItemFlags(ItemFlag.valueOf(flag.toUpperCase()));
            } catch (IllegalArgumentException ignored) {
                // ervenytelen flag - kihagyjuk
            }
        }

        // Player head textura
        if (material == Material.PLAYER_HEAD && section.isString("skin") && meta instanceof SkullMeta skull) {
            applySkin(skull, section.getString("skin"));
        }
        // Spawner mob
        if (material == Material.SPAWNER && section.isString("mob") && meta instanceof BlockStateMeta blockMeta) {
            applyMob(blockMeta, section.getString("mob"), logger);
        }
        // Potion
        if (section.isConfigurationSection("potion") && meta instanceof PotionMeta potionMeta) {
            applyPotion(potionMeta, section.getConfigurationSection("potion"));
        }

        stack.setItemMeta(meta);
        return stack;
    }

    private static void applySkin(SkullMeta meta, String base64) {
        try {
            String json = new String(Base64.getDecoder().decode(base64));
            Matcher matcher = SKIN_URL.matcher(json);
            if (!matcher.find()) {
                return;
            }
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(URI.create(matcher.group(1)).toURL());
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);
        } catch (Exception ignored) {
            // hibas skin -> sima fej
        }
    }

    private static void applyMob(BlockStateMeta meta, String mobName, Logger logger) {
        EntityType type = resolveEntity(mobName);
        if (type == null) {
            if (logger != null) {
                logger.warning("Ismeretlen mob a spawnerhez: " + mobName);
            }
            return;
        }
        try {
            CreatureSpawner spawner = (CreatureSpawner) meta.getBlockState();
            spawner.setSpawnedType(type);
            meta.setBlockState(spawner);
        } catch (Exception ignored) {
            // nem allithato
        }
    }

    private static void applyPotion(PotionMeta meta, ConfigurationSection potion) {
        String rawType = potion.getString("type", "");
        int level = potion.getInt("level", 1);
        boolean extended = potion.getBoolean("extended", false);
        String base = POTION_BASE.getOrDefault(rawType.toUpperCase(), rawType.toUpperCase());

        PotionType resolved = resolvePotionType(base, level, extended);
        if (resolved != null) {
            try {
                meta.setBasePotionType(resolved);
            } catch (Exception ignored) {
                // nem allithato - marad sima uveg
            }
        }
    }

    private static PotionType resolvePotionType(String base, int level, boolean extended) {
        // Probaljuk a STRONG_/LONG_ valtozatokat, majd az alapot.
        List<String> candidates = new ArrayList<>();
        if (level >= 2) {
            candidates.add("STRONG_" + base);
        }
        if (extended) {
            candidates.add("LONG_" + base);
        }
        candidates.add(base);
        for (String name : candidates) {
            try {
                return PotionType.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                // probaljuk a kovetkezot
            }
        }
        return null;
    }

    /** Feloldja a regi/uj EntityType nevet. */
    public static EntityType resolveEntity(String name) {
        if (name == null) {
            return null;
        }
        String upper = name.toUpperCase();
        try {
            return EntityType.valueOf(upper);
        } catch (IllegalArgumentException ignored) {
            String alias = ENTITY_ALIASES.get(upper);
            if (alias != null) {
                try {
                    return EntityType.valueOf(alias);
                } catch (IllegalArgumentException ignored2) {
                    return null;
                }
            }
            return null;
        }
    }

    /** Feloldja a regi (ShopGUI+) enchantment nevet 1.21-es Enchantment-re. */
    public static Enchantment resolveEnchantment(String legacyName) {
        if (legacyName == null) {
            return null;
        }
        String key = ENCHANT_KEYS.get(legacyName.toUpperCase());
        if (key == null) {
            key = legacyName.toLowerCase();
        }
        try {
            return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(key));
        } catch (Exception ignored) {
            return null;
        }
    }
}
