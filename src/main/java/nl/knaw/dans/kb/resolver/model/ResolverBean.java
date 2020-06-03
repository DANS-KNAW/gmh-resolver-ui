package nl.knaw.dans.kb.resolver.model;

import nl.knaw.dans.kb.resolver.Location;
import nl.knaw.dans.kb.resolver.UrlResolver;
import nl.knaw.dans.kb.resolver.jdbc.PooledDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@ManagedBean(eager = true)
@RequestScoped
public class ResolverBean {

  private String identifier;
  private Boolean resolveDisabled = Boolean.FALSE;
  private int statusCode;
  private List<Location> locationList;
  private String redirectDisabled;
  private String pathurn;
  private final String URN_PREFIX = "urn:nbn:";

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {

    if (identifier != null && identifier.toLowerCase().startsWith(URN_PREFIX)) {
      this.identifier = identifier;
    }
  }

  public Boolean getResolveDisabled() {
    return resolveDisabled;
  }

  public void setResolveDisabled(Boolean resolveDisabled) {
    this.resolveDisabled = resolveDisabled;
  }

  public String getRedirectDisabled() {
    return redirectDisabled;
  }

  private static final Logger logger = LoggerFactory.getLogger(ResolverBean.class);

  public void setRedirectDisabled(String redirectDisabled) {
    this.redirectDisabled = redirectDisabled;
    if (redirectDisabled != null && redirectDisabled.equalsIgnoreCase("on")) {
      this.resolveDisabled = true;
    }
  }

  public String getPathurn() {
    return pathurn;
  }

  public void setPathurn(String pathurn) {
    this.pathurn = pathurn;
    if (pathurn != null && pathurn.toLowerCase().startsWith(URN_PREFIX)){
      this.identifier = pathurn;
//      this.resolveDisabled = Boolean.FALSE;
//      this.do_resolve();
    }
  }


  public void onload() {
    if (identifier != null && identifier.toLowerCase().startsWith(URN_PREFIX)) {
      this.do_resolve();
    }
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
    //Get prio-locations list from db for this nbn:
    List<Location> locations = PooledDataSource.getLocations(this.getIdentifier());
    this.setLocationList(locations);

    if (resolveDisabled == Boolean.FALSE && !locations.isEmpty()) {
      //Try to resolve the real-url, starting with the high-prio (first) one.
      String resolvableLocation = null;
      for (Location loc : locations) {
        int statuscode = UrlResolver.getResponseCode(loc.getUrl(), true);
        if (statuscode == 200) { //Redir to location-url (not the resolved URL)
          resolvableLocation = loc.getUrl();
          break; //Do redirection outside loop, IllegalstateException otherwise.
        }
      }
      if (resolvableLocation != null) {
        this.redirect(resolvableLocation);
      }
      else { // Still here??  None of at least one Location resolved to a realUrl...
        ArrayList<String> lokaties = new ArrayList<>();
        for (Location loc : locations) {
          lokaties.add(loc.getUrl());
        }
        logger.info("Redirection failed: Unresolvalbe location(s): " + lokaties.toString());
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Redirection failed: Unresolvalbe location(s):", lokaties.toString()));
      }
    }
    else if (resolveDisabled == Boolean.FALSE && locations.isEmpty()) {
      //      System.out.println("Redirection failed: No location(s) found for this identifier.");
      //      this.setShowGUILocations(Boolean.FALSE);
      FacesContext context = FacesContext.getCurrentInstance();
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Redirection failed: No location(s) available for this identifier:", this.getIdentifier()));
      logger.info("Redirection failed: No location(s) available for this identifier: " + this.getIdentifier());
    }
    return "index";
  }

  private void redirect(String location) {
    logger.info("Redirecting client to found location: " + location);
    ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
    try {
      externalContext.redirect(location);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}

//https://www.persistent-identifier.nl/?d-6417901-s=0&submittedBy=frm&identifier=urn%3ANBN%3Anl%3Aui%3A24-uuid%3Afcf57b9b-1a1d-410b-9e85-08c8fc72d93b&d-6417901-o=2&redirectDisabled=on
