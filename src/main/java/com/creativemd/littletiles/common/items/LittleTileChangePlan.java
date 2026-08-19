package com.creativemd.littletiles.common.items;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

import com.creativemd.littletiles.LittleTiles;
import com.creativemd.littletiles.common.blocks.BlockTile;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.utils.LittleTile;

public final class LittleTileChangePlan {

    private final ArrayList<ChangeEntry> changes;

    public LittleTileChangePlan(List<ChangeEntry> changes) {
        this.changes = new ArrayList<>();
        for (ChangeEntry change : changes) {
            this.changes.add(change.copy());
        }
    }

    public LittleTileChangePlan invert() {
        ArrayList<ChangeEntry> inverted = new ArrayList<>();
        for (ChangeEntry change : changes) {
            inverted.add(change.invert());
        }
        return new LittleTileChangePlan(inverted);
    }

    public ArrayList<ChunkCoordinates> getPlannedCoords() {
        ArrayList<ChunkCoordinates> coords = new ArrayList<>();
        for (ChangeEntry change : changes) {
            coords.add(change.getCoord());
        }
        return coords;
    }

    public boolean canApply(World world) {
        if (world == null || changes.isEmpty()) return false;

        for (ChangeEntry change : changes) {
            ChunkCoordinates coord = change.getCoord();
            TileEntityLittleTiles tile = getTileEntity(world, coord);
            Block block = world.getBlock(coord.posX, coord.posY, coord.posZ);

            if (tile == null && (block instanceof BlockTile || !block.getMaterial().isReplaceable())) return false;

            ArrayList<LittleTile> remainingTiles = tile == null ? new ArrayList<LittleTile>()
                    : new ArrayList<>(tile.getTiles());

            for (NBTTagCompound removedNbt : change.getRemovedTiles()) {
                LittleTile removed = findMatchingTile(remainingTiles, removedNbt);
                if (removed != null) {
                    remainingTiles.remove(removed);
                } else if (!isTileSpaceEmpty(world, coord, remainingTiles, removedNbt)) {
                    return false;
                }
            }

            TileEntityLittleTiles collisionTile = createTemporaryTileEntity(world, coord, remainingTiles);
            for (NBTTagCompound placedNbt : change.getPlacedTiles()) {
                LittleTile placed = LittleTile.CreateandLoadTile(collisionTile, world, placedNbt);
                if (placed == null || placed.boundingBox == null
                        || !collisionTile.isSpaceForLittleTile(placed.boundingBox, placed.getCutoutInfo()))
                    return false;
                collisionTile.getTiles().add(placed);
            }
        }
        return true;
    }

    public boolean apply(World world) {
        if (!canApply(world)) return false;

        for (ChangeEntry change : changes) {
            ChunkCoordinates coord = change.getCoord();
            TileEntityLittleTiles tile = getTileEntity(world, coord);
            if (tile == null) {
                if (change.getPlacedTiles().isEmpty()) continue;
                world.setBlock(coord.posX, coord.posY, coord.posZ, LittleTiles.blockTile, 0, 3);
                tile = getTileEntity(world, coord);
            }
            if (tile == null) return false;

            ArrayList<LittleTile> removedTiles = new ArrayList<>();
            ArrayList<LittleTile> removalCandidates = new ArrayList<>(tile.getTiles());
            for (NBTTagCompound removedNbt : change.getRemovedTiles()) {
                LittleTile removed = findMatchingTile(removalCandidates, removedNbt);
                if (removed != null) {
                    removedTiles.add(removed);
                    removalCandidates.remove(removed);
                } else if (!isTileSpaceEmpty(world, coord, removalCandidates, removedNbt)) {
                    return false;
                }
            }
            for (LittleTile removed : removedTiles) {
                tile.removeTile(removed, false);
            }

            for (NBTTagCompound placedNbt : change.getPlacedTiles()) {
                LittleTile placed = LittleTile.CreateandLoadTile(tile, world, placedNbt);
                if (placed == null) return false;
                placed.place();
            }
            tile.updateTiles();
        }
        return true;
    }

    private static LittleTile findMatchingTile(List<LittleTile> tiles, NBTTagCompound expectedNbt) {
        for (LittleTile tile : tiles) {
            NBTTagCompound currentNbt = new NBTTagCompound();
            tile.saveTile(currentNbt);
            if (currentNbt.equals(expectedNbt)) return tile;
        }
        return null;
    }

    private static boolean isTileSpaceEmpty(World world, ChunkCoordinates coord, List<LittleTile> tiles,
            NBTTagCompound expectedNbt) {
        TileEntityLittleTiles collisionTile = createTemporaryTileEntity(world, coord, tiles);
        LittleTile expected = LittleTile.CreateandLoadTile(collisionTile, world, expectedNbt);
        return expected != null && expected.boundingBox != null
                && collisionTile.isSpaceForLittleTile(expected.boundingBox, expected.getCutoutInfo());
    }

    private static TileEntityLittleTiles createTemporaryTileEntity(World world, ChunkCoordinates coord,
            List<LittleTile> tiles) {
        TileEntityLittleTiles tile = new TileEntityLittleTiles();
        tile.setWorldObj(world);
        tile.xCoord = coord.posX;
        tile.yCoord = coord.posY;
        tile.zCoord = coord.posZ;
        tile.setTiles(new ArrayList<>(tiles));
        return tile;
    }

    private static TileEntityLittleTiles getTileEntity(World world, ChunkCoordinates coord) {
        TileEntity tile = world.getTileEntity(coord.posX, coord.posY, coord.posZ);
        if (tile instanceof TileEntityLittleTiles littleTile) return littleTile;
        return null;
    }

    public static final class ChangeEntry {

        private final ChunkCoordinates coord;
        private final ArrayList<NBTTagCompound> placedTiles;
        private final ArrayList<NBTTagCompound> removedTiles;

        ChangeEntry(ChunkCoordinates coord, List<LittleTile> placedTiles, List<LittleTile> removedTiles) {
            this.coord = new ChunkCoordinates(coord.posX, coord.posY, coord.posZ);
            this.placedTiles = saveTiles(placedTiles);
            this.removedTiles = saveTiles(removedTiles);
        }

        private ChangeEntry(ChunkCoordinates coord, ArrayList<NBTTagCompound> placedTiles,
                ArrayList<NBTTagCompound> removedTiles) {
            this.coord = new ChunkCoordinates(coord.posX, coord.posY, coord.posZ);
            this.placedTiles = copyTiles(placedTiles);
            this.removedTiles = copyTiles(removedTiles);
        }

        private ChangeEntry copy() {
            return new ChangeEntry(coord, placedTiles, removedTiles);
        }

        private ChangeEntry invert() {
            return new ChangeEntry(coord, removedTiles, placedTiles);
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

        public boolean hasPlacedTiles() {
            return !placedTiles.isEmpty();
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
