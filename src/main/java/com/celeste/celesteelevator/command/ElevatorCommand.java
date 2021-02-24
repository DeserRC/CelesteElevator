package com.celeste.celesteelevator.command;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.manager.ElevatorManager;
import com.celeste.celesteelevator.util.adapter.MessageAdapter;
import lombok.AllArgsConstructor;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.celeste.celesteelevator.util.MessagesUtil.build;

@AllArgsConstructor
public class ElevatorCommand {

    private final CelesteElevator plugin;

    @Command(
      name = "elevator",
      permission = "elevator.admin",
      target = CommandTarget.ALL,
      usage = "elevator <player> <material> <amount>"
    )
    public void handleElevatorCommand(final Context<CommandSender> context, final String targetName, final String materialName, final int amount) {
        final ElevatorManager elevator = plugin.getElevatorFactory().getElevatorManager();
        final MessageAdapter message = plugin.getMessageFactory().getMessageAdapter();

        final CommandSender sender = context.getSender();
        final Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            message.adaptAndSendToSender(sender, "elevator.player_not_found",
              build("{player}", targetName)
            );

            return;
        }

        final String[] split = materialName.split(":");

        final Material material = Arrays.stream(Material.values())
          .filter(type -> split[0].equalsIgnoreCase(type.name()))
          .findFirst()
          .orElse(null);
        final int data = split.length == 2 && !Pattern.compile("[^0-9]").matcher(split[1]).find() ? Integer.parseInt(split[1]) : 0;

        if (material == null) {
            message.adaptAndSendToSender(sender, "elevator.material_not_found",
              build("{material}", materialName)
            );

            return;
        }

        if (amount <= 0) {
            message.adaptAndSendToSender(sender, "elevator.invalid_amount",
              build("{amount}", amount)
            );

            return;
        }

        final ItemStack item = elevator.getElevator(material, data, amount);
        target.getInventory().addItem(item);

        message.adaptAndSendToSender(sender, "elevator.success",
          build("{target}", target.getName()),
          build("{amount}", amount)
        );

        message.adaptAndSendToSender(target, "elevator.receive",
          build("{sender}", sender.getName()),
          build("{amount}", amount)
        );
    }

}
