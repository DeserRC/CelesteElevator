package com.celeste.celesteelevator;

import com.celeste.ServerPlugin;
import com.celeste.celesteelevator.command.ElevatorCommand;
import com.celeste.celesteelevator.command.ElevatorReloadCommand;
import com.celeste.celesteelevator.factory.ConnectionFactory;
import com.celeste.celesteelevator.factory.ElevatorFactory;
import com.celeste.celesteelevator.factory.MessageFactory;
import com.celeste.celesteelevator.factory.TaskFactory;
import com.celeste.celesteelevator.listener.BlockListener;
import com.celeste.celesteelevator.listener.TeleportListener;
import com.celeste.celesteelevator.manager.ConfigManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class CelesteElevator extends ServerPlugin {

    private ExecutorService executor;
    private ScheduledExecutorService scheduled;

    private ConfigManager configManager;

    private ConnectionFactory connectionFactory;
    private ElevatorFactory elevatorFactory;
    private MessageFactory messageFactory;
    private TaskFactory taskFactory;

    @Override
    public void onLoad() {
        this.executor = Executors.newCachedThreadPool();
        this.scheduled = Executors.newScheduledThreadPool(2);

        this.configManager = new ConfigManager(this);
    }

    @Override
    public void onEnable() {
        this.connectionFactory = new ConnectionFactory(this);
        this.elevatorFactory = new ElevatorFactory(this);
        this.messageFactory = new MessageFactory(this);
        this.taskFactory = new TaskFactory(this);

        startCommandManager("eua",
          new ElevatorCommand(this),
          new ElevatorReloadCommand(this)
        );

        registerListeners(
          new BlockListener(this),
          new TeleportListener(this)
        );

        loadTasks();
        loadCrafts();
    }

    @Override
    public void onDisable() {
        taskFactory.getElevatorUpdateTask().run();
        getConnectionFactory().getSqlProcessor().disconnect();

        HandlerList.unregisterAll();
        executor.shutdown();
        scheduled.shutdown();
    }

    private void loadTasks() {
        executor.execute(taskFactory.getElevatorGetTask());
        scheduled.scheduleWithFixedDelay(taskFactory.getElevatorUpdateTask(), 30, 30, TimeUnit.SECONDS);
    }

    private void loadCrafts() {
        configManager.getKeysConfig("craft").forEach(name -> {
            final String path = "craft." + name + ".";

            final ItemStack elevator = elevatorFactory.getElevatorManager().getElevator(
                Material.getMaterial(configManager.getConfig(path + "material")),
                configManager.getConfig(path + "data")
            );

            final NamespacedKey key = new NamespacedKey(this, Material.getMaterial(configManager.getConfig(path + "material")).name());
            final ShapedRecipe recipe = new ShapedRecipe(key, elevator);
            recipe.shape("012", "345", "678");

            configManager.getKeysConfig(path + "items").stream()
                .map(craftItemSlot -> craftItemSlot.charAt(0))
                .forEach(craftItemSlot -> {
                    final Material craftItemMaterial = Material.getMaterial(
                        configManager.getConfig("craft." + name + ".items." + craftItemSlot)
                    );

                    if (craftItemMaterial == null) return;

                    recipe.setIngredient(craftItemSlot, craftItemMaterial);
            });

            Bukkit.addRecipe(recipe);
        });
    }

}
