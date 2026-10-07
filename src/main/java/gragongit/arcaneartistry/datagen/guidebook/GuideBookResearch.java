package gragongit.arcaneartistry.datagen.guidebook;

import java.util.List;
import com.klikli_dev.modonomicon.api.datagen.research.ResearchFactRef;
import com.klikli_dev.modonomicon.api.datagen.research.ResearchNodeRef;
import com.klikli_dev.modonomicon.api.datagen.research.SingleResearchSubProvider;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.common.registry.ModRegistries;
import gragongit.arcaneartistry.common.staff.StaffItems;
import gragongit.arcaneartistry.common.staff.StaffType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

public class GuideBookResearch extends SingleResearchSubProvider {

  public GuideBookResearch() {
    super("guide_book", ArcaneArtistry.MOD_ID);
  }

  public static ResearchNodeRef staffObtained(ResourceKey<StaffType> staffType) {
    return ResearchNodeRef.of(ArcaneArtistry.id(staffObtainedPath(staffType)));
  }

  private static String staffObtainedPath(ResourceKey<StaffType> staffType) {
    Identifier id = staffType.identifier();
    return "guide_book/staff_obtained/" + id.getNamespace() + "/" + id.getPath();
  }

  @Override
  protected void generateResearch() {
    registries().lookupOrThrow(ModRegistries.STAFF_TYPE_KEY).listElements().forEach(staffType -> {
      List<Item> items = StaffItems.of(staffType.key());
      if (items.isEmpty()) {
        return;
      }
      String path = staffObtainedPath(staffType.key());
      ResearchFactRef fact = fact(path);
      for (Item item : items) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        ingress().onItemAcquired(new ItemStackTemplate(item)).grantFact(path + "/" + itemId.getNamespace() + "/" + itemId.getPath(), fact);
      }
      ResearchNodeRef node = node(staffObtained(staffType.key()), fact);
      researchNodeName(node, "Obtain a staff of " + staffType.key().identifier());
    });
  }
}
