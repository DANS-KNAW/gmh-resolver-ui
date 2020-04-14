package nl.knaw.dans.kb.resolver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlResolver {

  public static void main(String[] args) {

    String[] locations = { "http://dds.nl", "http://localhost:8081/redir", "http://disq.us/8o29ub", "http://bit.ly/1Wd7H3h", "http://httpstat.us/500", "http://httpstat.us/405", "https://www.persistent-identifier.nl/urn:nbn:nl:ui:12-d68990f1-78fb-4c7d-a407-45477ac821ad", "https://pure.buas.nl/en/publications/d8bf9616-8cd9-4b1f-ad3e-e3277b92adfa", "http://resolver.tudelft.nl/uuid%3Afcf57b9b-1a1d-410b-9e85-08c8fc72d93b?steiny=gek" };

    for (String location : locations) {
      int statuscode = UrlResolver.getResponseCode(location, true);
      System.out.println("RESOLVED code: " + statuscode);
    }
  }

  //  private static final Logger LOG = LoggerFactory.getLogger(ResolveIdentifierController.class);

  private static final int MAX_HOP_COUNT = 10;
  private static final String HEADER_FIELD_LOCATION = "Location";
  private static final String user_agent = "nbn-resolver/0.1.0 - harvester@dans.knaw.nl";
  public static final String METHOD_HEAD = "HEAD";
  public static final String METHOD_GET = "GET";

  public static int getResponseCode(String urlString, boolean followredirects) {
    return UrlResolver.getResponseCode(urlString, 0, followredirects, METHOD_HEAD);
  }

  /**
   * Gets the response code.
   *
   * @param urlString       the url string
   * @param cnt_redir       the redirected number counter
   * @param followredirects a boolean indicating whether or not to follow HTTP redirects.
   * @param method          the HTTP method
   * @return the HTTP response code or -1 if no connection can be established (URL does not exist) OR @param cnt_redir number exeeded.
   */
  public static int getResponseCode(String urlString, int cnt_redir, boolean followredirects, String method) {
    int response_code = -1;
    if (cnt_redir >= MAX_HOP_COUNT) {
      return response_code;
    }

    try {
      URL url = new URL(urlString);
      URLConnection connection = url.openConnection();
      if (connection instanceof HttpURLConnection) {
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        httpConnection.setRequestMethod(method);
        httpConnection.setInstanceFollowRedirects(false);  //it only applies to redirects to the same protocol. An HttpURLConnection won't automatically follow redirects to a different protocol, even if the redirect flag is set.
        httpConnection.addRequestProperty("User-Agent", user_agent);
        httpConnection.connect();
        response_code = httpConnection.getResponseCode();

        if (response_code == HttpURLConnection.HTTP_MOVED_PERM || response_code == HttpURLConnection.HTTP_MOVED_TEMP || response_code == HttpURLConnection.HTTP_SEE_OTHER) {
          String movedLocation = httpConnection.getHeaderField(HEADER_FIELD_LOCATION);
          System.out.println(method + ": " + urlString + " (" + response_code + ") => Location-Header: " + movedLocation);
          if (movedLocation != null && !movedLocation.isEmpty() && cnt_redir <= MAX_HOP_COUNT) {
            movedLocation = mergePaths(urlString, movedLocation);
            response_code = getResponseCode(movedLocation, (cnt_redir + 1), true, METHOD_HEAD);
          }
        }
        else if (response_code == HttpURLConnection.HTTP_BAD_METHOD && method.equalsIgnoreCase(METHOD_HEAD)) {
          System.out.println(method + ": " + urlString + " (" + response_code + ") => Re-try with : " + METHOD_GET);
          response_code = getResponseCode(urlString, (cnt_redir + 1), true, METHOD_GET);
        }
        else { //Received other than 30x or 405 response. We're finished.
          System.out.println(method + ": " + urlString + " (" + response_code + ") => Re-tries : " + cnt_redir + ", realURL: " + httpConnection.getURL());
        }
      }
    }
    catch (IOException e) {
      System.out.println("Could not retrieve response for [" + urlString + "], IOException msg: " + e.getMessage());
    }
    return response_code;
  }

  public static String mergePaths(String baseurl, String newlocation) {

    if (newlocation.startsWith("/")) {
      try {
        URL base = new URL(baseurl);
        URL newone = new URL(base, newlocation);
        System.out.println(newone.toString());
        return newone.toString();
      }
      catch (MalformedURLException e) {
        e.printStackTrace();
        return baseurl;
      }
    }
    else {
      return newlocation;
    }
  }
}