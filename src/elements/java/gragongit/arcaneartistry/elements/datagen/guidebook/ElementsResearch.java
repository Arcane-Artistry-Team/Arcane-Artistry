package gragongit.arcaneartistry.elements.datagen.guidebook;

import com.klikli_dev.modonomicon.api.datagen.research.ResearchNodeRef;
import com.klikli_dev.modonomicon.api.datagen.research.SingleResearchSubProvider;
import gragongit.arcaneartistry.common.ArcaneArtistry;
import gragongit.arcaneartistry.datagen.guidebook.basics.BasicsCategory;
import gragongit.arcaneartistry.datagen.guidebook.basics.StaffsEntry;
import gragongit.arcaneartistry.elements.common.ArcaneArtistryElements;

public class ElementsResearch extends SingleResearchSubProvider {
  public static final ResearchNodeRef STAFFS_READ = ref("guide_book/staffs_read");

  public ElementsResearch() {
    super("guide_book", ArcaneArtistryElements.MOD_ID);
  }

  @Override
  protected void generateResearch() {
    node(STAFFS_READ,
        ingress().onEntryViewedOnce(ArcaneArtistry.id(BasicsCategory.ID + "/" + StaffsEntry.ID)).declareFact("guide_book/staffs_read"));
    researchNodeName(STAFFS_READ, "Staffs");
  }

  private static ResearchNodeRef ref(String path) {
    return ResearchNodeRef.of(ArcaneArtistryElements.id(path));
  }
}
