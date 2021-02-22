package com.celeste.celesteelevator.listener;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.manager.ElevatorManager;
import com.celeste.celesteelevator.util.adapter.MessageAdapter;
import lombok.AllArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class BlockListener implements Listener {

    private final CelesteElevator plugin;

    @EventHandler(ignoreCancelled = true)
    public void onPlace(final BlockPlaceEvent event) {
        final ElevatorManager elevator = plugin.getElevatorFactory().getElevatorManager();
        final ItemStack item = event.getItemInHand();

        if (!elevator.isElevator(item)) return;

        final Block block = event.getBlock();
        final List<String> worlds = plugin.getConfigManager().getConfig("blacklist");

        final MessageAdapter message = plugin.getMessageFactory().getMessageAdapter();
        final Player player = event.getPlayer();

        if (worlds.stream().anyMatch(world -> world.equalsIgnoreCase(block.getWorld().getName()))) {
            message.adaptAndSendToSender(player, "place.blacklist");
            event.setCancelled(true);
            return;
        }

        elevator.createElevator(block);
        elevator.sendSound(player, "sound.place.");
        message.adaptAndSendToSender(player, "place.success");
    }

    @EventHandler(ignoreCancelled = true)
    public void onBreak(final BlockBreakEvent event) {
        final ElevatorManager elevator = plugin.getElevatorFactory().getElevatorManager();
        final Block block = event.getBlock();

        if (!elevator.containsMetaData(block)) return;

        final Player player = event.getPlayer();
        final MessageAdapter message = plugin.getMessageFactory().getMessageAdapter();

        elevator.deleteElevator(UUID.fromString(elevator.getMetaData(block)));
        elevator.sendSound(player, "sound.break.");
        message.adaptAndSendToSender(player, "break.success");

        final ItemStack item = elevator.getElevator(block.getType(), 1);
        player.getInventory().addItem(item);
    }

}
