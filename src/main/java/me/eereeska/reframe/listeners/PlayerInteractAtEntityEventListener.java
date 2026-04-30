package me.eereeska.reframe.listeners;

import me.eereeska.reframe.ReFrame;
import me.eereeska.reframe.gui.menu.ItemFrameMenuInventoryHolder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;

public class PlayerInteractAtEntityEventListener implements Listener {

    private final ReFrame plugin;

    public PlayerInteractAtEntityEventListener(ReFrame plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles right-clicking on an ItemFrame.
     *
     * - Shift + right-click  → open the ReFrame settings menu (visibility / fixation)
     * - Right-click on an INVISIBLE frame (no shift) → cancel the frame interaction and
     *   open any container directly behind the frame.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemFrameInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame itemFrame)) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();

        // ── Shift + right-click: open settings menu ─────────────────────────
        if (player.isSneaking()) {
            boolean hasVisibility = player.hasPermission(
                    plugin.getConfig().getString("permissions.visibility", "reframe.toggle.visibility"));
            boolean hasFixation = player.hasPermission(
                    plugin.getConfig().getString("permissions.fixation", "reframe.toggle.fixation"));

            if (!hasVisibility && !hasFixation) return;

            event.setCancelled(true);
            ItemFrameMenuInventoryHolder holder = new ItemFrameMenuInventoryHolder(plugin, player, itemFrame);
            player.openInventory(holder.getInventory());
            return;
        }

        // ── Regular right-click on an invisible frame ────────────────────────
        if (!itemFrame.isVisible()) {
            String clickthroughPerm = plugin.getConfig().getString(
                    "permissions.clickthrough", "reframe.clickthrough");

            if (player.hasPermission(clickthroughPerm)) {
                // attachedFace — сторона, к которой прикреплена рамка (напр. NORTH).
                // Блок за рамкой находится в направлении, противоположном attachedFace.
                BlockFace attachedFace = itemFrame.getAttachedFace();
                Block blockBehind = itemFrame.getLocation().getBlock()
                        .getRelative(attachedFace.getOppositeFace());
                BlockState state = blockBehind.getState();

                if (state instanceof InventoryHolder inventoryHolder) {
                    // Отменяем взаимодействие с рамкой и открываем инвентарь напрямую.
                    event.setCancelled(true);
                    player.openInventory(inventoryHolder.getInventory());
                }
            }
        }
        // Visible frames fall through — vanilla behaviour applies.
    }
}
