package me.vout.condenser.models;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CondenserInventory implements InventoryHolder {
    private String category;
    private int categoryPage;
    private int page;
    private Inventory inventory;

    public CondenserInventory(String category, int categoryPage, int page, int size, String title) {
        this.category = category;
        this.categoryPage = categoryPage;
        this.page = page;
        this.inventory = Bukkit.createInventory(this, size, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
    public String getCategory() {return category;}
    public int getCategoryPage() {return categoryPage;}
    public int getPage() {return page;}
}
