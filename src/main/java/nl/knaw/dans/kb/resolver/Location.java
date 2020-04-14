package nl.knaw.dans.kb.resolver;

public class Location {

  private final int prio;
  private final String url;

  public Location(int prio, String url) {
    this.prio = prio;
    this.url = url;
  }

  public int getPrio() {
    return prio;
  }

  public String getUrl() {
    return url;
  }
}


