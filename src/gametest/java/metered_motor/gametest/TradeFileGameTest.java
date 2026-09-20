package metered_motor.gametest;

import java.util.Map;
import java.util.Optional;
import metered_motor.MeteredMotor;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * MM-5: the three toolsmith trade files resolve through {@code Registries.VILLAGER_TRADE} and each
 * is tagged into its toolsmith level (TRADE-REQ-001, TRADE-REQ-003). MM-21
 * (`decisions/DEC-009-fixed-tiers.md`, `TRADE-REQ-002` withdrawn): each trade's {@code gives}
 * item template now carries the tier's stats component directly, and {@code metered_motor:roll}
 * is no longer registered as a loot function type at all — nothing rolled, nothing to register.
 */
public final class TradeFileGameTest {
    private static final Map<String, Tagged> TRADES = Map.of(
        "toolsmith/3/emerald_metered_motor_i", new Tagged(VillagerTradeTags.TOOLSMITH_LEVEL_3, Tier.I),
        "toolsmith/4/emerald_metered_motor_ii", new Tagged(VillagerTradeTags.TOOLSMITH_LEVEL_4, Tier.II),
        "toolsmith/5/emerald_metered_motor_iii", new Tagged(VillagerTradeTags.TOOLSMITH_LEVEL_5, Tier.III)
    );

    private record Tagged(TagKey<VillagerTrade> tag, Tier tier) {
    }

    @GameTest
    public void theThreeTradesResolveAndAreTaggedIntoTheirLevel(GameTestHelper helper) {
        Registry<VillagerTrade> registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE);
        TRADES.forEach((path, tagged) -> {
            Holder.Reference<VillagerTrade> holder = registry.get(MeteredMotor.id(path)).orElse(null);
            helper.assertTrue(holder != null, path + " resolves in the villager_trade registry");
            if (holder != null) {
                helper.assertTrue(holder.is(tagged.tag()), path + " is tagged into " + tagged.tag().location());
            }
        });
        helper.succeed();
    }

    /**
     * TRADE-REQ-002 (withdrawn), TRADE-DEC-003: each trade's {@code gives} template alone
     * determines the result — {@code VillagerTrade.getOffer} builds it with {@code gives.create()}
     * then applies every {@code given_item_modifiers} entry in order (full bytecode disassembly);
     * none of our trade files list any, so two offers created from two different, independently
     * random {@link LootContext}s must be identical: proof nothing is rolled at offer creation.
     */
    @GameTest
    public void eachTradeGivesItsFixedTierWithNoItemModifiers(GameTestHelper helper) {
        Registry<VillagerTrade> registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE);
        TRADES.forEach((path, tagged) -> {
            VillagerTrade trade = registry.get(MeteredMotor.id(path)).map(Holder.Reference::value).orElse(null);
            helper.assertTrue(trade != null, "set up: " + path + " resolves");
            if (trade == null) {
                return;
            }

            MerchantOffer first = offerFrom(helper, trade, "first-" + path);
            MerchantOffer second = offerFrom(helper, trade, "second-" + path);
            helper.assertTrue(first != null && second != null, path + " creates an offer for a villager with no existing offers");
            if (first == null || second == null) {
                return;
            }

            ItemStack firstResult = first.getResult();
            ItemStack secondResult = second.getResult();
            Stats firstStats = firstResult.get(MeteredMotor.STATS);
            Stats secondStats = secondResult.get(MeteredMotor.STATS);
            helper.assertTrue(firstStats != null, path + " gives an item carrying the stats component: " + firstResult);
            if (firstStats == null) {
                return;
            }
            helper.assertTrue(firstStats.tier() == tagged.tier(), path + " gives tier " + tagged.tier() + ", was " + firstStats.tier());
            helper.assertTrue(firstStats.version() == Stats.VERSION, path + " gives the current component version, was " + firstStats.version());
            helper.assertTrue(firstStats.equals(secondStats),
                path + " gives an identical component every time — nothing is rolled: " + firstStats + " vs " + secondStats);
        });
        helper.succeed();
    }

    @GameTest
    public void theRollLootFunctionTypeIsNoLongerRegistered(GameTestHelper helper) {
        helper.assertTrue(
            BuiltInRegistries.LOOT_FUNCTION_TYPE.getOptional(MeteredMotor.id("roll")).isEmpty(),
            "metered_motor:roll is withdrawn (DEC-009): no loot function type is registered at that id");
        helper.succeed();
    }

    private static MerchantOffer offerFrom(GameTestHelper helper, VillagerTrade trade, String name) {
        Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 2, 1));
        LootParams params = new LootParams.Builder(helper.getLevel())
            .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(villager.position()))
            .withParameter(LootContextParams.THIS_ENTITY, villager)
            .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
            .create(LootContextParamSets.VILLAGER_TRADE);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        return trade.getOffer(context);
    }
}
