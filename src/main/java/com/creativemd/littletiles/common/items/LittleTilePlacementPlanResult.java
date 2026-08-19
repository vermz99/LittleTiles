package com.creativemd.littletiles.common.items;

/**
 * Result of applying a {@link LittleTilePlacementPlan}.
 */
public final class LittleTilePlacementPlanResult {

    private final boolean placedTiles;

    public LittleTilePlacementPlanResult(boolean placedTiles) {
        this.placedTiles = placedTiles;
    }

    public boolean hasPlacedTiles() {
        return placedTiles;
    }
}
