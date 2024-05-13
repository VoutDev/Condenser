package me.vout.condenser.helpers;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InventoryHelper {

    public static boolean hasRoomForItem(Player player, ItemStack removeItem, List<ItemStack> addItemStacks, int removeCount) {
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] cloneContents = new ItemStack[contents.length];

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                cloneContents[i] = new ItemStack(contents[i]);
            }
        }
        removeSpecificItems(player,removeItem,removeCount);
        for (int i=0; i < addItemStacks.size(); i++) {
            HashMap<Integer, ItemStack> remainingItems = inventory.addItem(addItemStacks.get(i).clone());
            if (!remainingItems.isEmpty()){
                inventory.setContents(cloneContents);
                return false;
            }
        }
        inventory.setContents(cloneContents);
        return true;
    }

    public static boolean updatePlayerInventory(Player player, int itemCount, ConfigurationSection utilitySection, ItemStack item, boolean isCompress) {
        try {
            int cost = Integer.parseInt(utilitySection.getString("condense.count"));
            int result;
            int[] itemArray;
            if (isCompress)
                result = itemCount / cost;
            else
                result = itemCount * cost;
            int stacks = result / 64;
            int items = result % 64;
            List<ItemStack> blocks = new ArrayList<>();
            if (item.getMaxStackSize() == 1 && Boolean.parseBoolean(utilitySection.getString("condense_unstackable"))) {
                itemArray = new int[result];
                for (int i=0; i < result; i++) {
                    itemArray[i] = 1;
                }
            } else {
                itemArray = (items > 0 && stacks > 0) ? new int[stacks + 1] :
                        (items > 0) ? new int[1] :
                                (stacks > 0) ? new int[stacks] :
                                        new int[0];

                for (int i = 0; i < stacks; i++) {
                    itemArray[i] = 64;
                }
                if (items > 0) {
                    itemArray[itemArray.length - 1] = items;
                }
            }

            for (int i = 0; i < itemArray.length; i++) {
                ItemStack newBlock = new ItemStack(item.getType());
                ItemMeta meta = newBlock.getItemMeta();
                if (isCompress && meta != null) {
                    meta.addEnchant(Enchantment.ARROW_INFINITE, 1, false);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    meta.setDisplayName(ChatColor.DARK_PURPLE + "Compressed " + GuiHelper.formatMaterialString(newBlock.getType()));
                }
                newBlock.setItemMeta(meta);
                newBlock.setAmount(itemArray[i]);
                blocks.add(newBlock);
            }
            if (InventoryHelper.hasRoomForItem(player, item, blocks, isCompress ? (result * cost) : (result / cost))) {
                InventoryHelper.removeSpecificItems(player, item, isCompress ? (result * cost) : (result / cost));
                for (int i = 0; i < blocks.size(); i++) {
                    player.getInventory().addItem(blocks.get(i));
                }
                return true;
            }
        } catch (Exception e) {

        }
        return false;
    }

    public static void removeSpecificItems(Player player, ItemStack item, int quantity) {
        Material material = item.getType();
        Enchantment enchantment = item.getEnchantments().keySet().stream().findFirst().orElse(null);
        if (enchantment == null) {
            removeItems(player, item.getType(),quantity);
            return;
        }
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();

        int remainingQuantity = quantity;
        for (int i=0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (stack != null && stack.getType() == material && stack.getEnchantments().keySet().contains(enchantment)) {
                int stackQuantity = stack.getAmount();
                if (stackQuantity <= remainingQuantity) {
                    remainingQuantity -= stackQuantity;
                    contents[i] = null;
                } else {
                    stack.setAmount(stackQuantity - remainingQuantity);
                    remainingQuantity = 0;
                    break;
                }
            }
        }
        if (remainingQuantity == 0) {
            player.getInventory().setContents(contents);
        }
    }

    public static void removeItems(Player player, Material material,int quantity) {
        ItemStack[] contents = player.getInventory().getContents();
        int remainingQuantity = quantity;

        for (int i=0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack != null && itemStack.getType() == material && itemStack.getEnchantments().isEmpty()) {
                int stackQuantity = itemStack.getAmount();
                if (stackQuantity <= remainingQuantity) {
                    remainingQuantity -= stackQuantity;
                    contents[i] = null;
                } else {
                    itemStack.setAmount(stackQuantity - remainingQuantity);
                    remainingQuantity = 0;
                    break;
                }
            }
        }
        if (remainingQuantity == 0) {
            player.getInventory().setContents(contents);
        }
    }

    public static int getItemCount(Inventory inventory, ItemStack targetItem) {
        int totalItems = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.isSimilar(targetItem)) {
                totalItems += item.getAmount();
            }
        }
        return totalItems;
    }
}
