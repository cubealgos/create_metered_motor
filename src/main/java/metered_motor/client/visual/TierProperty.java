package metered_motor.client.visual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import metered_motor.block.MotorTier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * The {@code metered_motor:tier} select item model property: picks the item model's tier case
 * (MOTOR-REQ-013, MOTOR-DEC-004) so the item shows its rolled tier's casing colour in the
 * inventory, in hand and in the trade screen (TRADE-REQ-005), not just on the placed block.
 *
 * <p>Stateless, like vanilla's own {@code DisplayContext} property (no JSON fields beyond
 * {@code "property": "metered_motor:tier"}), so the {@link #TYPE}'s property codec is {@link
 * MapCodec#unit}. The mapping itself is {@link MotorTier#of(ItemStack)}, a server-safe pure
 * function shared with {@code MeteredMotorBlock#getStateForPlacement} so the block and the item
 * model never disagree about what an unrolled or newer-than-this-build stack shows.
 *
 * <p>Registered under {@code metered_motor:tier} from {@link MotorVisuals#register()} through
 * {@link metered_motor.mixin.client.SelectItemModelPropertiesAccessor}: vanilla's own {@code
 * SelectItemModelProperties.ID_MAPPER} (an {@code ExtraCodecs.LateBoundIdMapper}, publicly
 * mutable through its own {@code put}) is a private static field with no public registration
 * hook for an add-on, unlike a real {@code Registry} — reached with the smallest possible mixin,
 * a static accessor, rather than the ticket's {@code custom_model_data} fallback.
 */
public final class TierProperty implements SelectItemModelProperty<MotorTier> {
    public static final Codec<MotorTier> VALUE_CODEC = StringRepresentable.fromEnum(MotorTier::values);
    public static final SelectItemModelProperty.Type<TierProperty, MotorTier> TYPE =
        SelectItemModelProperty.Type.create(MapCodec.unit(new TierProperty()), VALUE_CODEC);

    @Override
    public MotorTier get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return MotorTier.of(stack);
    }

    @Override
    public Codec<MotorTier> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public SelectItemModelProperty.Type<TierProperty, MotorTier> type() {
        return TYPE;
    }
}
