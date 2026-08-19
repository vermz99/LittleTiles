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

    private final ArrayList<PlacedBlock> placedBlocks = new ArrayList<>();

    void addPlacedTiles(ChunkCoordinates coord, List<LittleTile> tiles) {
        if (tiles.isEmpty()) return;
        placedBlocks.add(new PlacedBlock(coord, tiles));
    }

    public boolean hasPlacedTiles() {
        return !placedBlocks.isEmpty();
    }

    public ArrayList<PlacedBlock> getPlacedBlocks() {
        return new ArrayList<>(placedBlocks);
    }

    public static final class PlacedBlock {

        private final ChunkCoordinates coord;
        private final ArrayList<NBTTagCompound> tiles = new ArrayList<>();

        private PlacedBlock(ChunkCoordinates coord, List<LittleTile> placedTiles) {
            this.coord = new ChunkCoordinates(coord.posX, coord.posY, coord.posZ);
            for (LittleTile tile : placedTiles) {
                NBTTagCompound nbt = new NBTTagCompound();
                tile.saveTile(nbt);
                tiles.add(nbt);
            }
        }

        public ChunkCoordinates getCoord() {
            return new ChunkCoordinates(coord.posX, coord.posY, coord.posZ);
        }

        public ArrayList<NBTTagCompound> getTiles() {
            ArrayList<NBTTagCompound> result = new ArrayList<>();
            for (NBTTagCompound tile : tiles) {
                result.add((NBTTagCompound) tile.copy());
            }
            return result;
        }
    }
}
