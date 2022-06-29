package luxielx.echoban;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public final class EchoBan extends JavaPlugin implements Listener {
    public static MySql sql;

    public static String convertToDays(long ago) {
        if (ago == Long.MAX_VALUE) return " Permanent. ";
        long seconds = ago / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        String time;
        if (days <= 0) {
            time = hours % 24 + " hours " + minutes % 60 + " minutes " + seconds % 60 + " seconds";
        } else {
            time = days + " days ";
        }

        return time;
    }

    private static ItemStack skull(OfflinePlayer victim) throws SQLException {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setOwner(victim.getName());
        skullMeta.setDisplayName(ChatColor.RED + victim.getName() + " got reported " + sql.getReports(victim.getName()) + " times.");
        skull.setItemMeta(skullMeta);
        return skull;
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        ConfigManager.getInstance().setPlugin(this);
        try {
            sql = new MySql(this);
        } catch (IOException e) {
            getLogger().info("sql connection failed 1 ");
        } catch (SQLException e) {
            e.printStackTrace();
            getLogger().info("sql connection failed 2 ");
        } catch (ClassNotFoundException e) {
            getLogger().info("sql connection failed 3 ");
        }
    }

    @Override
    public void onDisable() {
        try {
            sql.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // /punish <player>
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (sender.hasPermission("echoban.ban")) {
                if (cmd.getName().equalsIgnoreCase("punish")) {
                    if (args.length > 0) {
                        if (Bukkit.getOfflinePlayer(args[0]) != null) {
                            OfflinePlayer victim = Bukkit.getOfflinePlayer(args[0]);
                            try {
                                if (!sql.isExist(victim.getName())) {
                                    sql.addInfo(victim.getName(), 0l, 0l, "", 0, 0l, 0l, "");
                                }
                                openBanGUI(p, victim);

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
                if (cmd.getName().equalsIgnoreCase("unpunish")) {
                    if (args.length > 0) {
                        if (Bukkit.getOfflinePlayer(args[0]) != null) {
                            OfflinePlayer victim = Bukkit.getOfflinePlayer(args[0]);
                            try {
                                if (!sql.isExist(victim.getName())) {
                                    sql.addInfo(victim.getName(), 0l, 0l, "", 0, 0l, 0l, "");
                                }
                                unban(victim);
                                p.sendMessage(ChatColor.RED+"You unbanned " + victim.getName());

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
                if (cmd.getName().equalsIgnoreCase("unmute")) {
                    if (args.length > 0) {
                        if (Bukkit.getOfflinePlayer(args[0]) != null) {
                            OfflinePlayer victim = Bukkit.getOfflinePlayer(args[0]);
                            try {
                                if (!sql.isExist(victim.getName())) {
                                    sql.addInfo(victim.getName(), 0l, 0l, "", 0, 0l, 0l, "");
                                }
                                unmute(victim);
                                p.sendMessage(ChatColor.RED+"You unmuted " + victim.getName());

                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
                if (cmd.getName().equalsIgnoreCase("reportgui")) {
                    try {
                        openReportGUI(p);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (cmd.getName().equalsIgnoreCase("report")) {
                if (args.length > 0) {
                    if (Bukkit.getOfflinePlayer(args[0]) != null) {
                        OfflinePlayer victim = Bukkit.getOfflinePlayer(args[0]);
                        try {
                            if (!sql.isExist(victim.getName())) {
                                sql.addInfo(victim.getName(), 0l, 0l, "", 0, 0l, 0l, "");
                            }
                            String reason = String.join(" ", (CharSequence[]) ArrayUtils.remove(args, 0));
                            report(p, victim, reason);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return false;
    }

    public ItemStack banItem(OfflinePlayer victim, String time) {
        ItemStack is = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        im.setDisplayName(ChatColor.RED + "Ban " + victim.getName() + " for " + time);
        is.setItemMeta(im);
        is.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return is;
    }

    public ItemStack muteItem(OfflinePlayer victim, String time) {
        ItemStack is = new ItemStack(Material.STAINED_CLAY, 1, (short) 4);
        ItemMeta im = is.getItemMeta();
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        im.setDisplayName(ChatColor.RED + "Mute " + victim.getName() + " for " + time);
        is.setItemMeta(im);
        is.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 1);
        return is;
    }

    @EventHandler
    public void click(InventoryClickEvent e) throws SQLException {
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getHolder() instanceof holder)) return;
        e.setCancelled(true);
        if (!e.getClickedInventory().getTitle().contains("Punish")) return;
        Player p = (Player) e.getWhoClicked();
        String title = ChatColor.stripColor(e.getClickedInventory().getTitle());
        String v = title.split(" ")[1];
        if (Bukkit.getOfflinePlayer(v) == null) {
            p.closeInventory();
            return;
        }
        OfflinePlayer victim = Bukkit.getOfflinePlayer(v);
        switch (e.getSlot()) {
            case (0):
                ban(victim, p.getName(), timeConvert("14D"));
                break;
            case (1):
                ban(victim, p.getName(), timeConvert("1M"));
                break;
            case (2):
                ban(victim, p.getName(), timeConvert("3M"));
                break;
            case (3):
                ban(victim, p.getName(), timeConvert("6M"));
                break;
            case (4):
                ban(victim, p.getName(), timeConvert("1Y"));
                break;
            case (5):
                ban(victim, p.getName(), timeConvert("PERM"));
                break;

            case (9):
                mute(victim, p.getName(), timeConvert("7D"));
                break;
            case (10):
                mute(victim, p.getName(), timeConvert("14D"));
                break;
            case (11):
                mute(victim, p.getName(), timeConvert("1M"));
                break;
            case (12):
                mute(victim, p.getName(), timeConvert("3M"));
                break;
            case (13):
                mute(victim, p.getName(), timeConvert("6M"));
                break;
            case (14):
                mute(victim, p.getName(), timeConvert("1Y"));
                break;

        }
    }

    private void openBanGUI(Player p, OfflinePlayer victim) throws SQLException {
        Inventory inv = Bukkit.createInventory(new holder(), 18, "Punish " + ChatColor.RED + victim.getName());
        inv.setItem(0, banItem(victim, "14D"));
        inv.setItem(1, banItem(victim, "1M"));
        inv.setItem(2, banItem(victim, "3M"));
        inv.setItem(3, banItem(victim, "6M"));
        inv.setItem(4, banItem(victim, "1Y"));
        inv.setItem(5, banItem(victim, "Perm"));
        inv.setItem(8, skull(victim));
        inv.setItem(9, muteItem(victim, "7D"));
        inv.setItem(10, muteItem(victim, "14D"));
        inv.setItem(11, muteItem(victim, "1M"));
        inv.setItem(12, muteItem(victim, "3M"));
        inv.setItem(13, muteItem(victim, "6M"));
        inv.setItem(14, muteItem(victim, "1Y"));
        p.openInventory(inv);
    }

    public void report(Player reporter, OfflinePlayer victim, String reason) throws SQLException {
        sql.updateReports(victim.getName(), sql.getReports(victim.getName()) + 1);
        reporter.sendMessage(ChatColor.RED + victim.getName() + " has been reported for " + reason + ".");

    }

    public void ban(OfflinePlayer victim, String punisher, long period) throws SQLException {
        sql.updateBanPeriod(victim.getName(), period);
        sql.updateBanPunisher(victim.getName(), punisher);
        sql.updateBantime(victim.getName(), System.currentTimeMillis());
        if (victim.isOnline()) {
            ((Player) victim).kickPlayer(cc(ConfigManager.getConfig().getString("banmessage").replaceAll("%punisher%", punisher)
                    .replaceAll("%period%", convertToDays(period))));
        }
        Bukkit.broadcastMessage(ChatColor.RED + victim.getName() + " has been removed from the lobby for cheating.");

    }

    public void mute(OfflinePlayer victim, String punisher, long period) throws SQLException {
        sql.updateMutePeriod(victim.getName(), period);
        sql.updateMutePunisher(victim.getName(), punisher);
        sql.updateMutetime(victim.getName(), System.currentTimeMillis());
        if (victim.isOnline()) {
            ((Player) victim).sendMessage(cc(ConfigManager.getConfig().getString("banmessage").replaceAll("%punisher%", punisher)
                    .replaceAll("%period%", convertToDays(period))));
        }

    }
    public void unban(OfflinePlayer victim) throws SQLException {
        sql.updateBanPeriod(victim.getName(), 0);
        sql.updateBanPunisher(victim.getName(), "");
        sql.updateBantime(victim.getName(),0);


    }

    public void unmute(OfflinePlayer victim) throws SQLException {
        sql.updateMutePeriod(victim.getName(), 0);
        sql.updateMutePunisher(victim.getName(), "");
        sql.updateMutetime(victim.getName(), 0);


    }
    public String cc(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    // Y M D
    public long timeConvert(String duration) {
        if (duration.contains("PERM")) return Long.MAX_VALUE;
        int year = 0, month = 0, days = 0;
        if (duration.contains("Y")) {
            String sec = duration.split("Y")[0];
            while (!sec.matches("-?\\d+")) {
                sec = sec.substring(1);
            }
            year = Integer.valueOf(sec);
        }
        if (duration.contains("M")) {
            String sec = duration.split("M")[0];
            while (!sec.matches("-?\\d+")) {
                sec = sec.substring(1);
            }
            month = Integer.valueOf(sec);
        }
        if (duration.contains("D")) {
            String sec = duration.split("D")[0];
            while (!sec.matches("-?\\d+")) {
                sec = sec.substring(1);
            }
            days = Integer.valueOf(sec);
        }

        long time = year * 365 * TimeUnit.DAYS.toMillis(1) + month * 30 * TimeUnit.DAYS.toMillis(1) + TimeUnit.DAYS.toMillis(days);
        return time;
    }
    public static void openReportGUI(Player p) throws SQLException {

        Inventory inv = Bukkit.createInventory(new holder(), 54, ChatColor.RED+"Reported Players");
        int index = 0;
        for (String name : sql.sort()) {
            if(index > 53) break;
            inv.setItem(index, skull(Bukkit.getOfflinePlayer(name)));
            index++;
        }
        p.openInventory(inv);


    }

}

class holder implements InventoryHolder {

    @Override
    public Inventory getInventory() {
        return null;
    }
}