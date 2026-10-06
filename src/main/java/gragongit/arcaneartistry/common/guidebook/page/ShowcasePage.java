package gragongit.arcaneartistry.common.guidebook.page;

import java.util.Optional;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public record ShowcasePage(Either<Holder<Item>, Image> display, Optional<Component> title, Optional<Component> text)
    implements BookPage {

  public record Image(Identifier texture, int width, int height) {
    public static final Codec<Image> CODEC = RecordCodecBuilder
        .create(instance -> instance
            .group(Identifier.CODEC.fieldOf("texture").forGetter(Image::texture),
                ExtraCodecs.POSITIVE_INT.fieldOf("width").forGetter(Image::width),
                ExtraCodecs.POSITIVE_INT.fieldOf("height").forGetter(Image::height))
            .apply(instance, Image::new));
  }

  public static final MapCodec<ShowcasePage> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(Codec.mapEither(Item.CODEC.fieldOf("item"), Image.CODEC.fieldOf("image")).forGetter(ShowcasePage::display),
              ComponentSerialization.CODEC.optionalFieldOf("title").forGetter(ShowcasePage::title),
              ComponentSerialization.CODEC.optionalFieldOf("text").forGetter(ShowcasePage::text))
          .apply(instance, ShowcasePage::new));

  @Override
  public BookPageType<?> type() {
    return BookPageTypes.SHOWCASE;
  }
}
