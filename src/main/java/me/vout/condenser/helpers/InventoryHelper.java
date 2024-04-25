package me.vout.condenser.helpers;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class InventoryHelper {

    public static boolean hasRoomForItem(Player player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();
        ItemStack[] contents = inventory.getContents();
        ItemStack[] cloneContents = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                cloneContents[i] = new ItemStack(contents[i]);
            }
        }

        HashMap<Integer, ItemStack> remainingItems = inventory.addItem(itemStack);
        boolean hasRoom = remainingItems.isEmpty();
        inventory.setContents(cloneContents);
        return hasRoom;
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
            if (itemStack != null && itemStack.getType() == material) {
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
