package nl.knaw.dans.kb.resolver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlResolver {

  private static final int MAX_HOP_COUNT = 10;
  private static final String HEADER_FIELD_LOCATION = "Location";
  private static final String user_agent = "nbn-urlresolver - harvester@dans.knaw.nl";
  public static final String METHOD_HEAD = "HEAD";
  public static final String METHOD_GET = "GET";

  private static final Logger logger = LoggerFactory.getLogger(UrlResolver.class);

  public static int getResponseCode(String urlString, boolean followredirects) {
    return UrlResolver.getResponseCode(urlString, 0, followredirects, METHOD_HEAD);
  }

  /**
   * Gets the response code.
   *
   * @param urlString       the url string
   * @param cnt_redir       the redirected number counter
   * @param followredirects a boolean indicating whether or not to follow HTTP redirects by the java.net.URLConnection library.
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
        // Setting #.setInstanceFollowRedirects(true) is faster and probably more reliaable, since we don't have to follow the redirs ourselves. However, this hides the redirect url-hubs from this method. It still requires 'manual' action if the redirect encounters a protocol change (http => hhtps).
        httpConnection.setInstanceFollowRedirects(followredirects);  //it only applies to redirects to the same protocol. An HttpURLConnection won't automatically follow redirects to a different protocol, even if the redirect flag is set.
        httpConnection.addRequestProperty("User-Agent", user_agent);
        httpConnection.connect();
        response_code = httpConnection.getResponseCode();

        if (response_code == HttpURLConnection.HTTP_MOVED_PERM || response_code == HttpURLConnection.HTTP_MOVED_TEMP || response_code == HttpURLConnection.HTTP_SEE_OTHER) {
          String movedLocation = httpConnection.getHeaderField(HEADER_FIELD_LOCATION);
//          System.out.println(method + ": " + urlString + " (" + response_code + ") => Location-Header: " + movedLocation);
          if (movedLocation != null && !movedLocation.isEmpty() && cnt_redir <= MAX_HOP_COUNT) {
            movedLocation = mergePaths(urlString, movedLocation);
            response_code = getResponseCode(movedLocation, (cnt_redir + 1), true, METHOD_HEAD);
          }
        }
        else if (response_code == HttpURLConnection.HTTP_BAD_METHOD && method.equalsIgnoreCase(METHOD_HEAD)) {
          //          System.out.println(method + ": " + urlString + " (" + response_code + ") => Re-try with : " + METHOD_GET);
          response_code = getResponseCode(urlString, (cnt_redir + 1), true, METHOD_GET);
        }
        else { //Received other than 30x or 405 response. We're finished.
          logger.info("Resolved: " + urlString + " (" + response_code + ") => realURL: " + httpConnection.getURL() + ". #Re-dirs: " + cnt_redir );
        }
      }
    }
    catch (IOException e) {
      logger.info("Could not resolve response for [" + urlString + "], IOException: " + e.getMessage());
    }
    return response_code;
  }

  public static String mergePaths(String baseurl, String newlocation) {

    if (newlocation.startsWith("/")) {
      try {
        URL base = new URL(baseurl);
        URL newone = new URL(base, newlocation);
        //        System.out.println(newone.toString());
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