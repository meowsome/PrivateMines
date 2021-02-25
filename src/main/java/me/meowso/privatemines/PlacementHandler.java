package me.meowso.privatemines;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.BoundingBox;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.managers.IslandsManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PlacementHandler {
    private PrivateMines privateMines;
    private DatabaseManager databaseManager;
    private IslandsManager islandsManager;

    public PlacementHandler(PrivateMines privateMines) {
        this.privateMines = privateMines;
        databaseManager = new DatabaseManager(privateMines);
        islandsManager = BentoBox.getInstance().getIslands();
    }

    public int getDepth(ItemStack item) {
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("10x10x10")) return 10;
        else if (name.contains("15x15x15")) return 15;
        else if (name.contains("20x20x20")) return 20;
        else return 0;
    }

    public BoundingBox getMineBox(Player player, int depth) {
        Location coords = player.getLocation();
        int halfOfSize = depth / 2;

        double x = coords.getX();
        double y = coords.getY();
        double z = coords.getZ();

        return new BoundingBox(x - halfOfSize, y - halfOfSize, z - halfOfSize, x + halfOfSize, y + halfOfSize, z + halfOfSize);
    }


    public void showPlacementPreview(BoundingBox mineBox) {
        setParticlesWidth((int) mineBox.getMinX(), (int) mineBox.getWidthX(), (int) mineBox.getMinZ(), mineBox);
        setParticlesWidth((int) mineBox.getMinX(), (int) mineBox.getWidthX(), (int) mineBox.getMaxZ(), mineBox);
        setParticlesDepth((int) mineBox.getMinX(), (int) mineBox.getWidthZ(), (int) mineBox.getMinZ(), mineBox);
        setParticlesDepth((int) mineBox.getMaxX(), (int) mineBox.getWidthZ(), (int) mineBox.getMinZ(), mineBox);
    }

    public void setParticlesWidth(int x, int width, int z, BoundingBox mineBox) {
        for (int i = x; i < x + width; i++) {
            for (int j = (int) mineBox.getMinY(); j < mineBox.getMinY() + mineBox.getHeight(); j++) {
                Bukkit.getWorld("prison_cells").spawnParticle(Particle.BARRIER, i, j, z, 1);
            }
        }
    }

    public void setParticlesDepth(int x, int depth, int z, BoundingBox mineBox) {
        for (int i = z; i < z + depth; i++) {
            for (int j = (int) mineBox.getMinY(); j < mineBox.getMinY() + mineBox.getHeight(); j++) {
                Bukkit.getWorld("prison_cells").spawnParticle(Particle.BARRIER, x, j, i, 1);
            }
        }
    }

    public void placeMine(Player player, BoundingBox mineBox, ItemStack item) throws IOException {
        FileConfiguration config = privateMines.getConfig();
        String mineName = player.getName() + "_" + System.currentTimeMillis();
        ConfigurationSection mineInfo = config.getConfigurationSection("mines." + getDepth(item));
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String islandId = islandsManager.getIslandAt(player.getLocation()).get().getUniqueId();

        // Save to file
        databaseManager.addMine(islandId, mineName);

        // Actually place mine
        try {
            player.setOp(true);

            // Create mine
            player.performCommand("/pos1 " + (int) mineBox.getMinX() + "," + (int) mineBox.getMinY() + "," + (int) mineBox.getMinZ());
            player.performCommand("/pos2 " + (int) mineBox.getMaxX() + "," + (int) mineBox.getMaxY() + "," + (int) mineBox.getMaxZ());
            player.performCommand("mrl create " + mineName);

            // Get mine compositions from config
            Map<String, Object> compositions = mineInfo.getConfigurationSection("composition").getValues(false);
            if (compositions.isEmpty()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " An error occurred fetching the composition of blocks for this mine.");
                return;
            }
            for (Map.Entry<String,Object> block : compositions.entrySet()) {
                Bukkit.dispatchCommand(console, "mrl set " + mineName + " " + block.getKey() + " " + block.getValue() + "%");
            }

            // Set recent percent
            Bukkit.dispatchCommand(console, "mrl flag " + mineName + " resetPercent " + mineInfo.getDouble("resetPercent") + "%");
            // Reset mine to display it
            Bukkit.dispatchCommand(console, "mrl reset " + mineName);
        } catch (Exception err) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " An error occurred creating the mine.");
            throw err;
        } finally {
            player.setOp(false);
        }
    }

    public void removeItemFromInventory(Player player, ItemStack itemVoucher) {
        player.getInventory().removeItem(itemVoucher);
        player.updateInventory();
    }


    public void removeMines(List<String> mines) {
        for (Object mine : mines) {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

            Bukkit.dispatchCommand(console, "mrl erase " + mine);
        }
    }
}
