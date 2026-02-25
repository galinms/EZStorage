package com.zerofall.ezstorage.nei;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.zerofall.ezstorage.EZStorage;
import com.zerofall.ezstorage.gui.GuiStorageCore;
import com.zerofall.ezstorage.network.client.MsgReqCrafting;
import com.zerofall.ezstorage.util.EZInventory;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.GuiOverlayButton.ItemOverlayState;
import codechicken.nei.recipe.IRecipeHandler;

public class NeiCraftingOverlay implements IOverlayHandler {

    @Override
    public void overlayRecipe(final GuiContainer gui, final IRecipeHandler recipe, final int recipeIndex,
        final boolean shift) {
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);
        overlayRecipe(gui, ingredients);
    }

    public void overlayRecipe(final GuiContainer gui, final List<PositionedStack> ingredients) {
        if (!(gui instanceof com.zerofall.ezstorage.gui.GuiCraftingCore)) {
            return;
        }

        final NBTTagCompound recipe = new NBTTagCompound();

        for (final PositionedStack positionedStack : ingredients) {
            if (positionedStack == null || positionedStack.items == null || positionedStack.items.length == 0) {
                continue;
            }

            final int col = (positionedStack.relx - 25) / 18;
            final int row = (positionedStack.rely - 6) / 18;

            for (final Slot slot : gui.inventorySlots.inventorySlots) {
                if (!(slot.inventory instanceof InventoryCrafting) || slot.getSlotIndex() != col + row * 3) {
                    continue;
                }

                final NBTTagList tags = new NBTTagList();

                final List<ItemStack> list = new LinkedList<>(Arrays.asList(positionedStack.items));

                for (final ItemStack is : list) {
                    final NBTTagCompound tag = new NBTTagCompound();
                    is.writeToNBT(tag);
                    tags.appendTag(tag);
                }

                recipe.setTag("#" + slot.getSlotIndex(), tags);
                break;
            }
        }

        EZStorage.instance.network.sendToServer(new MsgReqCrafting(recipe));
    }

    @Override
    public List<ItemOverlayState> presenceOverlay(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex) {
        List<ItemStack> invStacks = new ArrayList<>();

        // Collect slots of storage core
        if (firstGui instanceof GuiStorageCore coreGui) {
            EZInventory inventory = coreGui.getInventory();
            if (inventory != null) {
                invStacks.addAll(
                    inventory.inventory.stream()
                        .map(ItemStack::copy)
                        .collect(Collectors.toCollection(ArrayList::new)));
            }
        }

        // Collect slots of player inventory
        invStacks
            .addAll(getFromInventory(firstGui.mc.thePlayer.inventoryContainer.inventorySlots, firstGui.mc.thePlayer));

        final List<ItemOverlayState> itemPresenceSlots = new ArrayList<>();
        final List<PositionedStack> ingredients = recipe.getIngredientStacks(recipeIndex);

        for (PositionedStack stack : ingredients) {
            Optional<ItemStack> used = invStacks.stream()
                .filter(is -> is.stackSize > 0 && isStackContains(stack, is))
                .findAny();

            itemPresenceSlots.add(new ItemOverlayState(stack, used.isPresent()));

            used.ifPresent(is -> is.stackSize -= 1);
        }
        return itemPresenceSlots;
    }

    private boolean isStackContains(PositionedStack stack, ItemStack is) {
        for (ItemStack s : stack.items) if (NEIServerUtils.areStacksSameTypeCraftingWithNBT(s, is)) return true;
        return false;
    }

    private List<ItemStack> getFromInventory(List<Slot> inventorySlots, EntityClientPlayerMP thePlayer) {
        return inventorySlots.stream()
            .filter(
                s -> s != null && s.getStack() != null
                    && s.getStack().stackSize > 0
                    && s.isItemValid(s.getStack())
                    && s.canTakeStack(thePlayer))
            .map(
                s -> s.getStack()
                    .copy())
            .collect(Collectors.toCollection(ArrayList::new));
    }
}
