package org.raml.olingo.codegen.core;

import java.util.Locale;

public abstract class Constants {

  public static Locale DEFAULT_LOCALE = Locale.ENGLISH;

  private Constants() {
    throw new UnsupportedOperationException();
  }
}