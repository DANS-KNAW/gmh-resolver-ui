/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
