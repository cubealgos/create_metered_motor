package metered_motor.gametest;

import metered_motor.MeteredMotor;
import metered_motor.block.MotorBlocks;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

/**
 * MM-9: the block and its item resolve at their documented ids and the item is stackable to
 * exactly one, so a rolled item's stats never merge with another's (MOTOR-REQ-001). The other
 * halves of MOTOR-REQ-001 — the stats component's shape, and the block and item existing at all —
 * are already exercised incidentally by every game test that places or breaks a motor; this test
 * covers the one clause none of them asserts directly.
 */
public final class RegistrationGameTest {
    @GameTest
    public void theBlockAndItemResolveAtTheirIdsAndTheItemIsStackableToOne(GameTestHelper helper) {
        helper.assertTrue(
            BuiltInRegistries.BLOCK.getOptional(MeteredMotor.id("metered_motor")).orElse(null) == MotorBlocks.BLOCK,
            "the block resolves at metered_motor:metered_motor");
        helper.assertTrue(
            BuiltInRegistries.ITEM.getOptional(MeteredMotor.id("metered_motor")).orElse(null) == MotorBlocks.ITEM,
            "the item resolves at metered_motor:metered_motor");

        ItemStack stack = new ItemStack(MotorBlocks.ITEM, 1);
        helper.assertTrue(stack.getMaxStackSize() == 1, "the item is stackable to one: " + stack.getMaxStackSize());
        helper.succeed();
    }
}
