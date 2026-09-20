package metered_motor.gametest;

import metered_motor.MeteredMotor;
import metered_motor.block.MotorBlocks;
import metered_motor.block.MotorTier;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/**
 * {@link MotorTier#of(ItemStack)} is the mapping {@code metered_motor.client.visual.TierProperty}
 * (a client-only select item model property, not game-testable directly) delegates to for the
 * item model's tier case (MOTOR-REQ-013, MOTOR-DEC-004, TRADE-REQ-005): exercised headlessly here
 * against the same server-safe method, since {@code src/test} cannot construct an {@link
 * ItemStack} without the game's registries bootstrapped (docs/spec/operations/testing.md).
 */
public final class TierPropertyGameTest {
    @GameTest
    public void eachRolledTierMapsToItsOwnModelCase(GameTestHelper helper) {
        for (Tier tier : Tier.values()) {
            ItemStack stack = new ItemStack(MotorBlocks.ITEM);
            stack.set(MeteredMotor.STATS, Stats.middleOf(tier));
            MotorTier mapped = MotorTier.of(stack);
            helper.assertTrue(mapped == MotorTier.of(tier),
                "tier " + tier + " maps to its own model case, was " + mapped);
        }
        helper.succeed();
    }

    @GameTest
    public void anUnrolledStackMapsToTierI(GameTestHelper helper) {
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        MotorTier mapped = MotorTier.of(stack);
        helper.assertTrue(mapped == MotorTier.I, "no component maps to tier I, was " + mapped);
        helper.succeed();
    }

    @GameTest
    public void aReadOnlyNewerComponentMapsToTierI(GameTestHelper helper) {
        Stats newerVersion = new Stats(Stats.VERSION + 1, Tier.III, 200, 18_432, 0.75);
        helper.assertTrue(newerVersion.readOnly(), "set up: a newer version is read-only");
        ItemStack stack = new ItemStack(MotorBlocks.ITEM);
        stack.set(MeteredMotor.STATS, newerVersion);
        MotorTier mapped = MotorTier.of(stack);
        helper.assertTrue(mapped == MotorTier.I,
            "a read-only newer component never shows a tier it cannot vouch for, was " + mapped);
        helper.succeed();
    }
}
