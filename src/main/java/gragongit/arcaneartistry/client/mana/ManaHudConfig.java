package gragongit.arcaneartistry.client.mana;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.OptionInstance;

public final class ManaHudConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String FILE_NAME = ArcaneArtistry.MOD_ID + "-client.json";
  private static final String POSITION_KEY = "manaBarPosition";
  private static final String POSITION_TRANSLATION_KEY = "options.arcane-artistry.mana_bar";
  private static final ManaBarPosition DEFAULT_POSITION = ManaBarPosition.BOTTOM_RIGHT;

  public static final OptionInstance<ManaBarPosition> POSITION =
      new OptionInstance<>(POSITION_TRANSLATION_KEY, OptionInstance.noTooltip(), (caption, value) -> value.caption(),
          new OptionInstance.Enum<>(Arrays.asList(ManaBarPosition.values()), ManaBarPosition.CODEC), DEFAULT_POSITION, value -> save());

  public static final OptionInstance<?>[] OPTIONS = {POSITION};

  public static void load() {
    Path path = path();
    if (!Files.exists(path)) {
      return;
    }
    try (Reader reader = Files.newBufferedReader(path)) {
      JsonObject json = GSON.fromJson(reader, JsonObject.class);
      JsonElement position = json == null ? null : json.get(POSITION_KEY);
      if (position != null) {
        ManaBarPosition.CODEC
            .parse(JsonOps.INSTANCE, position)
            .resultOrPartial(error -> ArcaneArtistry.LOGGER.warn("Invalid mana bar position in {}: {}", FILE_NAME, error))
            .ifPresent(POSITION::set);
      }
    } catch (IOException | JsonParseException e) {
      ArcaneArtistry.LOGGER.warn("Failed to read {}, using defaults", FILE_NAME, e);
    }
  }

  private static void save() {
    JsonObject json = new JsonObject();
    json.add(POSITION_KEY, ManaBarPosition.CODEC.encodeStart(JsonOps.INSTANCE, POSITION.get()).getOrThrow());
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
