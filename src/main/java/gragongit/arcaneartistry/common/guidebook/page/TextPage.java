package gragongit.arcaneartistry.common.guidebook.page;

import java.util.Optional;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record TextPage(Optional<Component> title, Component text) implements BookPage {
  public static final MapCodec<TextPage> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(ComponentSerialization.CODEC.optionalFieldOf("title").forGetter(TextPage::title),
              ComponentSerialization.CODEC.fieldOf("text").forGetter(TextPage::text))
          .apply(instance, TextPage::new));

  @Override
  public BookPageType<?> type() {
    return BookPageTypes.TEXT;
  }
}
