package com.spring.hapi.fhirclient.search;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

@Component
public class SearchPager {
  /**
   * for each set of pages, go after the next ..
   * @param firstPage
   * @param client
   * @param pageConsumer
   */
  public void forEach(Bundle firstPage, IGenericClient client, Consumer<Bundle> pageConsumer) {
    Bundle page = firstPage;
    while (page != null) {
      pageConsumer.accept(page);
      Bundle.BundleLinkComponent next = page.getLink("next");
      if (next == null || next.getUrl() == null) break;
      page = client.loadPage().byUrl(next.getUrl()).andReturnBundle(Bundle.class).execute();
    }
  }

  /**
   * Lets iterate over the bundles
   *
   * @param firstPage
   * @param client
   * @return
   */
  public Iterable<Bundle> iterable(Bundle firstPage, IGenericClient client) {
    return () -> new Iterator<Bundle>() {
      Bundle current = firstPage;
      boolean first = true;
      @Override public boolean hasNext() {
        if (first) return true;
        Bundle.BundleLinkComponent next = current.getLink("next");
        return next != null && next.getUrl() != null;
      }
      @Override public Bundle next() {
        if (first) {
          first = false;
          return current;
        }
        Bundle.BundleLinkComponent next = current.getLink("next");
        if (next == null || next.getUrl() == null) throw new NoSuchElementException();
        current = client.loadPage().byUrl(next.getUrl()).andReturnBundle(Bundle.class).execute();
        return current;
      }
    };
  }
}
