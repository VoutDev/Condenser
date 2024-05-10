package me.vout.condenser.commands;

import me.vout.condenser.helpers.GuiHelper;
import me.vout.condenser.helpers.InventoryHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class CondenseCommand implements CommandExecutor {

    private FileConfiguration config;
    private JavaPlugin plugin;

    public CondenseCommand(FileConfiguration config, JavaPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            ConfigurationSection utilitySection = config.getConfigurationSection("utility");
            Material heldMaterial =  heldItem.getType();
            if (heldMaterial != Material.AIR) {
                int itemCount = InventoryHelper.getItemCount(player.getInventory(), heldItem);
                int cost = Integer.parseInt(utilitySection.getString("condense.count"));
                Enchantment enchantment = heldItem.getEnchantments().keySet().stream().findFirst().orElse(null);
               if (enchantment == null) {
                   if (itemCount/cost <= 0) {
                       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "Need " + (cost - itemCount) + " more " + heldItem.getItemMeta().getDisplayName() + " to compress!"));
                       return true;
                   }
                   if (!InventoryHelper.updatePlayerInventory(player, itemCount, utilitySection, heldItem, true)) {
                       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "No space to compress blocks!"));
                   }
               } else {
                   if (!InventoryHelper.updatePlayerInventory(player, itemCount, utilitySection, heldItem, false)) {
                       player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "No space to uncompress blocks!"));
                   }
               }

            } else if (Boolean.parseBoolean(utilitySection.getString("open_gui"))) {
                GuiHelper.openInventory(player,config,plugin,-1,1);
            } else {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "Main hand is empty!"));
            }
        }
        return true;
    }
}