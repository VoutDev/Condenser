package me.vout.condenser;

import me.vout.condenser.commands.CondenseCommand;
import me.vout.condenser.listeners.InventoryClickEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class Condenser extends JavaPlugin implements Listener {

    private FileConfiguration customConfig;
    @Override
    public void onEnable() {
        // Plugin startup logic

        //TO-DO
        //Add pages for categories so more can be added, needs to also leverage help buttons
            //Needs .yml update to add pages after categories, may break logic chains
        //Add logic so player can /condense CATEGORY/CATEGORY-DISPLAY_NAME
        //Check to make sure the back page and next page are right. The current page should not equal either of them
            //Ex: Back: Page 1, Next: Page 3 (means you are on page 2)
        //Add method that takes in a section and a block string. If Material.getMaterial(block) is null, grab the .url of the section.StringList(block).
            //This allows users to store heads anywhere, but they need the url path
            //If material is null, and url path doesn't give a skull. Create Bedrock item with the display name being the block, and lore being error message
        //Add 2 paths for border in utility. If second path is nothing, use first path for all borders.
        //Otherwise have first be for all odd pages, and second be for all even pages





        saveDefaultCustomConfig();
        reloadCustomConfig(); //Needed for some reason

        CondenseCommand condenseCommand = new CondenseCommand(customConfig);
        getCommand("condense").setExecutor(condenseCommand);
        InventoryClickEvent clickEvent = new InventoryClickEvent(customConfig,this);

        getServer().getPluginManager().registerEvents(clickEvent,this);
    }

    public void reloadCustomConfig() {
        customConfig = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "categories.yml"));
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

    public FileConfiguration getCustomConfig() {
        if (customConfig == null) {
            reloadCustomConfig();
        }
        return customConfig;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}