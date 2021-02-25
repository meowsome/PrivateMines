package me.meowso.privatemines;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.util.BoundingBox;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.managers.IslandsManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public class PlacementChecker {
    private ArrayList<String> previewPending;
    private HashMap<String, Long> clickCooldowns;
    private static final int cooldownTime = 1000; // Milliseconds
    private IslandsManager islandsManager;

    public PlacementChecker() {
        previewPending = new ArrayList<>();
        clickCooldowns = new HashMap<>();
        islandsManager = BentoBox.getInstance().getIslands();
    }

    public boolean isValidItem(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            String itemName = item.getItemMeta().getDisplayName();
            Repairable repairInfo = (Repairable) item.getItemMeta();
            return item.getType().equals(Material.NAME_TAG) && itemName.contains("Private Mine Voucher") && (itemName.contains("10x10x10") || itemName.contains("15x15x15") || itemName.contains("20x20x20")) && repairInfo.getRepairCost() >= 40;
        } else return false;
    }

    public boolean isValidPlacement(Player player, BoundingBox mineBox) {
        if (isOnIsland(player)) {
            BoundingBox islandBox = getIslandBox(player);
            return islandBox.contains(mineBox.getMaxX(), mineBox.getMaxY(), mineBox.getMaxZ()) && islandBox.contains(mineBox.getMinX(), mineBox.getMinY(), mineBox.getMinZ());
        } else return false;
    }

    public boolean isOnIsland(Player player) {
        Optional<Island> island = islandsManager.getIslandAt(player.getLocation());
        return island.isPresent();
    }

    public BoundingBox getIslandBox(Player player) {
        Island island = islandsManager.getIslandAt(player.getLocation()).get();

        return new BoundingBox(island.getMinProtectedX(), 0, island.getMinProtectedZ(), island.getMaxProtectedX(), 256, island.getMaxProtectedZ());
    }

    public void addPreviewPending(Player player) {
        previewPending.add(player.getName());
    }

    public void removePreviewPending(Player player) {
        previewPending.remove(player.getName());
    }

    public boolean isPreviewPending(Player player) {
        return previewPending.contains(player.getName());
    }

    public void addCooldown(Player player) {
        clickCooldowns.put(player.getName(), System.currentTimeMillis() + cooldownTime);
    }

    public boolean isInCooldown(Player player) {
        if (clickCooldowns.containsKey(player.getName())) {
            if (System.currentTimeMillis() < clickCooldowns.get(player.getName())) {
                return true;
            } else {
                clickCooldowns.remove(player.getName());
                return false;
            }
        } else {
            return false;
        }
    }

    public void removeCooldown(Player player) {
        clickCooldowns.remove(player.getName());
    }
}
