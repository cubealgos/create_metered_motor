package metered_motor.gametest;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import metered_motor.component.StatsCodec;
import metered_motor.model.Stats;
import metered_motor.model.Tier;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * MM-21 (`docs/spec/contracts/data-contract.md`, `DATA-REQ-002`): a saved version-1 component
 * ({@code {version:1, tier, rpm, capacity, efficiency}}, the shape {@code metered_motor:roll}
 * used to write) migrates forward to version 2 the moment it is read, keeping the tier and
 * dropping the withdrawn {@code rpm}, {@code capacity} and {@code efficiency} fields it can no
 * longer carry; a version this build cannot read (newer than {@link Stats#VERSION}) is kept
 * exactly as saved, at its own version number, and stays read-only (`MOTOR-REQ-014`).
 */
public final class MigrationGameTest {
    @GameTest
    public void aVersionOneComponentMigratesToVersionTwoKeepingTheTier(GameTestHelper helper) {
        JsonObject json = new JsonObject();
        json.addProperty("version", 1);
        json.addProperty("tier", 3);
        // The withdrawn fields a version-1 payload used to carry: present in the save, never read
        // by this build's codec (StatsCodec only ever looks for "version" and "tier").
        json.addProperty("rpm", 200);
        json.addProperty("capacity", 18_432);
        json.addProperty("efficiency", 0.75);

        Stats migrated = StatsCodec.CODEC.parse(JsonOps.INSTANCE, json)
            .getOrThrow(message -> new AssertionError("decode: " + message));

        helper.assertTrue(migrated.version() == Stats.VERSION, "the migrated component is version 2: " + migrated.version());
        helper.assertTrue(migrated.tier() == Tier.III, "the tier is kept across the migration: " + migrated.tier());
        helper.assertTrue(!migrated.readOnly(), "a migrated (readable) component is not read-only");
        helper.assertTrue(migrated.equals(new Stats(Stats.VERSION, Tier.III)),
            "the migrated component behaves as a normal tier III motor, fixed 64 rpm and capacity, nothing rolled: " + migrated);
        helper.succeed();
    }

    @GameTest
    public void aVersionThreeComponentStaysReadOnlyAndUnknown(GameTestHelper helper) {
        JsonObject json = new JsonObject();
        json.addProperty("version", Stats.VERSION + 1);
        json.addProperty("tier", 1);

        Stats future = StatsCodec.CODEC.parse(JsonOps.INSTANCE, json)
            .getOrThrow(message -> new AssertionError("decode: " + message));

        helper.assertTrue(future.version() == Stats.VERSION + 1, "a version newer than this build's is kept intact: " + future.version());
        helper.assertTrue(future.readOnly(), "a version newer than this build's is read-only (MOTOR-REQ-014)");
        helper.succeed();
    }

    @GameTest
    public void anUnversionedComponentDefaultsToTheCurrentVersion(GameTestHelper helper) {
        // DATA-REQ-001: version defaults so an unversioned save (predating the component's own
        // versioning) still reads.
        JsonObject json = new JsonObject();
        json.addProperty("tier", 2);

        Stats read = StatsCodec.CODEC.parse(JsonOps.INSTANCE, json)
            .getOrThrow(message -> new AssertionError("decode: " + message));

        helper.assertTrue(read.version() == Stats.VERSION, "an unversioned component reads at the current version: " + read.version());
        helper.assertTrue(read.tier() == Tier.II, "the tier is kept: " + read.tier());
        helper.succeed();
    }
}
