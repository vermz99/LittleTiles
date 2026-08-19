package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ChunkCoordinates;

import com.creativemd.littletiles.common.items.LittleTileChangePlan.ChangeEntry;
import com.creativemd.littletiles.common.utils.LittleTile;

/**
 * Result of applying a {@link LittleTilePlacementPlan}.
 */
public final class LittleTilePlacementPlanResult {

    private final ArrayList<ChangeEntry> changes = new ArrayList<>();

    void addChangedTiles(ChunkCoordinates coord, List<LittleTile> placedTiles, List<LittleTile> removedTiles) {
        if (placedTiles.isEmpty() && removedTiles.isEmpty()) return;
        changes.add(new ChangeEntry(coord, placedTiles, removedTiles));
    }

    public boolean hasPlacedTiles() {
        for (ChangeEntry change : changes) {
            if (!change.placedTiles.isEmpty()) return true;
        }
        return false;
    }

    public boolean hasChanges() {
        return !changes.isEmpty();
    }

    public LittleTileChangePlan createPlan() {
        return new LittleTileChangePlan(changes);
    }

    public ArrayList<ChangeEntry> getChanges() {
        return new ArrayList<>(changes);
    }

}
