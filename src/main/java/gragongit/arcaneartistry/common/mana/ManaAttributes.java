package gragongit.arcaneartistry.common.mana;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class ManaAttributes {
  public static final double BASE_MAX_MANA = 100.0;
  public static final double MIN_MAX_MANA = 0.0;
  public static final double MAX_MAX_MANA = 10000.0;

  public static final Holder<Attribute> MAX_MANA = Registry
      .registerForHolder(BuiltInRegistries.ATTRIBUTE, ArcaneArtistry.id("max_mana"),
          new RangedAttribute("attribute.name.arcane-artistry.max_mana", BASE_MAX_MANA, MIN_MAX_MANA, MAX_MAX_MANA).setSyncable(true));

  public static void register() {}
}
