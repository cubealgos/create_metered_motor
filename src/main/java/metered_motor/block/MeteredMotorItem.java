package metered_motor.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/** The metered motor's block item, stackable to one so two different tiers never merge (MOTOR-REQ-001). */
public final class MeteredMotorItem extends BlockItem {
    public MeteredMotorItem(Block block, Item.Properties properties) {
        super(block, properties);
    }
}
