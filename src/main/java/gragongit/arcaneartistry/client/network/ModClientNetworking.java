package gragongit.arcaneartistry.client.network;

import java.util.Set;
import gragongit.arcaneartistry.client.crystalball.CrystalBallNodeStates;
import gragongit.arcaneartistry.client.crystalball.CrystalBallScreen;
import gragongit.arcaneartistry.common.api.CastPattern;
import gragongit.arcaneartistry.common.crystalball.CrystalBallPayload;
import gragongit.arcaneartistry.common.crystalball.CrystalBallState;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;

public final class ModClientNetworking {
  private ModClientNetworking() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(CrystalBallPayload.TYPE, ModClientNetworking::openCrystalBall);
  }

  private static void openCrystalBall(CrystalBallPayload payload, ClientPlayNetworking.Context context) {
    LocalPlayer player = context.player();
    Registry<Spell> spells = player.registryAccess().lookupOrThrow(ModRegistries.SPELL_KEY);
    ResourceKey<StaffType> staffTypeKey = payload.staffType().unwrapKey().orElseThrow();
    Set<CastPattern> explored = CrystalBallState.of(player).getExplored(staffTypeKey);

    CrystalBallNodeStates states = CrystalBallNodeStates.forStaffType(spells, payload.staffType(), explored);
    context.client().gui.setScreen(new CrystalBallScreen(states, payload.maxPatternLength(), Vec3.atCenterOf(payload.origin())));
  }
}
