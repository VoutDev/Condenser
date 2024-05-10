package me.vout.condenser.helpers;

import me.vout.condenser.models.BlockObject;
import me.vout.condenser.models.CondenserInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;

import java.net.URI;
import java.util.*;

public class GuiHelper {

    public static void openInventory(Player player, FileConfiguration config, JavaPlugin plugin, int currentPage, int categoryPage) {
        CondenserInventory newInv = new CondenserInventory("",categoryPage,currentPage,45,"Condense Categories");
        ConfigurationSection section = config.getConfigurationSection("categories");
        ConfigurationSection pageSection = section.getConfigurationSection("page_" + categoryPage);
        categoryGUI(pageSection, config, plugin, newInv.getInventory(), currentPage, categoryPage);
        player.openInventory(newInv.getInventory());
    }

    private static void categoryGUI(ConfigurationSection section, FileConfiguration config, JavaPlugin plugin, Inventory inventory, int currentPage, int categoryPage) {
        Set<String> categoryKeys = section.getKeys(false);
        int type = Integer.parseInt(config.getConfigurationSection("categories").getString("type"));
        int invSize = inventory.getSize();
        int slot;
        int increment;

        switch (type){
            case 0: //Large
                slot = 0;
                increment = 1;
                break;
            case 1: //Medium
                slot = 0;
                increment = 2;
                break;
            case 2: //Small
                slot = 1;
                increment = 3;
                break;
            default:
                slot = 0;
                increment = 2;
                break;
        }
        for (String category : categoryKeys) {
            Material material = Material.matchMaterial(category);
            if (slot < invSize - 18) {
                if (material == null) {
                    material = Material.BARRIER;
                }
                ConfigurationSection blockSection = section.getConfigurationSection(category);
                String name = blockSection.getString("name");
                String desc = blockSection.getString("lore");

                ItemStack item = new ItemStack(material);
                ItemMeta meta = item.getItemMeta();
                if (meta != null && material != Material.BARRIER) {
                    meta.setDisplayName(ChatColor.AQUA + name);
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + desc);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                } else if (meta != null) {
                    meta.setDisplayName(ChatColor.RED + category);
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + category + " is not a valid material");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.setItem(slot,item);
                slot+=increment;
            } else {
                plugin.getLogger().warning("Exceeded Category Page Limit - Page: " + categoryPage + ", Category: " + category + ", Material: " + material);
            }
        }
        //Need to build the logic to show a next button for categories
        helpButtons(inventory,config, plugin, currentPage, "", categoryPage);
    }

    public static void subGUI(List<String> blocks, Player player, FileConfiguration config, JavaPlugin plugin, int currentPage, String category, int categoryPage) {
        CondenserInventory newInv = new CondenserInventory(category,categoryPage,currentPage,54,"Compressible Blocks");
        //Inventory inventory = Bukkit.createInventory(player,54, "Compressible Blocks");
        //int count = inventory.getSize();
        int count = newInv.getInventory().getSize();
        int slot =  0;
        for (String block : blocks) {
            if (slot <= count - 18) {
                Material material = Material.matchMaterial(block);
                if (material != null) {
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GREEN + "Left click to compress all.");
                    lore.add(ChatColor.RED + "Right click to uncompress");
                    meta.setLore(lore);
                    meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.setDisplayName(ChatColor.DARK_PURPLE + "Compressed " + GuiHelper.formatMaterialString(item.getType()));
                    item.setItemMeta(meta);
                    //inventory.setItem(slot,item);
                    newInv.getInventory().setItem(slot, item);
                    slot++;

                } else {
                    ItemStack item = new ItemStack(Material.BARRIER);
                    ItemMeta meta = item.getItemMeta();
                    meta.setDisplayName(ChatColor.RED + block);
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + block + " is not a valid material");
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    //inventory.setItem(slot,item);
                    newInv.getInventory().setItem(slot, item);
                    slot++;
                }
            } else {
                plugin.getLogger().warning("Exceeded Compress Block Page Limit - Category Page: " + categoryPage + ", Category: " + category + ", Page: " + currentPage + ", Material: " + block);
            }
        }
        //helpButtons(inventory,config, plugin, currentPage, category, categoryPage);
        helpButtons(newInv.getInventory(), config, plugin, currentPage, category, categoryPage);
        //player.openInventory(inventory);
        player.openInventory(newInv.getInventory());
    }

    public static void helpButtons(Inventory inventory, FileConfiguration config, JavaPlugin plugin, int currentPage, String category, int categoryPage) {
        Map<String, ItemStack> buttonMap = new HashMap<>();
        ConfigurationSection utilitySection = config.getConfigurationSection("utility");
        int invSize = inventory.getSize();

        for (int i = invSize - 18; i < invSize - 9; i++) {
            ItemStack item = new ItemStack(Material.getMaterial(utilitySection.getString("border")));
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin,"border"), PersistentDataType.STRING, "border");
            meta.setDisplayName(ChatColor.RESET + "");
            item.setItemMeta(meta);
            inventory.setItem(i,item);
        }


        if (currentPage == -1) { //Category case
            if (categoryPage -1 > 0) {
                buttonMap.put("Back", createSkullItem(getUtilityUrl(utilitySection,"back_arrow"),plugin,category, categoryPage));
            }
            if (pageExist(category,config, currentPage, categoryPage +1)) {
                buttonMap.put("Next", createSkullItem(getUtilityUrl(utilitySection,"next_arrow"),plugin,category, categoryPage));
            }
            currentPage = categoryPage; //Bad method, needs refactoring to use 1 variable as page

        } else if (currentPage > -1) {
            if (currentPage - 1 > 0) {
                buttonMap.put("Back", createSkullItem(getUtilityUrl(utilitySection,"back_arrow"),plugin,category, categoryPage));
            }
            if (pageExist(category, config, currentPage + 1, categoryPage)) {
                buttonMap.put("Next", createSkullItem(getUtilityUrl(utilitySection,"next_arrow"),plugin,category, categoryPage));
            }
        }
        buttonMap.put("Home", createSkullItem(getUtilityUrl(utilitySection,"home"),plugin,category, categoryPage));

        for (Map.Entry<String, ItemStack> entry : buttonMap.entrySet()) {
            ItemStack item = entry.getValue();
            ItemMeta meta = item.getItemMeta();

            switch (entry.getKey()) {
                case "Back":
                    meta.setDisplayName(ChatColor.RED + "Page " + (currentPage - 1));
                    item.setItemMeta(meta);
                    inventory.setItem(invSize - 8,item);
                    break;

                case "Home":
                    meta.setDisplayName(ChatColor.RED + "Home");
                    item.setItemMeta(meta);
                    inventory.setItem(invSize - 5, item);
                    break;

                case "Next":
                    meta.setDisplayName(ChatColor.RED + "Page " + (currentPage + 1));
                    item.setItemMeta(meta);
                    inventory.setItem(invSize - 2,item);
                    break;

                default:
                    break;
            }
        }
    }

    public static void selectionGUI(Player player, Material material, int count, FileConfiguration config, JavaPlugin plugin, String category, int categoryPage,  int currentPage) {
        //Inventory inventory = Bukkit.createInventory(player,36, "Uncompress Selection");
        CondenserInventory newInv = new CondenserInventory(category, categoryPage, currentPage, 36, "Uncompress Selection");

        //int invSize = inventory.getSize();
        int invSize = newInv.getInventory().getSize();
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "+1");
        item.setItemMeta(meta);
        //inventory.setItem(2, item);
        newInv.getInventory().setItem(2, item);

        item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        item.setAmount(32);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "+32");
        item.setItemMeta(meta);
        //inventory.setItem(11, item);
        newInv.getInventory().setItem(11, item);

        item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        item.setAmount(64);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "+64");
        item.setItemMeta(meta);
        //inventory.setItem(20, item);
        newInv.getInventory().setItem(20, item);

        item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "-1");
        item.setItemMeta(meta);
        //inventory.setItem(6, item);
        newInv.getInventory().setItem(6, item);

        item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        item.setAmount(32);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "-32");
        item.setItemMeta(meta);
        //inventory.setItem(15, item);
        newInv.getInventory().setItem(15, item);

        item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        item.setAmount(64);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "-64");
        item.setItemMeta(meta);
        //inventory.setItem(24, item);
        newInv.getInventory().setItem(24, item);

        item = new ItemStack(material);
        meta = item.getItemMeta();
        meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Compressed " + GuiHelper.formatMaterialString(item.getType()));
        item.setItemMeta(meta);
        //inventory.setItem(13, item);
        newInv.getInventory().setItem(13, item);

        item = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Blocks to uncompress.");
        meta.setLore(lore);
        meta.setDisplayName(ChatColor.WHITE + String.valueOf(count));
        item.setItemMeta(meta);
        //inventory.setItem(22,item);
        newInv.getInventory().setItem(22, item);

        ConfigurationSection utilitySection = config.getConfigurationSection("utility");

        item = GuiHelper.createSkullItem(GuiHelper.getUtilityUrl(utilitySection, "back_arrow"),plugin, category, categoryPage);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Page " + currentPage);
        item.setItemMeta(meta);
        //inventory.setItem(invSize - 6,item);
        newInv.getInventory().setItem(invSize - 6, item);


        item = GuiHelper.createSkullItem(GuiHelper.getUtilityUrl(utilitySection,"checkmark"),plugin, category, categoryPage);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Submit");
        item.setItemMeta(meta);
        //inventory.setItem(invSize - 5,item);
        newInv.getInventory().setItem(invSize - 5, item);


        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Uncompress all");
        item.setItemMeta(meta);
        //inventory.setItem(invSize - 4,item);
        newInv.getInventory().setItem(invSize - 4, item);

        //player.openInventory(inventory);
        player.openInventory(newInv.getInventory());
    }

    public static ItemStack createSkullItem(String textureUrl, JavaPlugin plugin, String category, int categoryPage) {
        String url = "http://textures.minecraft.net/texture/" + textureUrl;
        try {
            ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
            PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
            playerProfile.getTextures().setSkin(URI.create(url).toURL());

            SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
            skullMeta.setOwnerProfile(playerProfile);
            skullMeta.getPersistentDataContainer().set(new NamespacedKey(plugin,"texture"), PersistentDataType.STRING, textureUrl);
            if (!Objects.equals(category, "")) {
                skullMeta.getPersistentDataContainer().set(new NamespacedKey(plugin,"category"), PersistentDataType.STRING, category);
                skullMeta.getPersistentDataContainer().set(new NamespacedKey(plugin,"categoryPage"), PersistentDataType.INTEGER, categoryPage);
            }
            skullItem.setItemMeta(skullMeta);
            return skullItem;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUtilityUrl(ConfigurationSection utilitySection, String key) {
        if (utilitySection != null) {
            try {
                String url = utilitySection.getString(key + ".url");
                return url;
            } catch (Exception e) {
                return null;
            }

        } else {
            return null;
        }
    }

    public static BlockObject getCategoryBlocks(String category, int categoryPage, FileConfiguration config, int page) {
        ConfigurationSection categorySection = config.getConfigurationSection("categories.page_" + categoryPage + "." + category);
        if (categorySection != null) {
            return new BlockObject(categorySection.getStringList("page_" + page),categoryPage);
        }
        return null;
    }

    public static boolean pageExist(String category, FileConfiguration config, int blockPage, int categoryPage) {
        if (blockPage == -1) { //Category page
            ConfigurationSection categorySection = config.getConfigurationSection("categories");
            if (categorySection != null) {
                return categorySection.contains("page_" + categoryPage);
            }
        } else { //Condensed block page
            ConfigurationSection categorySection = config.getConfigurationSection("categories.page_" + categoryPage + "." + category);
            if (categorySection != null) {
                return categorySection.contains("page_" + blockPage);
            }
        }
        return false;
    }

    public static String formatMaterialString(Material material) {
        String[] split = material.name().toLowerCase().split("_");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].substring(0,1).toUpperCase() + split[i].substring(1);
        }
        return String.join(" ", split).trim();
    }
}
