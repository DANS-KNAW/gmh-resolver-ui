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
package nl.knaw.dans.kb.resolver.jdbc;

import nl.knaw.dans.kb.resolver.Location;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PooledDataSource {

  private static final BasicDataSource ds = new BasicDataSource();
  private static final Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

  static {
    ResourceBundle properties = ResourceBundle.getBundle("application");

    ds.setUrl(properties.getString("MYSQL_DB_URL"));
    ds.setUsername(properties.getString("MYSQL_DB_USERNAME"));
    ds.setPassword(properties.getString("MYSQL_DB_PASSWORD"));

    ds.setMinIdle(2);
    ds.setMaxIdle(10);
    ds.setMaxActive(15);
  }

  public static Connection getConnection() throws SQLException {
    printDbStatus();
    return ds.getConnection();
  }

  private PooledDataSource() {
  }

  // This method is used to print the Connection Pool status:
  private static void printDbStatus() {
    logger.debug("Max.: " + ds.getMaxActive() + "; Active: " + ds.getNumActive() + "; Idle: " + ds.getNumIdle());
  }

  public static List<Location> getLocations(String identifier) {
    List<Location> locations = new ArrayList<Location>();

    //    Get rid of the fragment part:
    String unfragmented = identifier;
    if (identifier != null && identifier.contains("#")) {
      unfragmented = identifier.split("#")[0];
    }

    logger.info("Getting location(s) for: " + unfragmented);

    ResultSet rs = null;
    Connection conn = null;
    PreparedStatement pstmt = null;

    try {
      conn = PooledDataSource.getConnection();
      pstmt = conn.prepareStatement("SELECT L.location_url, IL.isFailover FROM identifier I JOIN identifier_location IL ON I.identifier_id = IL.identifier_id JOIN location L ON L.location_id = IL.location_id WHERE I.identifier_value=? ORDER BY IL.isFailover, IL.last_modified DESC");
      pstmt.setString(1, unfragmented);
      rs = pstmt.executeQuery();

      int i = 0;
      while (rs.next()) {
        locations.add(new Location(i++, rs.getString(1)));
      }
    }
    catch (SQLException e) {
      logger.error("Getting locations for NBN went wrong... (SQLException)");
      FacesContext facesContext = FacesContext.getCurrentInstance();
      try {
        facesContext.getExternalContext().responseSendError(503, "Lost DB connection");
      }
      catch (IOException io) {
        //ignored
      }
      facesContext.responseComplete();
    }
    finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (pstmt != null) {
          pstmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      }
      catch (Exception ex) {
        //ignored
      }
    }
    return locations;
  }

  public static void testDBConnection() {
    logger.debug("Testing DB connection");
    ResultSet rs = null;
    Connection conn = null;
    Statement stmt = null;

    try {
      conn = PooledDataSource.getConnection();
      stmt = conn.createStatement();
      rs = stmt.executeQuery("SELECT 'test'");
    }
    catch (SQLException e) {
      logger.error("Testing DB connection went wrong...");
      FacesContext facesContext = FacesContext.getCurrentInstance();
      try {
        facesContext.getExternalContext().responseSendError(503, "Lost DB connection");
      }
      catch (IOException io) {
        //ignored
      }
      facesContext.responseComplete();
    }
    finally {
      try {
        if (rs != null) {
          rs.close();
        }
        if (stmt != null) {
          stmt.close();
        }
        if (conn != null) {
          conn.close();
        }
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
    }
  }
}


