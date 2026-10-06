package gragongit.arcaneartistry.common.guidebook;

import org.jspecify.annotations.Nullable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.crystalball.CrystalBallTheme;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public record BookCategory(Holder<Item> icon, Component title, int sort, BookBackground background, int connectionColor) {

  public static Builder builder() {
    return new Builder();
  }

  public static final Codec<BookCategory> CODEC = RecordCodecBuilder
      .create(instance -> instance
          .group(Item.CODEC.fieldOf("icon").forGetter(BookCategory::icon),
              ComponentSerialization.CODEC.fieldOf("title").forGetter(BookCategory::title),
              Codec.INT.optionalFieldOf("sort", 0).forGetter(BookCategory::sort),
              BookBackground.CODEC.optionalFieldOf("background", BookBackground.DEFAULT).forGetter(BookCategory::background),
              ExtraCodecs.STRING_RGB_COLOR
                  .optionalFieldOf("connection_color", CrystalBallTheme.DEFAULT_CONNECTION_COLOR)
                  .forGetter(BookCategory::connectionColor))
          .apply(instance, BookCategory::new));

  public static final class Builder {
    private @Nullable Holder<Item> icon;
    private @Nullable Component title;
    private int sort;
    private BookBackground background = BookBackground.DEFAULT;
    private int connectionColor = CrystalBallTheme.DEFAULT_CONNECTION_COLOR;

    private Builder() {}

    public Builder icon(Item icon) {
      this.icon = BuiltInRegistries.ITEM.wrapAsHolder(icon);
      return this;
    }

    public Builder title(Component title) {
      this.title = title;
      return this;
    }

    public Builder sort(int sort) {
      this.sort = sort;
      return this;
    }

    public Builder textureBackground(Identifier texture) {
      this.background = new BookBackground.Texture(texture);
      return this;
    }

    public Builder shaderBackground(Identifier shader) {
      this.background = new BookBackground.Shader(shader);
      return this;
    }

    public Builder connectionColor(int connectionColor) {
      this.connectionColor = connectionColor;
      return this;
    }

    public BookCategory build() {
      return new BookCategory(required(icon, "icon"), required(title, "title"), sort, background, connectionColor);
    }

    private static <T> T required(@Nullable T value, String name) {
      if (value == null) {
        throw new IllegalStateException("Missing required field '" + name + "'");
      }
      return value;
    }
  }
}
