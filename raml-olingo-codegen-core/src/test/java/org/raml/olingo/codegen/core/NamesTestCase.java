package org.raml.olingo.codegen.core;

import org.junit.Assert;
import org.junit.Test;
import org.raml.model.MimeType;

public class NamesTestCase {

  @Test
  public void testGetShortMimeType() {
    Assert.assertEquals(Names.getShortMimeType(null), "");
    Assert.assertEquals(Names.getShortMimeType(new MimeType("text/xml")), "xml");
    Assert.assertEquals(Names.getShortMimeType(new MimeType("application/json")), "json");
    Assert.assertEquals(Names.getShortMimeType(new MimeType("application/hal+json")), "haljson");
    Assert.assertEquals(Names.getShortMimeType(new MimeType("application/octet-stream")), "octetstream");
    Assert.assertEquals(Names.getShortMimeType(new MimeType("application/x-www-form-urlencoded")), "formurlencoded");
    Assert.assertEquals(Names.getShortMimeType(new MimeType("application/vnd.example.v1+json")), "vndExampleV1Json");
  }

  @Test
  public void testBuildJavaFriendlyName() {
    Assert.assertEquals(Names.buildJavaFriendlyName("/songs"), "Songs");
    Assert.assertEquals(Names.buildJavaFriendlyName("/Songs"), "Songs");
    Assert.assertEquals(Names.buildJavaFriendlyName("/songs/get"), "SongsGet");
  }
}