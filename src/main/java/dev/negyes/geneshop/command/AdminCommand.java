package dev.negyes.geneshop.command;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.Shop;
import dev.negyes.geneshop.shop.ShopEntry;
import dev.negyes.geneshop.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A /geneshop admin parancs (reload, prices).
 * GeNe Shop - keszitette: negyes Gerii06
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    private final GeNeShop plugin;

    public AdminCommand(GeNeShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("geneshop.admin")) {
            plugin.getLang().send(sender, "MSG.NOACCESS");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadAll();
                plugin.getLang().send(sender, "MSG.RELOADED");
            }
            case "prices" -> showPrices(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void showPrices(CommandSender sender) {
        sender.sendMessage(Text.color("&8&m                                        "));
        sender.sendMessage(Text.color("#B84DD3GeNe Shop &7- jelenleg beesett eladasi arak:"));
        boolean any = false;
        for (Shop shop : plugin.getShopManager().getShops().values()) {
            for (ShopEntry entry : shop.getEntries()) {
                if (!entry.isSellable()) {
                    continue;
                }
                if (plugin.getPriceManager().isDiscounted(entry.getMaterial())) {
                    any = true;
                    double mult = plugin.getPriceManager().getMultiplier(entry.getMaterial());
                    double current = entry.getSellPrice() * mult;
                    sender.sendMessage(Text.color("&7- &f" + entry.getPlainName()
                            + " &8(" + shop.getId() + ") &7-> &e"
                            + plugin.getEconomyHook().format(current)
                            + " &8(" + Math.round(mult * 100) + "%)"));
                }
            }
        }
        if (!any) {
            sender.sendMessage(Text.color("&7Jelenleg minden item az alap aran van. &a✔"));
        }
        sender.sendMessage(Text.color("&8&m                                        "));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Text.color("&8&m                                        "));
        sender.sendMessage(Text.color("#B84DD3GeNe Shop &7admin parancsok:"));
        sender.sendMessage(Text.color("&e/geneshop reload &7- ujratolti a configot"));
        sender.sendMessage(Text.color("&e/geneshop prices &7- a beesett arak listaja"));
        sender.sendMessage(Text.color("&8Keszitette: negyes Gerii06"));
        sender.sendMessage(Text.color("&8&m                                        "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            for (String sub : List.of("reload", "prices")) {
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT))) {
                    result.add(sub);
                }
            }
        }
        return result;
    }
}
