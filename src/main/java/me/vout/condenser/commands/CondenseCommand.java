package me.vout.condenser.commands;

import me.vout.condenser.helpers.GuiHelper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CondenseCommand implements CommandExecutor {

    private FileConfiguration config;

    public CondenseCommand(FileConfiguration config) {
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            GuiHelper.openInventory(player,config);
        }
        return true;
    }
}
