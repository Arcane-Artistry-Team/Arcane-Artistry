package gragongit.arcaneartistry.common.crystalball;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class CrystalBallItem extends Item {
  public CrystalBallItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {
    if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
      ServerPlayNetworking.send(serverPlayer, new CrystalBallPayload());
    }
    return InteractionResult.SUCCESS;
  }
}
