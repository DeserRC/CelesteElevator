package com.celeste.celesteelevator.command;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.manager.ConfigManager;
import com.celeste.celesteelevator.util.adapter.MessageAdapter;
import lombok.AllArgsConstructor;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import org.bukkit.command.CommandSender;

@AllArgsConstructor
public class ElevatorReloadCommand {

    private final CelesteElevator plugin;

    @Command(
      name = "elevator.reload",
      permission = "elevator.admin",
      target = CommandTarget.ALL,
      usage = "elevator",
      aliases = {"elevator rl"}
    )
    public void handleElevatorReloadCommand(final Context<CommandSender> context) {
        final ConfigManager config = plugin.getConfigManager();
        final MessageAdapter message = plugin.getMessageFactory().getMessageAdapter();

        final CommandSender sender = context.getSender();
        config.load();

        message.adaptAndSendToSender(sender, "reload.success");
    }
}
