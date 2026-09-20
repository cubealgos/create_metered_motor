package metered_motor.mixin.client;

import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Reaches {@link SelectItemModelProperties}'s private static {@code ID_MAPPER} (an {@code
 * ExtraCodecs.LateBoundIdMapper}, verified public and mutable through its own {@code put} in the
 * merged jar) so {@code metered_motor.client.visual.TierProperty} can register itself under
 * {@code metered_motor:tier} without vanilla's own {@code SelectItemModelProperties.bootstrap()}
 * knowing about it (MOTOR-REQ-013, MM-15 Findings). Unlike {@code DataComponentType} or
 * {@code BlockEntityType}, this id mapper is not a {@code Registry} an add-on can call {@code
 * Registry.register} on, and it has no public setter of its own — a static accessor is the
 * smallest reach that avoids the ticket's {@code custom_model_data} fallback.
 */
@Mixin(SelectItemModelProperties.class)
public interface SelectItemModelPropertiesAccessor {
    @Accessor("ID_MAPPER")
    static ExtraCodecs.LateBoundIdMapper<Identifier, SelectItemModelProperty.Type<?, ?>> idMapper() {
        throw new AssertionError();
    }
}
