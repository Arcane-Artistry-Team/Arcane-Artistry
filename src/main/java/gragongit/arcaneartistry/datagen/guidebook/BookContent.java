package gragongit.arcaneartistry.datagen.guidebook;

/**
 * Declares guide book categories and entries together with their English text. {@link #define} runs once per output (the category
 * registry, the entry registry and the language file), so implementations must not have side effects.
 */
public interface BookContent {
  /** The namespace of the generated category/entry ids and translation keys. */
  String namespace();

  void define(BookContext context);
}
