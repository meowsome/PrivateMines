package me.meowso.privatemines;

import javafx.geometry.BoundingBox;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import world.bentobox.bentobox.api.events.island.IslandEvent;
import java.io.IOException;

public class Listeners implements Listener {
    private DatabaseManager databaseManager;
    private PlacementChecker placementChecker;
    private PlacementHandler placementHandler;
    private PrivateMines privateMines;

    public Listeners(PrivateMines privateMines) {
        this.privateMines = privateMines;
        databaseManager = new DatabaseManager(privateMines);
        placementChecker = new PlacementChecker(privateMines);
        placementHandler = new PlacementHandler(privateMines);
    }

    // Detect right-clicking the mine set item
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) throws IOException {
        FileConfiguration config = privateMines.getConfig();
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemVoucher = player.getInventory().getItemInMainHand();

        if ((action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && placementChecker.isValidItem(itemVoucher)) {
            int depth = placementHandler.getDepth(itemVoucher);
            BoundingBox mineBox = placementHandler.getMineBox(player, depth);

            // If player is in cooldown, ignore. Otherwise, add cooldown.
            if (placementChecker.isInCooldown(player)) return;
            else placementChecker.addCooldown(player);

            // If the depth of the mine cannot be determined, inform user and cancel
            if (depth == 0) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " Invalid mine size.");
                return;
            }

            if (placementChecker.isPreviewPending(player)) {
                // Player has a mine preview pending and they clicked their item again

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.GRAY + " Placing your private mine, please wait.");
                placementHandler.removeItemFromInventory(player, itemVoucher);
                placementChecker.removePreviewPending(player);
                placementHandler.placeMine(player, mineBox, itemVoucher);
            } else {
                // Player clicks their item for the first time

                if (placementChecker.isValidPlacement(player, mineBox)) {
                    placementChecker.addPreviewPending(player);
                    placementHandler.showPlacementPreview(mineBox);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.GRAY + " Is this where you want your mine? Click again to confirm, move to cancel.");
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " Invalid placement");
                }
            }
        }
    }

    // Detect player movement to reject pending placement
    @EventHandler void onPlayerMove(PlayerMoveEvent event) {
        FileConfiguration config = privateMines.getConfig();
        Player player = event.getPlayer();

        if ((event.getFrom().getX() != event.getTo().getX() || event.getFrom().getY() != event.getTo().getY() || event.getFrom().getZ() != event.getTo().getZ()) && placementChecker.isPreviewPending(player)) {
            placementChecker.removePreviewPending(player);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("chatPrefix")) + ChatColor.RED + " Cancelled mine placement because you moved.");
        }
    }

    // Detect player quit and remove from cooldown if in
    @EventHandler void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        placementChecker.removeCooldown(player);
        if (placementChecker.isPreviewPending(player)) placementChecker.removePreviewPending(player);
    }

    // Removes all mines on an island on island reset
    @EventHandler void onIslandReset(IslandEvent.IslandResettedEvent event) throws IOException {
        String islandId = event.getOldIsland().getUniqueId();

        placementHandler.removeMines(databaseManager.getMines(islandId));
        databaseManager.removeMines(islandId);

        //todo maybe give back voucher?
    }
}
