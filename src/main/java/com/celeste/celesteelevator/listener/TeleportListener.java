package com.celeste.celesteelevator.listener;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.manager.ConfigManager;
import com.celeste.celesteelevator.manager.ElevatorManager;
import com.celeste.celesteelevator.util.adapter.MessageAdapter;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

@AllArgsConstructor
public class TeleportListener implements Listener {

    private final CelesteElevator plugin;

    @EventHandler(ignoreCancelled = true)
    public void onMove(final PlayerMoveEvent event) {
        final ElevatorManager elevator = plugin.getElevatorFactory().getElevatorManager();
        final Player player = event.getPlayer();

        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getY() <= to.getY() || from.getX() != to.getX() || from.getZ() != to.getZ()) return;

        final Block block = from.clone().subtract(0, 1, 0).getBlock();
        final Location location = block.getLocation();

        if (!elevator.containsMetaData(block)) return;

        final ConfigManager config = plugin.getConfigManager();

        int limit = block.getY() + (int) config.getConfig("maxdistance");
        if (limit > 256) limit = 256;

        final MessageAdapter message = plugin.getMessageFactory().getMessageAdapter();
        final Location newLocation = location.clone();

        for (int i = block.getY(); i <= limit; i++) {
            newLocation.setY(i);

            final Block nextBlock = newLocation.getBlock();
            if (!elevator.containsMetaData(nextBlock)) continue;

            player.teleport(newLocation);
            elevator.sendSound(player, "sound.up.");
            message.adaptAndSendToSender(player, "up.success");
            break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onSneak(final PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        final ElevatorManager elevator = plugin.getElevatorFactory().getElevatorManager();
        final Player player = event.getPlayer();

        final Location location = player.getLocation();
        final Block block = location.clone().subtract(0, 1, 0).getBlock();

        if (!elevator.containsMetaData(block)) return;

        final ConfigManager config = plugin.getConfigManager();

        int limit = block.getY() - (int) config.getConfig("maxdistance");
        if (limit < 0) limit = 0;

        final MessageAdapter message = plugin.getMessageFactory().getMessageAdapter();
        final Location newLocation = location.clone();

        for (int i = block.getY(); i >= limit; i--) {
            newLocation.setY(i);

            final Block nextBlock = newLocation.getBlock();
            if (!elevator.containsMetaData(nextBlock)) continue;

            player.teleport(newLocation);
            elevator.sendSound(player, "sound.down.");
            message.adaptAndSendToSender(player, "down.success");
            break;
        }
    }

}
