package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

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

    public ArrayList<ChangeEntry> getChanges() {
        return new ArrayList<>(changes);
    }

    public static final class ChangeEntry {

        private final ChunkCoordinates coord;
        private final ArrayList<NBTTagCompound> placedTiles;
        private final ArrayList<NBTTagCompound> removedTiles;

        private ChangeEntry(ChunkCoordinates coord, List<LittleTile> placedTiles, List<LittleTile> removedTiles) {
            this.coord = new ChunkCoordinates(coord.posX, coord.posY, coord.posZ);
            this.placedTiles = saveTiles(placedTiles);
            this.removedTiles = saveTiles(removedTiles);
        }

        public ChunkCoordinates getCoord() {
            return new ChunkCoordinates(coord.posX, coord.posY, coord.posZ);
        }

        public ArrayList<NBTTagCompound> getPlacedTiles() {
            return copyTiles(placedTiles);
        }

        public ArrayList<NBTTagCompound> getRemovedTiles() {
            return copyTiles(removedTiles);
        }

        private static ArrayList<NBTTagCompound> saveTiles(List<LittleTile> tiles) {
            ArrayList<NBTTagCompound> result = new ArrayList<>();
            for (LittleTile tile : tiles) {
                NBTTagCompound nbt = new NBTTagCompound();
                tile.saveTile(nbt);
                result.add(nbt);
            }
            return result;
        }

        private static ArrayList<NBTTagCompound> copyTiles(List<NBTTagCompound> tiles) {
            ArrayList<NBTTagCompound> result = new ArrayList<>();
            for (NBTTagCompound tile : tiles) {
                result.add((NBTTagCompound) tile.copy());
            }
            return result;
        }
    }
}
