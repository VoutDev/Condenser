package me.vout.condenser;

import me.vout.condenser.commands.CondenseCommand;
import me.vout.condenser.commands.ReloadCommand;
import me.vout.condenser.listeners.BlockPlaceEvent;
import me.vout.condenser.listeners.InventoryClickEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Condenser extends JavaPlugin implements Listener {

    private FileConfiguration categoryConfig;
    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultCustomConfig();
        reloadCustomConfig();

        CondenseCommand condenseCommand = new CondenseCommand(categoryConfig,this);
        getCommand("condense").setExecutor(condenseCommand);
        ReloadCommand reloadCommand = new ReloadCommand(categoryConfig, this);
        getCommand("reloadCategories").setExecutor(reloadCommand);
        InventoryClickEvent clickEvent = new InventoryClickEvent(categoryConfig,this);
        BlockPlaceEvent placeEvent = new BlockPlaceEvent();

        getServer().getPluginManager().registerEvents(clickEvent,this);
        getServer().getPluginManager().registerEvents(placeEvent, this);
    }

    public void reloadCustomConfig() {
        categoryConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "categories.yml"));
        getLogger().info("Reloaded");
    }

    public void saveDefaultCustomConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File customConfigFile = new File(getDataFolder(), "categories.yml");
        if (!customConfigFile.exists()) {
            saveResource("categories.yml",false);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
