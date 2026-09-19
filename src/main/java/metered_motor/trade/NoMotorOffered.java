package metered_motor.trade;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import metered_motor.MeteredMotor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

/**
 * The {@code metered_motor:no_motor_offered} merchant predicate: true unless the merchant that
 * {@code LootContextParams.THIS_ENTITY} names already offers a metered motor (TRADE-REQ-006,
 * TRADE-DEC-004). Verified against 26.2 (this ticket's first step, recorded in
 * {@code .gitkontor/items/*&#47;item.md} key MM-5): {@code VillagerTrade.getOffer} tests the
 * merchant predicate against the same {@link LootContext} {@code AbstractVillager.addOffersFromTradeSet}
 * builds with {@code THIS_ENTITY} set to the villager and {@code ADDITIONAL_COST_COMPONENT_ALLOWED}
 * set; the {@code MerchantOffers} instance drawn into is the villager's own live {@code getOffers()}
 * list, appended to trade-by-trade, including earlier trades drawn in the same level's batch, so no
 * villager-hook fallback is needed.
 */
public final class NoMotorOffered implements LootItemCondition {
    public static final MapCodec<NoMotorOffered> MAP_CODEC = MapCodec.unit(NoMotorOffered::new);

    @Override
    public MapCodec<NoMotorOffered> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.THIS_ENTITY);
    }

    @Override
    public boolean test(LootContext context) {
        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Merchant merchant)) {
            return true;
        }
        Item motor = BuiltInRegistries.ITEM.getOptional(MeteredMotor.id("metered_motor")).orElse(null);
        if (motor == null) {
            return true;
        }
        for (MerchantOffer offer : merchant.getOffers()) {
            if (offer.getResult().getItem() == motor) {
                return false;
            }
        }
        return true;
    }
}
