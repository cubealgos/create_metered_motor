package metered_motor.gametest;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import metered_motor.MeteredMotor;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import metered_motor.trade.RollFunction;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * MM-5: {@code metered_motor:roll}, decoded exactly as a trade file would write it and applied
 * through {@link net.minecraft.world.level.storage.loot.functions.LootItemFunction#apply}, lands
 * within its tier's bands; a malformed band is rejected in favour of the tier's default
 * (TRADE-REQ-002, TRADE-REQ-004).
 */
public final class RollGameTest {
    @GameTest
    public void aTierIiRollLandsWithinItsBands(GameTestHelper helper) {
        RollFunction function = decode(tierTwoJson());
        Stats stats = rolledStats(helper, function);
        helper.assertTrue(stats != null, "the roll wrote a stats component");
        if (stats != null) {
            helper.assertTrue(stats.tier() == Tier.II, "tier II rolled, was " + stats.tier());
            helper.assertTrue(withinInclusive(stats.rpm(), Tier.II.rpmBand().min(), Tier.II.rpmBand().max()),
                "rpm within tier II's band: " + stats.rpm());
            helper.assertTrue(withinInclusive(stats.capacity(), Tier.II.capacityBand().min(), Tier.II.capacityBand().max()),
                "capacity within tier II's band: " + stats.capacity());
            helper.assertTrue(withinInclusive(stats.efficiency(), Tier.II.efficiencyBand().min(), Tier.II.efficiencyBand().max()),
                "efficiency within tier II's band: " + stats.efficiency());
        }
        helper.succeed();
    }

    @GameTest
    public void aMalformedBandFallsBackToTheTierDefaultAndLogs(GameTestHelper helper) {
        JsonObject json = tierTwoJson();
        JsonObject malformedRpm = new JsonObject();
        malformedRpm.addProperty("min", 100.0);
        malformedRpm.addProperty("max", 10.0); // min > max: malformed per TRADE-REQ-004.
        json.add("rpm", malformedRpm);
        RollFunction function = decode(json);
        Stats stats = rolledStats(helper, function);
        helper.assertTrue(stats != null, "the roll wrote a stats component despite the malformed band");
        if (stats != null) {
            helper.assertTrue(withinInclusive(stats.rpm(), Tier.II.rpmBand().min(), Tier.II.rpmBand().max()),
                "the malformed rpm band fell back to tier II's default: " + stats.rpm());
        }
        helper.succeed();
    }

    private static boolean withinInclusive(double value, double min, double max) {
        return value >= min && value <= max;
    }

    private static Stats rolledStats(GameTestHelper helper, RollFunction function) {
        Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 2, 1));
        LootParams params = new LootParams.Builder(helper.getLevel())
            .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(villager.position()))
            .withParameter(LootContextParams.THIS_ENTITY, villager)
            .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
            .create(LootContextParamSets.VILLAGER_TRADE);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());
        ItemStack stack = function.apply(new ItemStack(Items.IRON_INGOT), context);
        return stack.get(MeteredMotor.STATS);
    }

    private static JsonObject tierTwoJson() {
        JsonObject json = new JsonObject();
        json.addProperty("function", "metered_motor:roll");
        json.addProperty("tier", 2);
        return json;
    }

    private static RollFunction decode(JsonObject json) {
        return RollFunction.MAP_CODEC.codec().parse(JsonOps.INSTANCE, json)
            .getOrThrow(message -> new AssertionError("metered_motor:roll decode: " + message));
    }
}
