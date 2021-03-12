package com.celeste.celesteelevator.listener;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.manager.ConfigManager;
import com.celeste.celesteelevator.manager.ElevatorManager;
import com.celeste.celesteelevator.type.DirectionType;
import com.celeste.celesteelevator.util.adapter.MessageAdapter;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.stream.Collectors;

@AllArgsConstructor
public class TeleportListener implements Listener {

    private final CelesteElevator plugin;

    @EventHandler(ignoreCancelled = true)
    public void onMove(final PlayerMoveEvent event) {
        final ElevatorManager elevator = plugin.getElevatorFactory().getElevatorManager();
        final Player player = event.getPlayer();

        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getY() <= to.getY()) return;

        final Block block = from.clone().subtract(0, 1, 0).getBlock();
        final Location location = block.getLocation();

        if (!elevator.containsMetaData(block)) return;

        final ConfigManager config = plugin.getConfigManager();

        int limit = block.getY() + (int) config.getConfig("maxdistance");
        if (limit > 256) limit = 256;

        final MessageAdapter message = plugin.getMessageFactory().getMessageAdapter();
        final Location newLocation = location.clone();

        for (int i = block.getY() + 2; i <= limit; i++) {
            newLocation.setY(i);

            final Block nextBlock = newLocation.getBlock();
            if (!elevator.containsMetaData(nextBlock)) continue;

            final Location nextBlockLocation = nextBlock.getLocation();
            nextBlockLocation.add(0.5, 1, 0.5);
            nextBlockLocation.setPitch(to.getPitch());
            nextBlockLocation.setYaw(to.getYaw());

            player.teleport(nextBlockLocation);
            elevator.sendSound(player, "sound.up.");
            elevator.sendParticle(player, DirectionType.UP);
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

        for (int i = block.getY() - 1; i >= limit; i--) {
            newLocation.setY(i);

            final Block nextBlock = newLocation.getBlock();
            if (!elevator.containsMetaData(nextBlock)) continue;

            final Location nextBlockLocation = nextBlock.getLocation();
            nextBlockLocation.add(0.5, 1, 0.5);
            nextBlockLocation.setPitch(location.getPitch());
            nextBlockLocation.setYaw(location.getYaw());

            player.teleport(nextBlockLocation);
            elevator.sendSound(player, "sound.down.");
            elevator.sendParticle(player, DirectionType.DOWN);
            message.adaptAndSendToSender(player, "down.success");
            break;
        }
    }

    @EventHandler
    public void onTabCompleter(final TabCompleteEvent event) {
        event.setCompletions(event.getCompletions().stream()
          .map(completer -> completer
            .replaceAll("§[a-f0-9]", "")
            .replaceAll("&[a-f0-9]", "")
          ).collect(Collectors.toList()));
    }

}
