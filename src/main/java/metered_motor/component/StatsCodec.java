package metered_motor.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** The {@code metered_motor:stats} component's codecs: the on-disk shape of docs/spec/contracts/data-contract.md, and the wire shape for sync. */
public final class StatsCodec {
    private static final Codec<Tier> TIER = Codec.INT.xmap(Tier::ofNumber, Tier::number);

    /** Persistent (save) codec: {@code version} defaults to 1 so an unversioned save still reads (DATA-REQ-001). */
    public static final Codec<Stats> CODEC = RecordCodecBuilder.create(b -> b.group(
        Codec.INT.optionalFieldOf("version", Stats.VERSION).forGetter(Stats::version),
        TIER.fieldOf("tier").forGetter(Stats::tier),
        Codec.INT.fieldOf("rpm").forGetter(Stats::rpm),
        Codec.INT.fieldOf("capacity").forGetter(Stats::capacity),
        Codec.DOUBLE.fieldOf("efficiency").forGetter(Stats::efficiency)
    ).apply(b, Stats::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Stats> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private StatsCodec() {
    }
}
