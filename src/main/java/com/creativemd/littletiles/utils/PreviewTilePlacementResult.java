package com.creativemd.littletiles.utils;

import java.util.ArrayList;
import java.util.List;

import com.creativemd.littletiles.common.utils.LittleTile;

public final class PreviewTilePlacementResult {

    private final ArrayList<LittleTile> placedTiles = new ArrayList<>();
    private final ArrayList<LittleTile> removedTiles = new ArrayList<>();

    public void addPlacedTile(LittleTile tile) {
        placedTiles.add(tile);
    }

    public void addPlacedTiles(List<LittleTile> tiles) {
        placedTiles.addAll(tiles);
    }

    public void addRemovedTile(LittleTile tile) {
        removedTiles.add(tile);
    }

    public List<LittleTile> getPlacedTiles() {
        return placedTiles;
    }

    public List<LittleTile> getRemovedTiles() {
        return removedTiles;
    }
}
