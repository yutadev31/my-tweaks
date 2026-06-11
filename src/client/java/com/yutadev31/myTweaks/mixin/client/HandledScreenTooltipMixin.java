package com.yutadev31.myTweaks.mixin.client;

import com.yutadev31.myTweaks.client.MyTweaksConfig;
import java.util.ArrayList;
import java.util.List;
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

        int totalCount = 0;
        for (Slot slot : handler.slots) {
            ItemStack slotStack = slot.getStack();
            if (!slotStack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, slotStack)) {
                totalCount += slotStack.getCount();
            }
        }

        List<Text> tooltip = new ArrayList<>(cir.getReturnValue());
        tooltip.add(Text.literal("現在の画面内合計: " + totalCount).formatted(Formatting.DARK_GRAY));
        cir.setReturnValue(tooltip);
    }
}
