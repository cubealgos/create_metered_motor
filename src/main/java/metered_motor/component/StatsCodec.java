package metered_motor.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * The {@code metered_motor:stats} component's codecs (docs/spec/contracts/data-contract.md):
 * version 2 stores only {@code version} and {@code tier}. A saved version 1 payload
 * ({@code {version:1, tier, rpm, capacity, efficiency}}) still decodes — this codec only ever
 * reads {@code version} and {@code tier}, so the withdrawn {@code rpm}, {@code capacity} and
 * {@code efficiency} fields are simply never looked at — and {@link #normalise} migrates any
 * readable version (at or below {@link Stats#VERSION}) to {@link Stats#VERSION} on the spot
 * (DATA-REQ-002): nothing older than the current version is ever kept around in memory once its
 * tier is read. A version newer than this build's is kept exactly as saved, at its own version
 * number, so {@link Stats#readOnly()} reports it intact and unread (DATA-REQ-001, MOTOR-REQ-014).
 */
public final class StatsCodec {
    private static final Codec<Tier> TIER = Codec.INT.xmap(Tier::ofNumber, Tier::number);

    /** Persistent (save) codec: {@code version} defaults to {@link Stats#VERSION} so an unversioned save still reads (DATA-REQ-001). */
    public static final Codec<Stats> CODEC = RecordCodecBuilder.create(b -> b.group(
        Codec.INT.optionalFieldOf("version", Stats.VERSION).forGetter(Stats::version),
        TIER.fieldOf("tier").forGetter(Stats::tier)
    ).apply(b, StatsCodec::normalise));

    public static final StreamCodec<RegistryFriendlyByteBuf, Stats> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    /** A readable version is migrated to {@link Stats#VERSION} in memory (DATA-REQ-002); a version
     *  this build cannot read is kept at its own number so it stays read-only (MOTOR-REQ-014). */
    private static Stats normalise(int version, Tier tier) {
        return new Stats(version <= Stats.VERSION ? Stats.VERSION : version, tier);
    }

    private StatsCodec() {
    }
}
