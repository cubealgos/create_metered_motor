package metered_motor.block;

import com.zurrtum.create.AllCreativeModeTabs;
import metered_motor.MeteredMotor;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * Registers the metered motor block, its item and its block entity type, and lists the item in
 * Create's base creative tab (MOTOR-REQ-001, MOTOR-REQ-002). The mod's own registrations beyond
 * the stats component live here so {@link MeteredMotor#onInitialize()} stays a one-line call per
 * add-on, keeping this file the single place another ticket's own registrations must not collide
 * with.
 */
public final class MotorBlocks {
    public static final ResourceKey<Block> BLOCK_KEY = ResourceKey.create(Registries.BLOCK, MeteredMotor.id("metered_motor"));
    public static final ResourceKey<Item> ITEM_KEY = ResourceKey.create(Registries.ITEM, MeteredMotor.id("metered_motor"));

    /** The metered motor block: a directional kinetic source (MOTOR-REQ-001, MOTOR-REQ-002, ARCH-DEC-002). */
    public static final MeteredMotorBlock BLOCK = Registry.register(BuiltInRegistries.BLOCK, BLOCK_KEY,
        new MeteredMotorBlock(BlockBehaviour.Properties.of()
            .setId(BLOCK_KEY)
            .mapColor(MapColor.COLOR_ORANGE)
            .strength(5.0f, 6.0f)
            .sound(SoundType.COPPER)
            .requiresCorrectToolForDrops()));

    /** The block's item, stackable to one so its rolled stats never merge (MOTOR-REQ-001). */
    public static final MeteredMotorItem ITEM = Registry.register(BuiltInRegistries.ITEM, ITEM_KEY,
        new MeteredMotorItem(BLOCK, new Item.Properties()
            .setId(ITEM_KEY)
            .stacksTo(1)
            .useBlockDescriptionPrefix()));

    /** The block entity type Create's kinetic network ticks (docs/spec/README.md Verification 7). */
    public static final BlockEntityType<MeteredMotorBlockEntity> BLOCK_ENTITY_TYPE = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE, MeteredMotor.id("metered_motor"),
        FabricBlockEntityTypeBuilder.create(MeteredMotorBlockEntity::new, BLOCK).build());

    private MotorBlocks() {
    }

    /**
     * Forces this class's static registrations to run and lists the item in Create's base creative
     * tab; call once from {@link MeteredMotor#onInitialize()}.
     */
    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(AllCreativeModeTabs.BASE_GROUP).register(output ->
            output.accept(new ItemStack(ITEM), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
    }
}
