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
package nl.knaw.dans.kb.resolver;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class UrlResolverTest {

  private static final Logger logger = LoggerFactory.getLogger(UrlResolverTest.class);
  String[] locations = { "http://dds.nl", "http://localhost:8081/redir", "http://disq.us/8o29ub", "http://bit.ly/1Wd7H3h", "http://httpstat.us/500", "http://httpstat.us/405", "https://www.persistent-identifier.nl/urn:nbn:nl:ui:12-d68990f1-78fb-4c7d-a407-45477ac821ad", "https://pure.buas.nl/en/publications/d8bf9616-8cd9-4b1f-ad3e-e3277b92adfa", "http://resolver.tudelft.nl/uuid%3Afcf57b9b-1a1d-410b-9e85-08c8fc72d93b?some=param" };
  int[] statuscodes = {200, -1, 410, -1, 500, 405, 200, 200, 200};

  @Test
  public void testResolveRealUrls() {

    int i = 0;
    for (String location : locations) {
      int statuscode = UrlResolver.getResponseCode(location, true);
      assertEquals(statuscode, statuscodes[i]);
      System.out.println( statuscode + ": " + location);
      i++;
    }
  }
}
