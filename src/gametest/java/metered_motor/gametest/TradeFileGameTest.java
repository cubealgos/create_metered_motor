package metered_motor.gametest;

import java.util.Map;
import metered_motor.MeteredMotor;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.world.item.trading.VillagerTrade;

/**
 * MM-5: the three toolsmith trade files resolve through {@code Registries.VILLAGER_TRADE} and each
 * is tagged into its toolsmith level (TRADE-REQ-001, TRADE-REQ-003).
 */
public final class TradeFileGameTest {
    private static final Map<String, TagKey<VillagerTrade>> TRADES = Map.of(
        "toolsmith/3/emerald_metered_motor_i", VillagerTradeTags.TOOLSMITH_LEVEL_3,
        "toolsmith/4/emerald_metered_motor_ii", VillagerTradeTags.TOOLSMITH_LEVEL_4,
        "toolsmith/5/emerald_metered_motor_iii", VillagerTradeTags.TOOLSMITH_LEVEL_5
    );

    @GameTest
    public void theThreeTradesResolveAndAreTaggedIntoTheirLevel(GameTestHelper helper) {
        Registry<VillagerTrade> registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE);
        TRADES.forEach((path, tag) -> {
            Holder.Reference<VillagerTrade> holder = registry.get(MeteredMotor.id(path)).orElse(null);
            helper.assertTrue(holder != null, path + " resolves in the villager_trade registry");
            if (holder != null) {
                helper.assertTrue(holder.is(tag), path + " is tagged into " + tag.location());
            }
        });
        helper.succeed();
    }
}
