package gragongit.arcaneartistry.client.mana;

import java.util.Locale;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;

public enum ManaBarPosition implements StringRepresentable {
  CROSSHAIR("options.arcane-artistry.mana_bar.crosshair"), BOTTOM_RIGHT("options.arcane-artistry.mana_bar.bottom_right");

  public static final Codec<ManaBarPosition> CODEC = StringRepresentable.fromEnum(ManaBarPosition::values);

  private final Component caption;

  ManaBarPosition(String key) {
    this.caption = Component.translatable(key);
  }

  public Component caption() {
    return caption;
  }

  @Override
  public String getSerializedName() {
    return name().toLowerCase(Locale.ROOT);
  }
}
