package gragongit.arcaneartistry.common.registry;

import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.guidebook.BookCategory;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import gragongit.arcaneartistry.common.guidebook.page.BookPageType;
import gragongit.arcaneartistry.common.spell.Spell;
import gragongit.arcaneartistry.common.spell.SpellEffectType;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public final class ModRegistries {
  private ModRegistries() {}

  public static final ResourceKey<Registry<StaffType>> STAFF_TYPE_KEY = ResourceKey.createRegistryKey(ArcaneArtistry.id("staff_type"));

  public static final ResourceKey<Registry<SpellEffectType<?>>> SPELL_EFFECT_TYPE_KEY =
      ResourceKey.createRegistryKey(ArcaneArtistry.id("spell_effect_type"));

  public static final Registry<SpellEffectType<?>> SPELL_EFFECT_TYPES =
      FabricRegistryBuilder.create(SPELL_EFFECT_TYPE_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

  public static final ResourceKey<Registry<Spell>> SPELL_KEY = ResourceKey.createRegistryKey(ArcaneArtistry.id("spell"));

  public static final ResourceKey<Registry<BookPageType<?>>> BOOK_PAGE_TYPE_KEY =
      ResourceKey.createRegistryKey(ArcaneArtistry.id("book_page_type"));

  public static final Registry<BookPageType<?>> BOOK_PAGE_TYPES =
      FabricRegistryBuilder.create(BOOK_PAGE_TYPE_KEY).attribute(RegistryAttribute.SYNCED).buildAndRegister();

  public static final ResourceKey<Registry<BookCategory>> BOOK_CATEGORY_KEY = ResourceKey.createRegistryKey(ArcaneArtistry.id("book_category"));

  public static final ResourceKey<Registry<BookEntry>> BOOK_ENTRY_KEY = ResourceKey.createRegistryKey(ArcaneArtistry.id("book_entry"));

  public static void register() {
    DynamicRegistries.registerSynced(STAFF_TYPE_KEY, StaffType.CODEC);
    DynamicRegistries.registerSynced(SPELL_KEY, Spell.CODEC);
    DynamicRegistries.registerSynced(BOOK_CATEGORY_KEY, BookCategory.CODEC);
    DynamicRegistries.registerSynced(BOOK_ENTRY_KEY, BookEntry.CODEC);
  }
}
