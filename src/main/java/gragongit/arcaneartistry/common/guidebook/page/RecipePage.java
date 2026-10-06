package gragongit.arcaneartistry.common.guidebook.page;

import java.util.Optional;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

public record RecipePage(ResourceKey<Recipe<?>> recipe, Optional<Component> title, Optional<Component> text) implements BookPage {
  public static final MapCodec<RecipePage> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(ResourceKey.codec(Registries.RECIPE).fieldOf("recipe").forGetter(RecipePage::recipe),
              ComponentSerialization.CODEC.optionalFieldOf("title").forGetter(RecipePage::title),
              ComponentSerialization.CODEC.optionalFieldOf("text").forGetter(RecipePage::text))
          .apply(instance, RecipePage::new));

  @Override
  public BookPageType<?> type() {
    return BookPageTypes.RECIPE;
  }
}
