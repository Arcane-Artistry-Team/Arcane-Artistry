package gragongit.arcaneartistry.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import gragongit.arcaneartistry.client.mana.ManaBarPosition;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;

public final class ArcaneArtistryClientConfig {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final String FILE_NAME = ArcaneArtistry.MOD_ID + "-client.json";
  private static final String TOOLTIP_SUFFIX = ".tooltip";
  private static final String PERCENT_VALUE_TRANSLATION_KEY = "options.percent_value";
  private static final double PERCENT_SCALE = 100.0;

  public static final Component SECTION_HEADER =
      Component.translatable("options.arcane-artistry.header").withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);

  // Mana bar position
  private static final String MANA_BAR_POSITION_KEY = "manaBarPosition";
  private static final String MANA_BAR_POSITION_TRANSLATION_KEY = "options.arcane-artistry.mana_bar";
  private static final ManaBarPosition DEFAULT_MANA_BAR_POSITION = ManaBarPosition.BOTTOM_RIGHT;

  public static final OptionInstance<ManaBarPosition> MANA_BAR_POSITION =
      new OptionInstance<>(MANA_BAR_POSITION_TRANSLATION_KEY, OptionInstance.noTooltip(), (caption, value) -> value.caption(),
          new OptionInstance.Enum<>(Arrays.asList(ManaBarPosition.values()), ManaBarPosition.CODEC), DEFAULT_MANA_BAR_POSITION,
          value -> save());

  private static final String STROKE_THRESHOLD_KEY = "strokeThreshold";
  private static final String STROKE_THRESHOLD_TRANSLATION_KEY = "options.arcane-artistry.stroke_threshold";
  private static final int MIN_STROKE_THRESHOLD = 50;
  private static final int MAX_STROKE_THRESHOLD = 500;
  private static final int STROKE_THRESHOLD_STEP = 10;
  private static final int DEFAULT_STROKE_THRESHOLD = 200;

  public static final OptionInstance<Integer> STROKE_THRESHOLD = new OptionInstance<>(STROKE_THRESHOLD_TRANSLATION_KEY,
      OptionInstance.cachedConstantTooltip(Component.translatable(STROKE_THRESHOLD_TRANSLATION_KEY + TOOLTIP_SUFFIX)),
      Options::genericValueLabel,
      new OptionInstance.IntRange(MIN_STROKE_THRESHOLD / STROKE_THRESHOLD_STEP, MAX_STROKE_THRESHOLD / STROKE_THRESHOLD_STEP)
          .xmap(steps -> steps * STROKE_THRESHOLD_STEP, value -> value / STROKE_THRESHOLD_STEP, true),
      DEFAULT_STROKE_THRESHOLD, value -> save());

  private static final String MAX_STAFF_MOVEMENT_KEY = "maxStaffMovement";
  private static final String MAX_STAFF_MOVEMENT_TRANSLATION_KEY = "options.arcane-artistry.max_staff_movement";
  private static final double MAX_STAFF_MOVEMENT_STEP = 0.01;
  private static final int MAX_STAFF_MOVEMENT_MAX_STEPS = 50;
  private static final double DEFAULT_MAX_STAFF_MOVEMENT = 0.25;
  private static final double MAX_STAFF_MOVEMENT_PERCENT_REFERENCE = DEFAULT_MAX_STAFF_MOVEMENT;

  public static final OptionInstance<Double> MAX_STAFF_MOVEMENT = new OptionInstance<>(MAX_STAFF_MOVEMENT_TRANSLATION_KEY,
      OptionInstance.cachedConstantTooltip(Component.translatable(MAX_STAFF_MOVEMENT_TRANSLATION_KEY + TOOLTIP_SUFFIX)),
      (caption, value) -> percentValueLabel(caption, value / MAX_STAFF_MOVEMENT_PERCENT_REFERENCE),
      new OptionInstance.IntRange(0, MAX_STAFF_MOVEMENT_MAX_STEPS)
          .xmap(steps -> steps * MAX_STAFF_MOVEMENT_STEP, value -> (int) Math.round(value / MAX_STAFF_MOVEMENT_STEP), true),
      DEFAULT_MAX_STAFF_MOVEMENT, value -> save());

  public static final OptionInstance<?>[] VIDEO_OPTIONS = {MANA_BAR_POSITION, MAX_STAFF_MOVEMENT};
  public static final OptionInstance<?>[] MOUSE_OPTIONS = {STROKE_THRESHOLD};

  private static final Map<String, OptionInstance<?>> SAVED_OPTIONS = Map.of(
      MANA_BAR_POSITION_KEY, MANA_BAR_POSITION,
      STROKE_THRESHOLD_KEY, STROKE_THRESHOLD,
      MAX_STAFF_MOVEMENT_KEY, MAX_STAFF_MOVEMENT);

  private static boolean loading;

  public static void load() {
    Path path = path();
    if (!Files.exists(path)) {
      return;
    }
    loading = true;
    try (Reader reader = Files.newBufferedReader(path)) {
      JsonObject json = GSON.fromJson(reader, JsonObject.class);
      if (json != null) {
        SAVED_OPTIONS.forEach((key, option) -> loadOption(json.get(key), key, option));
      }
    } catch (IOException | JsonParseException e) {
      ArcaneArtistry.LOGGER.warn("Failed to read {}, using defaults", FILE_NAME, e);
    } finally {
      loading = false;
    }
  }

  private static <T> void loadOption(JsonElement element, String key, OptionInstance<T> option) {
    if (element == null) {
      return;
    }
    option.codec()
        .parse(JsonOps.INSTANCE, element)
        .resultOrPartial(error -> ArcaneArtistry.LOGGER.warn("Invalid value for {} in {}: {}", key, FILE_NAME, error))
        .ifPresent(option::set);
  }

  private static void save() {
    if (loading) {
      return;
    }
    JsonObject json = new JsonObject();
    SAVED_OPTIONS.forEach((key, option) -> json.add(key, encodeOption(option)));
    try (Writer writer = Files.newBufferedWriter(path())) {
      GSON.toJson(json, writer);
    } catch (IOException e) {
      ArcaneArtistry.LOGGER.warn("Failed to write {}", FILE_NAME, e);
    }
  }

  private static <T> JsonElement encodeOption(OptionInstance<T> option) {
    return option.codec().encodeStart(JsonOps.INSTANCE, option.get()).getOrThrow();
  }

  private static Component percentValueLabel(Component caption, double fraction) {
    return Component.translatable(PERCENT_VALUE_TRANSLATION_KEY, caption, (int) Math.round(fraction * PERCENT_SCALE));
  }

  private static Path path() {
    return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
  }
}
