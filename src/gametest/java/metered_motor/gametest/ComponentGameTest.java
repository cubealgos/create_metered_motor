package metered_motor.gametest;

import com.mojang.serialization.DynamicOps;
import metered_motor.MeteredMotor;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** MM-2: the stats component survives an item stack's save/parse round trip, and a newer version reads back intact and read-only (DATA-REQ-001). */
public final class ComponentGameTest {
    @GameTest
    public void theComponentRoundTripsThroughAnItemStack(GameTestHelper helper) {
        Stats stats = new Stats(Stats.VERSION, Tier.III);
        ItemStack stack = new ItemStack(Items.IRON_INGOT);
        stack.set(MeteredMotor.STATS, stats);
        DynamicOps<Tag> ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        Tag saved = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow(msg -> new AssertionError("encode: " + msg));
        ItemStack back = ItemStack.CODEC.parse(ops, saved).getOrThrow(msg -> new AssertionError("parse: " + msg));
        Stats read = back.get(MeteredMotor.STATS);
        helper.assertTrue(read != null && read.equals(stats), "the component round-trips: " + read);
        helper.succeed();
    }

    @GameTest
    public void aNewerVersionReadsBackIntactAndReadOnly(GameTestHelper helper) {
        Stats future = new Stats(Stats.VERSION + 1, Tier.I);
        ItemStack stack = new ItemStack(Items.IRON_INGOT);
        stack.set(MeteredMotor.STATS, future);
        DynamicOps<Tag> ops = helper.getLevel().registryAccess().createSerializationContext(NbtOps.INSTANCE);
        Tag saved = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow(msg -> new AssertionError("encode: " + msg));
        ItemStack back = ItemStack.CODEC.parse(ops, saved).getOrThrow(msg -> new AssertionError("parse: " + msg));
        Stats read = back.get(MeteredMotor.STATS);
        helper.assertTrue(read != null && read.equals(future), "a newer version is kept intact: " + read);
        helper.assertTrue(read != null && read.readOnly(), "a newer version reads back read-only");
        helper.succeed();
    }
}
