package com.yutadev31.myTweaks.mixin.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantScreen.class)
abstract class MerchantScreenMixin extends HandledScreen<MerchantScreenHandler> {
    private static final String LIBRARIAN_JA = "司書";
    private static final String LIBRARIAN_EN = "Librarian";
    private static final int INFO_MARGIN_X = 6;
    private static final int INFO_OFFSET_Y = 26;
    private static final int INFO_LINE_HEIGHT = 10;
    private static final int INFO_BOX_MIN_WIDTH = 84;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private MerchantScreenMixin(MerchantScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "renderMain", at = @At("TAIL"))
    private void myTweaks$drawLibrarianGachaInfo(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (!isLibrarianScreen()) {
            return;
        }

        StatusDisplay status = buildStatusDisplay(handler.getRecipes());
        int contentWidth = Math.max(textRenderer.getWidth(status.line1()), textRenderer.getWidth(status.line2()));
        int boxWidth = Math.max(INFO_BOX_MIN_WIDTH, contentWidth) + INFO_MARGIN_X * 2;
        int boxHeight = status.line2().getString().isEmpty() ? 16 : 16 + INFO_LINE_HEIGHT;
        int drawX = (this.width - boxWidth) / 2 + INFO_MARGIN_X;
        int drawY = Math.max(4, this.y - INFO_OFFSET_Y);

        context.fill(drawX - INFO_MARGIN_X, drawY - INFO_MARGIN_X, drawX + boxWidth - INFO_MARGIN_X, drawY + boxHeight - INFO_MARGIN_X, 0xA0101010);
        context.drawTextWithShadow(textRenderer, status.line1(), drawX, drawY, TEXT_COLOR);
        if (!status.line2().getString().isEmpty()) {
            context.drawTextWithShadow(textRenderer, status.line2(), drawX, drawY + INFO_LINE_HEIGHT, TEXT_COLOR);
        }
    }

    private boolean isLibrarianScreen() {
        String titleText = this.title.getString();
        return titleText.contains(LIBRARIAN_JA) || titleText.contains(LIBRARIAN_EN);
    }

    private static StatusDisplay buildStatusDisplay(TradeOfferList offers) {
        TradeOffer offer = findEnchantedBookOffer(offers);
        if (offer == null) {
            return new StatusDisplay(Text.literal("本なし").formatted(Formatting.RED), Text.empty());
        }

        ItemStack sellItem = offer.getSellItem();
        EnchantmentInfo enchantmentInfo = getEnchantmentInfo(sellItem);
        if (enchantmentInfo == null) {
            return new StatusDisplay(Text.literal("本 判定不可").formatted(Formatting.RED), Text.empty());
        }

        int maxLevel = enchantmentInfo.enchantment().value().getMaxLevel();
        int price = offer.getOriginalFirstBuyItem().getCount();
        int theoreticalMin = getTheoreticalMinPrice(enchantmentInfo.enchantment(), enchantmentInfo.level());
        boolean isBestLevel = enchantmentInfo.level() >= maxLevel;
        boolean isMinPrice = price == theoreticalMin;

        MutableText summary = Text.empty();
        if (isBestLevel) {
            summary.append(Text.literal("最高レベル").formatted(Formatting.GREEN));
        }

        if (isMinPrice) {
            if (isBestLevel) {
                summary.append(" & ");
            }
            summary.append(Text.literal("理論値").formatted(Formatting.GREEN));
        }

        MutableText detail = Text.empty()
                .append(enchantmentInfo.enchantment().value().description().copy().formatted(Formatting.AQUA))
                .append(Text.literal(" " + enchantmentInfo.level() + "/" + maxLevel + "lv ").formatted(isBestLevel ? Formatting.GREEN : Formatting.RED))
                .append(Text.literal(price + "/" + theoreticalMin + "e").formatted(isMinPrice ? Formatting.GREEN : Formatting.RED));
        return new StatusDisplay(summary, detail);
    }

    private static TradeOffer findEnchantedBookOffer(TradeOfferList offers) {
        int offerCount = Math.min(2, offers.size());
        for (int i = 0; i < offerCount; i++) {
            TradeOffer offer = offers.get(i);
            if (offer.getSellItem().isOf(Items.ENCHANTED_BOOK)) {
                return offer;
            }
        }
        return null;
    }

    private static EnchantmentInfo getEnchantmentInfo(ItemStack stack) {
        ItemEnchantmentsComponent enchantments = stack.getOrDefault(
                DataComponentTypes.STORED_ENCHANTMENTS,
                ItemEnchantmentsComponent.DEFAULT
        );
        for (var entry : enchantments.getEnchantmentEntries()) {
            return new EnchantmentInfo(entry.getKey(), entry.getIntValue());
        }
        return null;
    }

    private static int getTheoreticalMinPrice(RegistryEntry<Enchantment> enchantment, int level) {
        int price = 2 + 3 * level;
        if (enchantment.isIn(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
            price *= 2;
        }
        return Math.min(price, 64);
    }

    private record EnchantmentInfo(RegistryEntry<Enchantment> enchantment, int level) {
    }

    private record StatusDisplay(Text line1, Text line2) {
    }
}
