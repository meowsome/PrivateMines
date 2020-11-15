package me.meowso.privatemines;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import java.io.IOException;
import java.util.List;

public class CommandHandler implements CommandExecutor {
    private PlacementHandler placementHandler;
    private DatabaseManager databaseManager;
    private PrivateMines privateMines;

    public CommandHandler(PrivateMines privateMines) {
        placementHandler = new PlacementHandler(privateMines);
        databaseManager = new DatabaseManager(privateMines);
        this.privateMines = privateMines;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FileConfiguration config = privateMines.getConfig();

        if (args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " Specify a sub-command");
            return true;
        }

        switch (args[0]) {
            case "erase":
                eraseMine(args, sender);
                return true;
            case "reload":
                reload(sender);
                return true;
            default:
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " Unrecognized sub-command");
                return true;
        }
    }

    public void eraseMine(String[] args, CommandSender sender) {
        FileConfiguration config = privateMines.getConfig();
        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " You need to specify an island's ID.");
            return;
        }

        try {
            String islandId = args[1];

            placementHandler.removeMines(databaseManager.getMines(islandId));

            try {
                List<String> mines = databaseManager.getMines(islandId);

                if (!mines.isEmpty()) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.GRAY + " Removing " + mines.size() + " mines on this island.");
                    databaseManager.removeMines(islandId);
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " 0 mines found for this island.");
                }
            } catch (IOException e) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " An error occurred with the database.");
                e.printStackTrace();
            }
        } catch (Exception err) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " An error occurred. Are you sure you used the right island ID?");
        }
    }

    private void reload(CommandSender sender) {
        FileConfiguration config = privateMines.getConfig();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.GRAY + " Reloading config.");
        privateMines.reloadConfig();
    }
}
