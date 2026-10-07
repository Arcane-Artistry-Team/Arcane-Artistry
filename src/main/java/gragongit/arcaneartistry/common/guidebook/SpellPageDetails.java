package gragongit.arcaneartistry.common.guidebook;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import gragongit.arcaneartistry.common.spell.Spell;
import net.minecraft.network.chat.Component;

public final class SpellPageDetails {
  public static final String PATTERN = "guide_book.arcane-artistry.spell.pattern";
  public static final String MANA_COST = "guide_book.arcane-artistry.spell.mana_cost";

  @FunctionalInterface
  public interface Detail {
    Optional<Component> describe(Spell spell);
  }

  private static final List<Detail> DETAILS = new CopyOnWriteArrayList<>();

  static {
    register(spell -> Optional.of(Component.translatable(PATTERN, spell.pattern().toArrows())));
    register(spell -> Optional.of(Component.translatable(MANA_COST, spell.manaCost())));
  }

  private SpellPageDetails() {}

  public static void register(Detail detail) {
    DETAILS.add(detail);
  }

  static List<Component> describe(Spell spell) {
    return DETAILS.stream().flatMap(detail -> detail.describe(spell).stream()).toList();
  }
}
