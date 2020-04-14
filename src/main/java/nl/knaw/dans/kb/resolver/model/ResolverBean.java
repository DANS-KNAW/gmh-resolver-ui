package nl.knaw.dans.kb.resolver.model;

import nl.knaw.dans.kb.resolver.Location;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;

@ManagedBean(eager = true)
@RequestScoped
public class ResolverBean {

  private String identifier = "urn:NBN:nl:ui:24-uuid:fcf57b9b-1a1d-410b-9e85-08c8fc72d93b";
  private Boolean resolveDisabled;
  private String realUrl;
  private int statusCode;
  private List<Location> locationList;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public Boolean getResolveDisabled() {
    return resolveDisabled;
  }

  public void setResolveDisabled(Boolean resolveDisabled) {
    this.resolveDisabled = resolveDisabled;
  }

  public String getRealUrl() {
    return realUrl;
  }

  public void setRealUrl(String realUrl) {
    this.realUrl = realUrl;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }

  public List<Location> getLocationList() {
    return locationList;
  }

  public void setLocationList(List<Location> locationList) {
    this.locationList = locationList;
  }

  public String do_resolve() {
    if (resolveDisabled) {
      locationList = new ArrayList<Location>();
      locationList.add(new Location(1,"http://www.dds.nl"));
      locationList.add(new Location(2,"https://www.narcis.nl"));
      locationList.add(new Location(3,"https://www.persistent-identifier.nl/"));
      this.setLocationList(locationList);
    }
    else {
      URL realUrl = null;
      try {
        realUrl = new URL("http://www.dds.nl");
        this.redirect(realUrl);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    return "index";
  }

  private void redirect(URL realUrl) throws IOException {
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    externalContext.redirect(realUrl.toString());
  }

}
//https://www.persistent-identifier.nl/?d-6417901-s=0&submittedBy=frm&identifier=urn%3ANBN%3Anl%3Aui%3A24-uuid%3Afcf57b9b-1a1d-410b-9e85-08c8fc72d93b&d-6417901-o=2&redirectDisabled=on