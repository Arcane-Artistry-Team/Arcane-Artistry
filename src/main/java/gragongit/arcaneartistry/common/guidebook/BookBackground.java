package gragongit.arcaneartistry.common.guidebook;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.resources.Identifier;

public sealed interface BookBackground {
  Texture DEFAULT = new Texture(ArcaneArtistry.id("textures/gui/guide_book/parchment.png"));

  record Texture(Identifier texture) implements BookBackground {
    private static final Codec<Texture> CODEC = RecordCodecBuilder
        .create(instance -> instance.group(Identifier.CODEC.fieldOf("texture").forGetter(Texture::texture)).apply(instance, Texture::new));
  }

  record Shader(Identifier shader) implements BookBackground {
    private static final Codec<Shader> CODEC = RecordCodecBuilder
        .create(instance -> instance.group(Identifier.CODEC.fieldOf("shader").forGetter(Shader::shader)).apply(instance, Shader::new));
  }

  Codec<BookBackground> CODEC = Codec.xor(Texture.CODEC, Shader.CODEC).xmap(either -> either.map(texture -> texture, shader -> shader),
      background -> switch (background) {
        case Texture texture -> Either.left(texture);
        case Shader shader -> Either.right(shader);
      });
}
