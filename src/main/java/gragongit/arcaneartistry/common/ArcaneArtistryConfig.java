package gragongit.arcaneartistry.common;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import gragongit.arcaneartistry.common.api.CastPattern;
import net.fabricmc.loader.api.FabricLoader;

public final class ArcaneArtistryConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String FILE_NAME = ArcaneArtistry.MOD_ID + ".json";

  private static final String MAX_PATTERN_LENGTH_KEY = "maxPatternLength";
  private static final int MIN_MAX_PATTERN_LENGTH = 1;
  private static final int DEFAULT_MAX_PATTERN_LENGTH = 8;
  private static final Codec<Integer> MAX_PATTERN_LENGTH_CODEC = Codec.intRange(MIN_MAX_PATTERN_LENGTH, CastPattern.MAX_LENGTH);

  private static int maxPatternLength = DEFAULT_MAX_PATTERN_LENGTH;

  public static int maxPatternLength() {
    return maxPatternLength;
  }

  public static void load() {
    Path path = path();
    if (Files.exists(path)) {
      try (Reader reader = Files.newBufferedReader(path)) {
        JsonObject json = GSON.fromJson(reader, JsonObject.class);
        if (json != null) {
          maxPatternLength = loadInt(json.get(MAX_PATTERN_LENGTH_KEY), MAX_PATTERN_LENGTH_KEY, MAX_PATTERN_LENGTH_CODEC, maxPatternLength);
        }
      } catch (IOException | JsonParseException e) {
        ArcaneArtistry.LOGGER.warn("Failed to read {}, using defaults", FILE_NAME, e);
      }
    }
    save();
  }

  private static int loadInt(JsonElement element, String key, Codec<Integer> codec, int fallback) {
    if (element == null) {
      return fallback;
    }
    return codec
        .parse(JsonOps.INSTANCE, element)
        .resultOrPartial(error -> ArcaneArtistry.LOGGER.warn("Invalid value for {} in {}: {}", key, FILE_NAME, error))
        .orElse(fallback);
  }

  private static void save() {
    JsonObject json = new JsonObject();
    json.add(MAX_PATTERN_LENGTH_KEY, new JsonPrimitive(maxPatternLength));
    try (Writer writer = Files.newBufferedWriter(path())) {
      GSON.toJson(json, writer);
    } catch (IOException e) {
      ArcaneArtistry.LOGGER.warn("Failed to write {}", FILE_NAME, e);
    }
  }

  private static Path path() {
    return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
  }
}
