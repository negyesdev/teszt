package dev.negyes.geneshop.command;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.Shop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A /shop parancs (fomenu vagy konkret kategoria).
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
            plugin.getLang().send(sender, "MSG.INGAMEONLY");
            return true;
        }
        if (!player.hasPermission("geneshop.use")) {
            plugin.getLang().send(player, "MSG.NOACCESS");
            return true;
        }

        if (args.length >= 1) {
            Shop shop = plugin.getShopManager().getShop(args[0].toLowerCase(Locale.ROOT));
            if (shop == null) {
                plugin.getLang().send(player, "MSG.INVALIDSHOP", "%shop%", args[0]);
                return true;
            }
            plugin.getShopGUI().openShop(player, shop, 1);
            return true;
        }

        if (plugin.getShopManager().isMainMenuDisabled()) {
            plugin.getLang().send(player, "MSG.MAINMENUDISABLED");
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
            for (String id : plugin.getShopManager().getShops().keySet()) {
                if (id.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                    result.add(id);
                }
            }
        }
        return result;
    }
}
