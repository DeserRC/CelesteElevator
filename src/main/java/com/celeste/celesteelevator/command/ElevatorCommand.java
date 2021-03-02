package com.celeste.celesteelevator.command;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.manager.ElevatorManager;
import com.celeste.celesteelevator.util.adapter.MessageAdapter;
import com.google.common.base.Functions;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.annotation.Completer;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.celeste.celesteelevator.util.MessagesUtil.build;

public class ElevatorCommand {

    private final CelesteElevator plugin;

    private final List<Material> materials;
    private final List<String> amounts;

    public ElevatorCommand(final CelesteElevator plugin) {
        this.plugin = plugin;
        this.amounts = new ArrayList<>();

        materials = Arrays.stream(Material.values())
          .filter(material -> material.isSolid() && material.isBlock() && !material.isInteractable() && !material.hasGravity())
          .collect(Collectors.toList());

        for (int i = 1; i < 2305; i++) {
            amounts.add(String.valueOf(i));
        }
    }

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
        final Material material = materials.stream()
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

    @Completer(
      name = "elevator"
    )
    public List<String> handleElevatorTabCompleter(final Context<CommandSender> context) {
        switch (context.getArgs().length) {
            case 1:
                return Bukkit.getOnlinePlayers().stream()
                  .map(Player::getDisplayName)
                  .filter(name -> name.toUpperCase().startsWith(context.getArg(0).toUpperCase()))
                  .collect(Collectors.toList());
            case 2:
                return materials.stream()
                  .map(Enum::name)
                  .filter(name -> name.toUpperCase().startsWith(context.getArg(1).toUpperCase()))
                  .collect(Collectors.toList());
            case 3:
                return amounts.stream()
                  .filter(amount -> amount.startsWith(context.getArg(2)))
                  .collect(Collectors.toList());
        }

        return null;
    }

}
