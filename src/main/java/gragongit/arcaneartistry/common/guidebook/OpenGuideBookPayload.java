package gragongit.arcaneartistry.common.guidebook;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

/**
 * Opens the guide book. Carries what the client can't know on its own: which entries have their advancement requirement met, and the
 * displays of every recipe shown in the book (clients don't receive recipes).
 */
public record OpenGuideBookPayload(Set<ResourceKey<BookEntry>> unlocked, Map<ResourceKey<Recipe<?>>, List<RecipeDisplay>> recipes)
    implements CustomPacketPayload {
  public static final CustomPacketPayload.Type<OpenGuideBookPayload> TYPE =
      new CustomPacketPayload.Type<>(ArcaneArtistry.id("open_guide_book"));

  public static final StreamCodec<RegistryFriendlyByteBuf, OpenGuideBookPayload> CODEC = StreamCodec
      .composite(GuideBookAttachments.ENTRY_SET_STREAM_CODEC, OpenGuideBookPayload::unlocked,
          ByteBufCodecs
              .map(HashMap::new, ResourceKey.streamCodec(Registries.RECIPE), RecipeDisplay.STREAM_CODEC.apply(ByteBufCodecs.list())),
          OpenGuideBookPayload::recipes, OpenGuideBookPayload::new);

  @Override
  public Type<? extends CustomPacketPayload> type() {
    return TYPE;
  }
}
