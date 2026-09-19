package metered_motor.client.visual;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import java.util.List;
import java.util.Locale;
import metered_motor.block.MeteredMotorBlockEntity;
import metered_motor.client.StatsText;
import metered_motor.model.Stats;
import net.minecraft.network.chat.Component;

/**
 * The goggles readout, appended after Create's own kinetic lines (speed, stress capacity) that
 * {@link GeneratingKineticTooltipBehaviour} already draws for any generating kinetic source
 * (MOTOR-REQ-012, UI-REQ-005, UI-UC-002). {@link MotorVisuals} attaches this to the block entity
 * type through {@code com.zurrtum.create.api.behaviour.BlockEntityBehaviour.addClient}, the same
 * client-side registry Create Fly's own {@code AllBlockEntityBehaviours} populates for its kinetic
 * blocks (docs/spec/README.md Verification 10).
 */
public final class MotorTooltipBehaviour extends GeneratingKineticTooltipBehaviour<MeteredMotorBlockEntity> {
    public MotorTooltipBehaviour(MeteredMotorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // Create's own lines first (speed, stress capacity added by the superclass); this motor's
        // readout is always shown regardless of whether stress impact is enabled, so the boolean
        // super() returns is not the gate here.
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        MeteredMotorBlockEntity motor = blockEntity;
        Stats stats = motor.stats();
        tooltip.add(Component.translatable("goggles.metered_motor.tier", stats.tier().name()));
        tooltip.add(Component.translatable("goggles.metered_motor.rpm", stats.rpm()));
        tooltip.add(Component.translatable("goggles.metered_motor.capacity", stats.capacity()));
        tooltip.add(Component.translatable("goggles.metered_motor.efficiency", StatsText.decimal(stats.efficiency())));
        tooltip.add(Component.translatable("goggles.metered_motor.rate", StatsText.decimal(stats.ratePerMinute())));
        tooltip.add(Component.translatable("goggles.metered_motor.load", StatsText.percent(motor.load())));
        tooltip.add(Component.translatable("goggles.metered_motor.emeralds", motor.emeraldsInside()));

        double secondsRemaining = motor.secondsRemaining();
        Component remaining = secondsRemaining < 0
            ? Component.translatable("goggles.metered_motor.idle")
            : Component.translatable("goggles.metered_motor.remaining", StatsText.duration(secondsRemaining));
        tooltip.add(remaining);

        String stateKey = "goggles.metered_motor.state." + motor.state().name().toLowerCase(Locale.ROOT);
        tooltip.add(Component.translatable("goggles.metered_motor.state", Component.translatable(stateKey)));
        return true;
    }
}
