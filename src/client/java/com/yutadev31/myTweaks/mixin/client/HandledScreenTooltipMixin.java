package com.yutadev31.myTweaks.mixin.client;

import com.yutadev31.myTweaks.client.MyTweaksConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
abstract class HandledScreenTooltipMixin<T extends ScreenHandler> {
    private static final int STACK_SIZE = 64;
    private static final int SHULKER_BOX_STACK_COUNT = 27;
    private static final int SHULKER_BOX_ITEM_COUNT = STACK_SIZE * SHULKER_BOX_STACK_COUNT;

    @Shadow protected T handler;
    @Shadow protected Slot focusedSlot;

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    private void myTweaks$appendTotalCountTooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        if (!MyTweaksConfig.get().isItemTooltipTotalCountEnabled() || stack.isEmpty() || focusedSlot == null) {
            return;
        }

        ItemStack focusedStack = focusedSlot.getStack();
        if (focusedStack.isEmpty() || !ItemStack.areItemsAndComponentsEqual(stack, focusedStack)) {
            return;
        }

        ItemStack targetStack = resolveTargetStack(focusedStack);
        if (targetStack.isEmpty()) {
            return;
        }

        int totalCount = 0;
        for (Slot slot : handler.slots) {
            totalCount += countMatchingItems(slot.getStack(), targetStack);
        }

        List<Text> tooltip = new ArrayList<>(cir.getReturnValue());
        tooltip.add(Text.literal("現在の画面内合計: " + formatTotalCount(totalCount)).formatted(Formatting.DARK_GRAY));
        cir.setReturnValue(tooltip);
    }

    private static ItemStack resolveTargetStack(ItemStack hoveredStack) {
        if (hoveredStack.contains(DataComponentTypes.CONTAINER)) {
            return getSingleContainedItem(hoveredStack);
        }

        return hoveredStack;
    }

    private static int countMatchingItems(ItemStack stack, ItemStack targetStack) {
        if (stack.isEmpty()) {
            return 0;
        }

        int totalCount = 0;
        if (ItemStack.areItemsAndComponentsEqual(stack, targetStack)) {
            totalCount += stack.getCount();
        }

        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) {
            return totalCount;
        }

        for (ItemStack containedStack : container.iterateNonEmpty()) {
            totalCount += countMatchingItems(containedStack, targetStack) * stack.getCount();
        }
        return totalCount;
    }

    private static ItemStack getSingleContainedItem(ItemStack stack) {
        ContainerComponent container = stack.get(DataComponentTypes.CONTAINER);
        if (container == null) {
            return ItemStack.EMPTY;
        }

        ItemStack singleItem = ItemStack.EMPTY;
        for (ItemStack containedStack : container.iterateNonEmpty()) {
            if (singleItem.isEmpty()) {
                singleItem = containedStack.copyWithCount(1);
                continue;
            }

            if (!ItemStack.areItemsAndComponentsEqual(singleItem, containedStack)) {
                return ItemStack.EMPTY;
            }
        }

        return singleItem;
    }

    private static String formatTotalCount(int totalCount) {
        if (totalCount < STACK_SIZE) {
            return Integer.toString(totalCount);
        }

        int shulkerBoxes = totalCount / SHULKER_BOX_ITEM_COUNT;
        int remainder = totalCount % SHULKER_BOX_ITEM_COUNT;
        int stacks = remainder / STACK_SIZE;
        int items = remainder % STACK_SIZE;

        StringBuilder builder = new StringBuilder()
            .append(totalCount)
            .append(" (");
        boolean appended = false;

        if (shulkerBoxes > 0) {
            builder.append(shulkerBoxes).append("sb");
            appended = true;
        }
        if (stacks > 0) {
            if (appended) {
                builder.append(" + ");
            }
            builder.append(stacks).append("st");
            appended = true;
        }
        if (items > 0 || !appended) {
            if (appended) {
                builder.append(" + ");
            }
            builder.append(items);
        }

        return builder.append(')').toString();
    }
}
