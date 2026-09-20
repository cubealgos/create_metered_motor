package metered_motor.gametest;

import java.util.Optional;
import metered_motor.MeteredMotor;
import metered_motor.trade.NoMotorOffered;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * MM-5: a villager that already offers a metered motor refuses a second, through
 * {@code metered_motor:no_motor_offered} reading the villager's live offers off
 * {@code LootContextParams.THIS_ENTITY} (TRADE-REQ-006, TRADE-DEC-004).
 */
public final class NoDuplicateOfferGameTest {
    @GameTest
    public void aVillagerAlreadyOfferingAMotorRefusesASecond(GameTestHelper helper) {
        Item motor = BuiltInRegistries.ITEM.getOptional(MeteredMotor.id("metered_motor")).orElse(null);
        helper.assertTrue(motor != null, "set up: metered_motor:metered_motor is registered");

        Villager villager = helper.spawn(EntityTypes.VILLAGER, new BlockPos(1, 2, 1));
        villager.setVillagerData(villager.getVillagerData()
            .withProfession(helper.getLevel().registryAccess(), VillagerProfession.TOOLSMITH)
            .withLevel(3));
        villager.getOffers().add(new MerchantOffer(new ItemCost(Items.EMERALD, 24), new ItemStack(motor), 2, 10, 0.2f));

        LootParams params = new LootParams.Builder(helper.getLevel())
            .withParameter(LootContextParams.ORIGIN, helper.absoluteVec(villager.position()))
            .withParameter(LootContextParams.THIS_ENTITY, villager)
            .withParameter(LootContextParams.ADDITIONAL_COST_COMPONENT_ALLOWED, Unit.INSTANCE)
            .create(LootContextParamSets.VILLAGER_TRADE);
        LootContext context = new LootContext.Builder(params).create(Optional.empty());

        NoMotorOffered condition = new NoMotorOffered();
        helper.assertTrue(!condition.test(context), "a villager already offering a motor refuses a second offer");
        helper.succeed();
    }
}
