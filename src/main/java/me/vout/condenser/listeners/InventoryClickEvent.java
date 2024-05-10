package me.vout.condenser.listeners;

import me.vout.condenser.helpers.GuiHelper;
import me.vout.condenser.helpers.InventoryHelper;
import me.vout.condenser.models.BlockObject;
import me.vout.condenser.models.CondenserInventory;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class InventoryClickEvent implements Listener {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    public InventoryClickEvent(FileConfiguration config, JavaPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (!(event.getInventory().getHolder() instanceof CondenserInventory))
            return;

         else if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.BARRIER
                || clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "border"), PersistentDataType.STRING)) {
            event.setCancelled(true);
            return;
        }

        String title = player.getOpenInventory().getTitle();
        CondenserInventory condenserInventory = (CondenserInventory) event.getInventory().getHolder();
        if (title.equals("Condense Categories")) {
            ConfigurationSection utilitySection = config.getConfigurationSection("utility");
            if (clickedItem.getType() == Material.PLAYER_HEAD && clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING)) {
                String textureUrl = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING);
                if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"back_arrow"))) {
                    GuiHelper.openInventory(player, config, plugin, -1, condenserInventory.getCategoryPage() -1);
                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"next_arrow"))) {
                    GuiHelper.openInventory(player, config, plugin, -1, condenserInventory.getCategoryPage() +1);
                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"home"))) {
                    player.closeInventory();
                }
            } else {
                BlockObject blockObject = GuiHelper.getCategoryBlocks(clickedItem.getType().name(), condenserInventory.getCategoryPage(), config,1);
                if (blockObject.getBlocks() != null) {
                    GuiHelper.subGUI(blockObject.getBlocks(),player, config, plugin,1, clickedItem.getType().name(), condenserInventory.getCategoryPage());
                }
            }
            event.setCancelled(true);
        } else if (title.equals("Uncompress Selection")) {
            ConfigurationSection utilitySection = config.getConfigurationSection("utility");
            if (clickedItem.getType() == Material.PLAYER_HEAD && clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING)) {
                String textureUrl = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING);
                if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"checkmark"))) {
                    ItemStack countBlock = player.getOpenInventory().getItem(22);
                    ItemStack item = player.getOpenInventory().getItem(13);
                    int itemCount = Integer.parseInt(ChatColor.stripColor(countBlock.getItemMeta().getDisplayName()));
                    if (!InventoryHelper.updatePlayerInventory(player, itemCount, utilitySection, item, false)) {
                        player.closeInventory();
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "No space to uncompress blocks!"));
                    } else {
                        GuiHelper.selectionGUI(player,item.getType(),0,config,plugin,condenserInventory.getCategory(),condenserInventory.getCategoryPage(), condenserInventory.getPage());
                    }
                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"back_arrow"))) {
                    BlockObject blockObject = GuiHelper.getCategoryBlocks(condenserInventory.getCategory(),condenserInventory.getCategoryPage() ,config, condenserInventory.getPage());
                    if (blockObject != null) {
                        GuiHelper.subGUI(blockObject.getBlocks(), player, config, plugin, condenserInventory.getPage(), condenserInventory.getCategory(), condenserInventory.getCategoryPage());
                    }
                }
            } else if (clickedItem.getType() == Material.CHEST && ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).equals("Uncompress all")) {
                ItemStack itemMaterial = player.getOpenInventory().getItem(13);
                int itemCount = InventoryHelper.getItemCount(player.getInventory(), itemMaterial);
                if (!InventoryHelper.updatePlayerInventory(player,itemCount,utilitySection,itemMaterial,false)) {
                    player.closeInventory();
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "No space to uncompress blocks!"));
                }
            } else {
                ItemStack itemCount = player.getOpenInventory().getItem(22);
                ItemStack itemMaterial = player.getOpenInventory().getItem(13);
                int count = Integer.parseInt(ChatColor.stripColor(itemCount.getItemMeta().getDisplayName()));
                int max = InventoryHelper.getItemCount(player.getInventory(),itemMaterial);
                String changeStr = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

                if (changeStr.contains("+")) {
                    int change = Integer.parseInt(changeStr.replace("+",""));
                    count = count + change;
                } else if (changeStr.contains("-")) {
                    int change = Integer.parseInt(changeStr.replace("-",""));
                    count = count - change;
                }
                if (count < 0) {
                    count = 0;
                } else if (count > max) {
                    count = max;
                }
                else if (count > 2304) {
                    count = 2304;
                }
                GuiHelper.selectionGUI(player, itemMaterial.getType(), count, config, plugin, condenserInventory.getCategory(), condenserInventory.getCategoryPage(), condenserInventory.getPage());
            }
            event.setCancelled(true);

        } else if (title.equals("Compressible Blocks")) {
            ConfigurationSection utilitySection = config.getConfigurationSection("utility");
            if (clickedItem.getType() == Material.PLAYER_HEAD && clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING)) {
                String textureUrl = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING);
                if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"back_arrow"))) {
                    BlockObject blockObject= GuiHelper.getCategoryBlocks(condenserInventory.getCategory(), condenserInventory.getCategoryPage(),config,condenserInventory.getPage() -1);
                    if (blockObject != null) {
                        GuiHelper.subGUI(blockObject.getBlocks(),player,config,plugin, condenserInventory.getPage() -1, condenserInventory.getCategory(), condenserInventory.getCategoryPage());
                    }

                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"next_arrow"))) {
                    BlockObject blockObject= GuiHelper.getCategoryBlocks(condenserInventory.getCategory(), condenserInventory.getCategoryPage(),config,condenserInventory.getPage() +1);
                    if (blockObject != null) {
                        GuiHelper.subGUI(blockObject.getBlocks(),player,config,plugin, condenserInventory.getPage() +1, condenserInventory.getCategory(), condenserInventory.getCategoryPage());
                    }

                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"home"))) {
                    GuiHelper.openInventory(player, config, plugin, -1, condenserInventory.getCategoryPage());
                }
            } else if (event.getClick() == ClickType.LEFT) {

                ItemStack item = new ItemStack(clickedItem.getType());
                int itemCount = InventoryHelper.getItemCount(player.getInventory(), item);
                if (!InventoryHelper.updatePlayerInventory(player, itemCount, utilitySection, item, true)) {
                    player.closeInventory();
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "No space to compress blocks!"));
                }
            } else if (event.getClick() == ClickType.RIGHT) {
                GuiHelper.selectionGUI(player,clickedItem.getType(),0,config,plugin,condenserInventory.getCategory(),condenserInventory.getCategoryPage(), condenserInventory.getPage());
            }
            event.setCancelled(true);
        }
    }
}
