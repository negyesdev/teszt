package dev.negyes.geneshop.command;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.ShopCategory;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A /shop parancs.
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class ShopCommand implements CommandExecutor, TabCompleter {

    private final GeNeShop plugin;

    public ShopCommand(GeNeShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessages().send(sender, "players-only");
            return true;
        }
        if (!player.hasPermission("geneshop.use")) {
            plugin.getMessages().send(player, "no-permission");
            return true;
        }

        if (args.length >= 1) {
            ShopCategory category = plugin.getShopManager().getCategory(args[0]);
            if (category == null) {
                plugin.getMessages().send(player, "unknown-category", "%category%", args[0]);
                return true;
            }
            plugin.getShopGUI().openCategory(player, category, 0);
            return true;
        }

        plugin.getShopGUI().openMain(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (ShopCategory category : plugin.getShopManager().getCategories()) {
                if (category.getId().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(category.getId());
                }
            }
        }
        return result;
    }
}
