package me.vout.condenser.helpers;

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

    public static void openInventory(Player player, FileConfiguration config) {
        Inventory inventory = Bukkit.createInventory(player,45,"Condense Categories");
        ConfigurationSection section = config.getConfigurationSection("categories");
        categoryGUI(section,inventory);
        player.openInventory(inventory);
    }

    private static void categoryGUI(ConfigurationSection section, Inventory inventory) {
        Set<String> categoryKeys = section.getKeys(false);
        int slot = 10;
        for (String category : categoryKeys) {
            Material material = Material.matchMaterial(category);
            ConfigurationSection blockSection = section.getConfigurationSection(category);
            String name = blockSection.getString("name");
            String desc = blockSection.getString("lore");

            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.AQUA + name);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + desc);
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot,item);
            slot+=3;
        }
    }

    public static void subGUI(List<String> blocks, Player player, FileConfiguration config, JavaPlugin plugin, int currentPage, String category) {
        Inventory inventory = Bukkit.createInventory(player,54, "Compressible Blocks");
        int count = inventory.getSize();
        int slot =  0;
        for (String block : blocks) {
            if (slot <= count - 18) {
                Material material = Material.matchMaterial(block);
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
                inventory.setItem(slot,item);
                slot++;
            } else {
                player.sendMessage(block);
            }
        }
        helpButtons(inventory,config, plugin, currentPage, category);
        player.openInventory(inventory);
    }

    public static void helpButtons(Inventory inventory, FileConfiguration config, JavaPlugin plugin, int currentPage, String category) {
        Map<String, ItemStack> buttonMap = new HashMap<>();
        ConfigurationSection utilitySection = config.getConfigurationSection("utility");
        int invSize = inventory.getSize();

        for (int i = invSize - 18; i < invSize - 9; i++) {
            ItemStack item = new ItemStack(Material.getMaterial(utilitySection.getString("border")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.RESET + "");
            item.setItemMeta(meta);
            inventory.setItem(i,item);
        }

        if (currentPage - 1 > 0) {
            buttonMap.put("Back", createSkullItem(getUtilityUrl(utilitySection,"back_arrow"),plugin,category));
        }
        if (pageExist(category,config, currentPage + 1)) {
            buttonMap.put("Next", createSkullItem(getUtilityUrl(utilitySection,"next_arrow"),plugin,category));
        }
        buttonMap.put("Home", createSkullItem(getUtilityUrl(utilitySection,"home"),plugin,category));

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

    public static void selectionGUI(Player player, Material material, int count, FileConfiguration config, JavaPlugin plugin, String category, int currentPage) {
        Inventory inventory = Bukkit.createInventory(player,36, "Uncompress Selection");
        int invSize = inventory.getSize();
        ItemStack item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "+1");
        item.setItemMeta(meta);
        inventory.setItem(2, item);

        item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        item.setAmount(32);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "+32");
        item.setItemMeta(meta);
        inventory.setItem(11, item);

        item = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        item.setAmount(64);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "+64");
        item.setItemMeta(meta);
        inventory.setItem(20, item);

        item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "-1");
        item.setItemMeta(meta);
        inventory.setItem(6, item);

        item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        item.setAmount(32);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "-32");
        item.setItemMeta(meta);
        inventory.setItem(15, item);

        item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        item.setAmount(64);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "-64");
        item.setItemMeta(meta);
        inventory.setItem(24, item);

        item = new ItemStack(material);
        meta = item.getItemMeta();
        meta.addEnchant(Enchantment.ARROW_INFINITE,1,false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Compressed " + GuiHelper.formatMaterialString(item.getType()));
        item.setItemMeta(meta);
        inventory.setItem(13, item);

        item = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Blocks to uncompress.");
        meta.setLore(lore);
        meta.setDisplayName(ChatColor.WHITE + String.valueOf(count));
        item.setItemMeta(meta);
        inventory.setItem(22,item);

        ConfigurationSection utilitySection = config.getConfigurationSection("utility");

        item = GuiHelper.createSkullItem(GuiHelper.getUtilityUrl(utilitySection, "back_arrow"),plugin, category);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "Page " + currentPage);
        item.setItemMeta(meta);
        inventory.setItem(invSize - 6,item);


        item = GuiHelper.createSkullItem(GuiHelper.getUtilityUrl(utilitySection,"checkmark"),plugin, category);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Submit");
        item.setItemMeta(meta);
        inventory.setItem(invSize - 5,item);


        item = new ItemStack(Material.CHEST);
        meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Uncompress all");
        item.setItemMeta(meta);
        inventory.setItem(invSize - 4,item);

        player.openInventory(inventory);
    }

    public static ItemStack createSkullItem(String textureUrl, JavaPlugin plugin, String category) {
        String url = "http://textures.minecraft.net/texture/" + textureUrl;
        try {
            ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
            PlayerProfile playerProfile = Bukkit.createPlayerProfile(UUID.randomUUID());
            playerProfile.getTextures().setSkin(URI.create(url).toURL());

            SkullMeta skullMeta = (SkullMeta) skullItem.getItemMeta();
            skullMeta.setOwnerProfile(playerProfile);
            skullMeta.getPersistentDataContainer().set(new NamespacedKey(plugin,"texture"), PersistentDataType.STRING, textureUrl);
            skullMeta.getPersistentDataContainer().set(new NamespacedKey(plugin,"category"), PersistentDataType.STRING, category);
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

    public static String getCategoryForBlock(String block, FileConfiguration config) {
        ConfigurationSection categories = config.getConfigurationSection("categories");
        for (String category : categories.getKeys(false)) {
            ConfigurationSection categorySection = categories.getConfigurationSection(category);
            for (String pageKey : categorySection.getKeys(false)) {
                if (categorySection.getStringList(pageKey).contains(block)) {
                    return category;
                }
            }
        }
        return null;
    }

    public static List<String> getCategoryBlocks(String category, FileConfiguration config, int page) {
        ConfigurationSection categorySection = config.getConfigurationSection("categories." + category);
        if (categorySection != null) {
            return categorySection.getStringList("page_" + page);
        }
        return null;
    }

    public static boolean pageExist(String category, FileConfiguration config, int page) {
        ConfigurationSection categorySection = config.getConfigurationSection("categories." + category);
        if (categorySection != null) {
            return categorySection.contains("page_" + page);
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
