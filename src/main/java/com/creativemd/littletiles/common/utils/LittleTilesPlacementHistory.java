package com.creativemd.littletiles.common.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.creativemd.littletiles.common.items.LittleTileChangePlan;

public final class LittleTilesPlacementHistory {

    private static final int MAX_HISTORY = 30;
    private static final HashMap<UUID, Deque<PlacementAction>> undoHistory = new HashMap<>();
    private static final HashMap<UUID, Deque<PlacementAction>> redoHistory = new HashMap<>();

    private LittleTilesPlacementHistory() {}

    public static void recordPlacement(EntityPlayer player, PlacementAction action) {
        if (player == null || action == null || action.plan == null) {
            return;
        }

        UUID id = player.getUniqueID();
        Deque<PlacementAction> undoStack = getStack(undoHistory, id);
        if (player.capabilities.isCreativeMode) {
            undoStack.push(action);
            trimStack(undoStack);
        } else {
            // Non-creative players only keep their latest place action.
            undoStack.clear();
            undoStack.push(action);
        }
        getStack(redoHistory, id).clear();
    }

    public static void undo(EntityPlayer player) {
        if (player == null) {
            return;
        }

        UUID id = player.getUniqueID();
        Deque<PlacementAction> undoStack = getStack(undoHistory, id);
        if (undoStack.isEmpty()) {
            return;
        }

        PlacementAction action = undoStack.peek();
        if (!action.applyUndo(player)) {
            return;
        }
        undoStack.pop();

        if (player.capabilities.isCreativeMode) {
            Deque<PlacementAction> redoStack = getStack(redoHistory, id);
            redoStack.push(action);
            trimStack(redoStack);
        } else {
            getStack(redoHistory, id).clear();
        }
    }

    public static void redo(EntityPlayer player) {
        if (player == null || !player.capabilities.isCreativeMode) {
            return;
        }

        UUID id = player.getUniqueID();
        Deque<PlacementAction> redoStack = getStack(redoHistory, id);
        if (redoStack.isEmpty()) {
            return;
        }

        PlacementAction action = redoStack.peek();
        if (!action.applyRedo(player)) {
            return;
        }
        redoStack.pop();

        Deque<PlacementAction> undoStack = getStack(undoHistory, id);
        undoStack.push(action);
        trimStack(undoStack);
    }

    public static final class PlacementAction {

        public final int dimensionId;
        public final LittleTileChangePlan plan;

        public PlacementAction(int dimensionId, LittleTileChangePlan plan) {
            this.dimensionId = dimensionId;
            this.plan = plan;
        }

        private boolean applyUndo(EntityPlayer player) {
            World world = player.worldObj;
            if (world == null || world.provider == null || world.provider.dimensionId != dimensionId) {
                return false;
            }

            return plan.invert().apply(world);
        }

        private boolean applyRedo(EntityPlayer player) {
            World world = player.worldObj;
            if (world == null || world.provider == null || world.provider.dimensionId != dimensionId) {
                return false;
            }

            return plan.apply(world);
        }
    }

    private static Deque<PlacementAction> getStack(HashMap<UUID, Deque<PlacementAction>> storage, UUID id) {
        Deque<PlacementAction> stack = storage.get(id);
        if (stack == null) {
            stack = new ArrayDeque<>();
            storage.put(id, stack);
        }
        return stack;
    }

    private static void trimStack(Deque<PlacementAction> stack) {
        while (stack.size() > MAX_HISTORY) {
            stack.removeLast();
        }
    }

}
