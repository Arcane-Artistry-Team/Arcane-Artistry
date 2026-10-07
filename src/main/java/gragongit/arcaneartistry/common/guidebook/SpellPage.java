package gragongit.arcaneartistry.common.guidebook;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import com.klikli_dev.modonomicon.book.BookTextHolder;
import com.klikli_dev.modonomicon.book.conditions.BookCondition;
import com.klikli_dev.modonomicon.book.conditions.BookNoneCondition;
import com.klikli_dev.modonomicon.book.entries.BookContentEntry;
import com.klikli_dev.modonomicon.book.page.BookPage;
import com.klikli_dev.modonomicon.book.page.BookTextPage;
import com.klikli_dev.modonomicon.client.gui.book.markdown.BookTextRenderer;
import com.klikli_dev.modonomicon.data.BookPageType;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.spell.Spell;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

public class SpellPage extends BookTextPage {
  public static final Identifier ID = ArcaneArtistry.id("spell");
  public static final String UNKNOWN = "guide_book.arcane-artistry.spell.unknown";

  public static final MapCodec<SpellPage> CODEC = RecordCodecBuilder
      .mapCodec(instance -> instance
          .group(ResourceKey.codec(ModRegistries.SPELL_KEY).fieldOf("spell").forGetter(SpellPage::spell),
              Codec.STRING.fieldOf("id").forGetter(BookPage::getId),
              BookCondition.CODEC.optionalFieldOf("condition", new BookNoneCondition()).forGetter(BookPage::getCondition),
              BookPage.ASSOCIATED_ITEMS_CODEC.optionalFieldOf("associated_items", List.of()).forGetter(BookPage::getAssociatedItems))
          .apply(instance, SpellPage::new));

  public static final StreamCodec<RegistryFriendlyByteBuf, SpellPage> STREAM_CODEC =
      StreamCodec.composite(ResourceKey.streamCodec(ModRegistries.SPELL_KEY), SpellPage::spell, ByteBufCodecs.STRING_UTF8, BookPage::getId,
          BookCondition.STREAM_CODEC, BookPage::getCondition, BookPage.ASSOCIATED_ITEMS_STREAM_CODEC, BookPage::getAssociatedItems,
          SpellPage::new);

  private final ResourceKey<Spell> spell;
  private @Nullable Spell value;

  public SpellPage(ResourceKey<Spell> spell, String id, BookCondition condition, List<Either<ItemStackTemplate, Ingredient>> associatedItems) {
    super(BookTextHolder.EMPTY, BookTextHolder.EMPTY, false, true, null, null, id, condition, associatedItems);
    this.spell = spell;
  }

  public ResourceKey<Spell> spell() {
    return spell;
  }

  public Optional<Identifier> icon() {
    return Optional.ofNullable(value).map(spell -> spell.presentation().icon());
  }

  @Override
  public BookPageType<?> type() {
    return GuideBookPages.SPELL;
  }

  @Override
  public void build(Level level, BookContentEntry parentEntry, int pageNum) {
    super.build(level, parentEntry, pageNum);
    value = GuideBookPages.lookup(level, ModRegistries.SPELL_KEY, spell).orElse(null);
    Component name = value != null ? value.presentation().title() : Component.literal(spell.identifier().toString());
    title = GuideBookPages.title(book, name);
  }

  @Override
  public void prerenderMarkdown(BookTextRenderer textRenderer) {
    text = GuideBookPages.markdown(textRenderer, value != null ? markdown(value) : Component.translatable(UNKNOWN).getString());
    super.prerenderMarkdown(textRenderer);
  }

  private static String markdown(Spell spell) {
    String details = SpellPageDetails.describe(spell).stream().map(Component::getString).collect(Collectors.joining("\\\n"));
    return GuideBookPages.paragraphs(details, spell.presentation().description().map(Component::getString).orElse(""));
  }
}
