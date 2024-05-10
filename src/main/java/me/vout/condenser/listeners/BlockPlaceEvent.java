package me.vout.condenser.listeners;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockPlaceEvent implements Listener {

    @EventHandler
    public void onBlockPlace(org.bukkit.event.block.BlockPlaceEvent event) {
        ItemStack items = event.getItemInHand();
        if (items.getType() != Material.AIR) {
            ItemMeta meta = items.getItemMeta();
            if (meta != null && meta.hasEnchant(Enchantment.ARROW_INFINITE)) {
                event.getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacy(ChatColor.RED + "Can't place compressed blocks"));
                event.setCancelled(true);
            }
        }
    }
}