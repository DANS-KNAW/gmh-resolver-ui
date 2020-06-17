package nl.knaw.dans.kb.resolver.jdbc;

import nl.knaw.dans.kb.resolver.Location;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class PooledDataSource {

  private static BasicDataSource ds = new BasicDataSource();
  private static final Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

  static {
    ResourceBundle properties = ResourceBundle.getBundle("application");

    ds.setUrl(properties.getString("MYSQL_DB_URL"));
    ds.setUsername(properties.getString("MYSQL_DB_USERNAME"));
    ds.setPassword(properties.getString("MYSQL_DB_PASSWORD"));
//    ds.setPassword(System.getProperty("mysqldb.pwd"));

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
    logger.info("Max.: " + ds.getMaxActive() + "; Active: " + ds.getNumActive() + "; Idle: " + ds.getNumIdle());
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

      int i=0;
      while (rs.next()) {
        locations.add(new Location(i++, rs.getString(1)));
      }
    }
    catch (SQLException e) {
      e.printStackTrace();
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
        ex.printStackTrace();
      }
    }
    return locations;
  }
}


