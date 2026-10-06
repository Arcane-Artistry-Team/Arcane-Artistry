package gragongit.arcaneartistry.client.guidebook.page;

import gragongit.arcaneartistry.client.guidebook.GuideBookView;
import gragongit.arcaneartistry.common.guidebook.page.BookPage;

@FunctionalInterface
public interface BookPageRenderer<P extends BookPage> {
  PageContent content(P page, GuideBookView view, int width);
}
