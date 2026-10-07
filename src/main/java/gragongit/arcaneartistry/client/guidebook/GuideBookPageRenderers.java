package gragongit.arcaneartistry.client.guidebook;

import com.klikli_dev.modonomicon.client.render.page.BookSpotlightPageRenderer;
import com.klikli_dev.modonomicon.client.render.page.PageRendererRegistry;
import gragongit.arcaneartistry.common.guidebook.SpellPage;
import gragongit.arcaneartistry.common.guidebook.StaffTypePage;

public final class GuideBookPageRenderers {
  private GuideBookPageRenderers() {}

  public static void register() {
    PageRendererRegistry.registerPageRenderer(SpellPage.ID, page -> new SpellPageRenderer((SpellPage) page));
    PageRendererRegistry.registerPageRenderer(StaffTypePage.ID, page -> new BookSpotlightPageRenderer((StaffTypePage) page));
  }
}
