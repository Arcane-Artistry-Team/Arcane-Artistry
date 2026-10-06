package gragongit.arcaneartistry.datagen.guidebook;

import java.util.List;
import gragongit.arcaneartistry.common.guidebook.BookCategory;
import gragongit.arcaneartistry.common.guidebook.BookEntry;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;

public final class GuideBookBootstrap {
  private GuideBookBootstrap() {}

  public static void bootstrapCategories(BootstrapContext<BookCategory> context, List<BookContent> content) {
    define(content, new BookContext.Output() {
      @Override
      public void category(ResourceKey<BookCategory> key, BookCategory category) {
        context.register(key, category);
      }
    });
  }

  public static void bootstrapEntries(BootstrapContext<BookEntry> context, List<BookContent> content) {
    define(content, new BookContext.Output() {
      @Override
      public void entry(ResourceKey<BookEntry> key, BookEntry entry) {
        context.register(key, entry);
      }
    });
  }

  public static void addTranslations(TranslationBuilder translations, List<BookContent> content) {
    define(content, new BookContext.Output() {
      @Override
      public void translation(String key, String english) {
        translations.add(key, english);
      }
    });
  }

  private static void define(List<BookContent> content, BookContext.Output output) {
    content.forEach(book -> book.define(new BookContext(book.namespace(), output)));
  }
}
