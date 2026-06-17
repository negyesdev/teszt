package dev.negyes.geneshop.command;

import dev.negyes.geneshop.GeNeShop;
import dev.negyes.geneshop.shop.ShopCategory;
import dev.negyes.geneshop.shop.ShopItem;
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
            plugin.getMessages().send(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadAll();
                plugin.getMessages().send(sender, "reloaded");
            }
            case "prices" -> showPrices(sender);
            default -> sendHelp(sender);
        }
        return true;
    }

    private void showPrices(CommandSender sender) {
        sender.sendMessage(Text.color("&8&m                                        "));
        sender.sendMessage(Text.color("&b&lGeNe Shop &7- jelenleg beesett arak:"));
        boolean any = false;
        for (ShopCategory category : plugin.getShopManager().getCategories()) {
            for (ShopItem item : category.getItems()) {
                if (!item.isSellable()) {
                    continue;
                }
                if (plugin.getPriceManager().isDiscounted(item.getMaterial())) {
                    any = true;
                    double current = plugin.getPriceManager()
                            .currentSellPrice(item.getMaterial(), item.getBaseSell());
                    int percent = (int) Math.round(
                            plugin.getPriceManager().getMultiplier(item.getMaterial()) * 100);
                    sender.sendMessage(Text.color("&7- &f" + Text.pretty(item.getMaterial())
                            + " &7eladas: &e" + plugin.getShopManager().formatPrice(current)
                            + " &8(" + percent + "% / alap: "
                            + plugin.getShopManager().formatPrice(item.getBaseSell()) + ")"));
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
        sender.sendMessage(Text.color("&b&lGeNe Shop &7admin parancsok:"));
        sender.sendMessage(Text.color("&e/geneshop reload &7- ujratolti a configot"));
        sender.sendMessage(Text.color("&e/geneshop prices &7- listazza a beesett arakat"));
        sender.sendMessage(Text.color("&8Keszitette: negyes Gerii06"));
        sender.sendMessage(Text.color("&8&m                                        "));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            for (String sub : List.of("reload", "prices")) {
                if (sub.startsWith(prefix)) {
                    result.add(sub);
                }
            }
        }
        return result;
    }
}
