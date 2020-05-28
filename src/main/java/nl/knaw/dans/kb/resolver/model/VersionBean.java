package nl.knaw.dans.kb.resolver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@ManagedBean
@ApplicationScoped
public class VersionBean {

  private String appVersion;
  private static final Logger logger = LoggerFactory.getLogger(VersionBean.class);

  public VersionBean() {
    try {
      ResourceBundle properties = ResourceBundle.getBundle("projectversion");
      this.appVersion = properties.getString("PROJECTVERSION");
      logger.info("AppVersion: " + getAppVersion());
    }
    catch (MissingResourceException ex) {
      this.appVersion = "n.a.";
      logger.warn("AppVersion cannot be resolved!");
    }
  }

  public String getAppVersion() {
    return appVersion;
  }

  public void setAppVersion(String appVersion) {
    this.appVersion = appVersion;
  }
}
