package me.vout.condenser.models;

import java.util.List;

public class BlockObject {
    private List<String> blocks;
    private int categoryPage;

    public BlockObject(List<String> blocks, int categoryPage) {
        this.blocks = blocks;
        this.categoryPage = categoryPage;
    }

    public List<String> getBlocks() {
        return blocks;
    }

    public int getCategoryPage() {
        return categoryPage;
    }
}
