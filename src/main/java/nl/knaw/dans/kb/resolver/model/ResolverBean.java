package nl.knaw.dans.kb.resolver.model;

import nl.knaw.dans.kb.resolver.Location;
import nl.knaw.dans.kb.resolver.UrlResolver;
import nl.knaw.dans.kb.resolver.jdbc.PooledDataSource;
import nl.knaw.dans.kb.resolver.validator.NbnValidator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ManagedBean(eager = true)
@RequestScoped
public class ResolverBean {

  private String identifier;
  private Boolean resolveDisabled = Boolean.FALSE;
  private int statusCode;
  private List<Location> locationList;
  private String redirectDisabled;

  private final String URN_PREFIX = "urn:nbn:";
  private Pattern pattern = Pattern.compile(NbnValidator.NBN_PATTERN);

  private Matcher matcher;

  private static final Logger logger = LoggerFactory.getLogger(ResolverBean.class);

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    if (identifier != null && !identifier.toLowerCase().startsWith("index.xhtml")) {
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

  public void setRedirectDisabled(String redirectDisabled) {
    this.redirectDisabled = redirectDisabled;
    if (redirectDisabled != null && redirectDisabled.equalsIgnoreCase("on")) {
      this.resolveDisabled = true;
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

  public void onload() {
    if (identifier != null) {
      this.do_resolve();
    }
    else {
      PooledDataSource.testDBConnection();
    }
  }

  public String do_resolve() {
    FacesContext context = FacesContext.getCurrentInstance();
    // Check if identifier isValid:, if not return to index and set error msge...
    matcher = pattern.matcher(this.getIdentifier());
    if (!matcher.matches()) {
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Given nbn-identifier is not valid.", this.getIdentifier()));
      logger.info("Given nbn-identifier is not valid: " + this.getIdentifier());
      return "index";
    }

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
        logger.info("Redirection failed: Unresolvable location(s): " + lokaties.toString());
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Redirection failed: Unresolvable location(s):", lokaties.toString()));
      }
    }
    else if (resolveDisabled == Boolean.FALSE && locations.isEmpty()) {
      context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Redirection failed: No location(s) available for this identifier:", this.getIdentifier()));
      logger.info("Redirection failed: No location(s) available for this identifier: " + this.getIdentifier());
    }
    return "index";
    //    return "pretty:resolvepathurn";
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
