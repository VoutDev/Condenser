package me.vout.condenser.listeners;

import me.vout.condenser.commands.CondenseCommand;
import me.vout.condenser.helpers.GuiHelper;
import me.vout.condenser.helpers.InventoryHelper;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class InventoryClickEvent implements Listener {
    private JavaPlugin plugin;
    private FileConfiguration config;

    public InventoryClickEvent(FileConfiguration config, JavaPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }
    @EventHandler
    public void onInventoryClick(org.bukkit.event.inventory.InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        String title = player.getOpenInventory().getTitle();
        if (title.equals("Condense Categories")) {
            List<String> blocks = GuiHelper.getCategoryBlocks(clickedItem.getType().name(), config, 1);
            if (blocks != null) {
                GuiHelper.subGUI(blocks,player,config,plugin,1,clickedItem.getType().name());
            }
            event.setCancelled(true);
        } else if (title.equals("Uncompress Selection")) {
            ConfigurationSection utilitySection = config.getConfigurationSection("utility");
            if (clickedItem.getType() == Material.PLAYER_HEAD && clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING)) {
                String textureUrl = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING);
                if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"checkmark"))) {
                    ItemStack itemCount = player.getOpenInventory().getItem(22);
                    ItemStack itemMaterial = player.getOpenInventory().getItem(13);
                    int count = Integer.parseInt(ChatColor.stripColor(itemCount.getItemMeta().getDisplayName()));
                    if (player.getInventory().containsAtLeast(itemMaterial,count) && count > 0) {
                        int cost = Integer.parseInt(utilitySection.getString("condense.count"));
                        ItemStack item = new ItemStack(itemMaterial.getType());
                        item.setAmount(count * cost);

                        if (InventoryHelper.hasRoomForItem(player,item)) {
                            InventoryHelper.removeSpecificItems(player,itemMaterial,count);
                            player.getInventory().addItem(item);
                            String blockCategory = GuiHelper.getCategoryForBlock(item.getType().name(),config);
                            int invSize = event.getInventory().getSize();
                            String[] pageStr =  event.getInventory().getItem(invSize - 6).getItemMeta().getDisplayName().split(" ");
                            int page = Integer.parseInt(pageStr[1]);
                            GuiHelper.selectionGUI(player,itemMaterial.getType(),0,config,plugin,blockCategory,page);
                            return;
                        } else {
                            player.closeInventory();
                            player.sendMessage(ChatColor.RED + "No space to uncompress blocks!");
                        }
                    }
                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"back_arrow"))) {
                    String[] pageStr = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).split(" ");
                    ItemStack itemMaterial = player.getOpenInventory().getItem(13);
                    String blockCategory = GuiHelper.getCategoryForBlock(itemMaterial.getType().name(),config);
                    if (blockCategory != null) {
                        List<String> blocks = GuiHelper.getCategoryBlocks(blockCategory,config,Integer.parseInt(pageStr[1]));
                        if (blocks != null) {
                            GuiHelper.subGUI(blocks,player,config,plugin, Integer.parseInt(pageStr[1]),blockCategory);
                        }
                    }
                }
            } else if (clickedItem.getType() == Material.CHEST && ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).equals("Uncompress all")) {
                ItemStack itemMaterial = player.getOpenInventory().getItem(13);
                int count = InventoryHelper.getItemCount(player.getInventory(), itemMaterial);
                if (player.getInventory().containsAtLeast(itemMaterial,count) && count > 0) {
                    int cost = Integer.parseInt(utilitySection.getString("condense.count"));
                    ItemStack item = new ItemStack(itemMaterial.getType());
                    item.setAmount(count * cost);

                    if (InventoryHelper.hasRoomForItem(player,item)) {
                        InventoryHelper.removeSpecificItems(player,itemMaterial,count);
                        player.getInventory().addItem(item);
                        player.closeInventory();
                    } else {
                        player.closeInventory();
                        player.sendMessage(ChatColor.RED + "No space to uncompress blocks!");
                    }
                }

            } else if (clickedItem.getType() != Material.AIR) {
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
                String blockCategory = GuiHelper.getCategoryForBlock(itemMaterial.getType().name(),config);
                int invSize = event.getInventory().getSize();
                String[] pageStr =  event.getInventory().getItem(invSize - 6).getItemMeta().getDisplayName().split(" ");
                int page = Integer.parseInt(pageStr[1]);
                GuiHelper.selectionGUI(player,itemMaterial.getType(),count,config,plugin,blockCategory,page);
            }
            event.setCancelled(true);

        } else if (title.equals("Compressible Blocks")) {
            ConfigurationSection utilitySection = config.getConfigurationSection("utility");
            if (clickedItem.getType() == Material.PLAYER_HEAD && clickedItem.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING)) {
                String textureUrl = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "texture"), PersistentDataType.STRING);
                if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"back_arrow"))) {
                    String[] pageStr = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).split(" ");
                    String blockCategory = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "category"), PersistentDataType.STRING);
                    List<String> blocks = GuiHelper.getCategoryBlocks(blockCategory,config,Integer.parseInt(pageStr[1]));
                    if (blocks != null) {
                        GuiHelper.subGUI(blocks,player,config,plugin,Integer.parseInt(pageStr[1]),blockCategory);
                    }

                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"next_arrow"))) {
                    String[] pageStr = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName()).split(" ");
                    String blockCategory = clickedItem.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin, "category"), PersistentDataType.STRING);
                    List<String> blocks = GuiHelper.getCategoryBlocks(blockCategory,config,Integer.parseInt(pageStr[1]));
                    if (blocks != null) {
                        GuiHelper.subGUI(blocks,player,config,plugin,Integer.parseInt(pageStr[1]),blockCategory);
                    }

                } else if (textureUrl.equals(GuiHelper.getUtilityUrl(utilitySection,"home"))) {
                    GuiHelper.openInventory(player,config);
                }
            } else if (clickedItem.getType() != Material.AIR && event.getClick() == ClickType.LEFT) {
                ItemStack item = new ItemStack(clickedItem.getType());
                if (player.getInventory().containsAtLeast(item,9)) {
                    int totalItems = InventoryHelper.getItemCount(player.getInventory(),item); //.getItemCount(player,item);
                    int cost = Integer.parseInt(utilitySection.getString("condense.count"));
                    int result = totalItems/cost;
                    if (result > 0) {
                        ItemMeta meta = item.getItemMeta();
                        meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
                        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        meta.setDisplayName(ChatColor.DARK_PURPLE + "Compressed " + GuiHelper.formatMaterialString(item.getType()));
                        item.setItemMeta(meta);
                        item.setAmount(result);

                        if (InventoryHelper.hasRoomForItem(player,item)) {
                            ItemStack removeItem = new ItemStack(item.getType());
                            InventoryHelper.removeSpecificItems(player,removeItem,result * cost);
                            player.getInventory().addItem(item);
                        } else {
                            player.closeInventory();
                            player.sendMessage(ChatColor.RED + "No space to compress blocks!");
                        }
                    }
                }
            } else if (clickedItem.getType() != Material.AIR && event.getClick() == ClickType.RIGHT) {
                int invSize = event.getInventory().getSize();
                //Need a check if the next arrow is null (No further page), if so use the back arrow item to get the page number
                ItemStack item = event.getInventory().getItem(invSize -2);
                if (item == null) { //Using back arrow item
                    item = event.getInventory().getItem(invSize - 8);
                    if (item == null) { //If back arrow and next don't exist, only 1 page
                        String blockCategory = GuiHelper.getCategoryForBlock(clickedItem.getType().name(),config);
                        GuiHelper.selectionGUI(player,clickedItem.getType(),0,config,plugin,blockCategory,1);
                    } else if (item != null) {
                        String[] pageStr = item.getItemMeta().getDisplayName().split(" ");
                        int page = Integer.parseInt(pageStr[1]) + 1;
                        String blockCategory = GuiHelper.getCategoryForBlock(clickedItem.getType().name(),config);
                        GuiHelper.selectionGUI(player,clickedItem.getType(),0,config,plugin,blockCategory,page);
                    }

                } else if (item != null) { //Using next arrow item
                    String[] pageStr = item.getItemMeta().getDisplayName().split(" ");
                    int page = Integer.parseInt(pageStr[1]) - 1;
                    String blockCategory = GuiHelper.getCategoryForBlock(clickedItem.getType().name(),config);
                    GuiHelper.selectionGUI(player,clickedItem.getType(),0,config,plugin,blockCategory,page);
                }
            }
            event.setCancelled(true);
        }
    }
}
