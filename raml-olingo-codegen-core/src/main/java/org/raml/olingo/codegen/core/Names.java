package org.raml.olingo.codegen.core;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.olingo.codegen.core.extn.GeneratorExtension;
import org.raml.olingo.codegen.core.extn.NestedSchemaNameComputer;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.remove;
import static org.raml.olingo.codegen.core.Constants.DEFAULT_LOCALE;

public class Names {

  public static String getShortMimeType(final MimeType mimeType) {
    if (mimeType == null) {
      return "";
    }
    String subType = StringUtils.substringAfter(mimeType.getType()
      .toLowerCase(DEFAULT_LOCALE), "/");

    if (subType.contains(".")) {
      StringBuilder sb  = new StringBuilder();
      for (String s: subType.split("\\W+")) {
        sb.append(sb.length() == 0 ? s: StringUtils.capitalize(s));
      }
      return sb.toString();
    } else {
      return StringUtils.remove(
        StringUtils.remove(
          StringUtils.remove(subType, "x-www-"), "+"), "-");
    }
  }

  public static String buildResourceInterfaceName(final Resource resource,
                                                  Configuration configuration) {
    return buildResourceInterfaceName(resource, configuration, configuration.getEntityCollectionInterfaceNameSuffix());
  }

  public static String buildResourceInterfaceName(final Resource resource,
                                                  Configuration configuration,
                                                  String suffix) {
    final String resourceInterfaceName = buildJavaFriendlyName(
      StringUtils.defaultIfBlank(resource.getDisplayName(), resource.getRelativeUri()));

    return StringUtils.isBlank(resourceInterfaceName) ? "Root" :
      "Abstract".concat(resourceInterfaceName).concat(suffix);
  }

  public static String buildJavaFriendlyName(final String source) {
    final String baseName = source.replaceAll("[\\W_]", " ");

    String friendlyName = WordUtils.capitalize(baseName).replaceAll("[\\W_]", "");

    if (NumberUtils.isDigits(StringUtils.left(friendlyName, 1))) {
      friendlyName = "_" + friendlyName;
    }

    return friendlyName;
  }

  public static String buildNestedSchemaName(final MimeType mimeType,
                                             final Configuration configuration) {
    for (GeneratorExtension e: configuration.getExtensions()) {
      if (e instanceof NestedSchemaNameComputer) {
        NestedSchemaNameComputer nc = (NestedSchemaNameComputer) e;
        String computed = nc.computeNestedSchemaName(mimeType);
        if (computed != null) {
          return computed;
        }
      }
    }

    if (!StringUtils.isBlank(mimeType.getSchema())) {
      try {
        JsonObject p = (JsonObject) new JsonParser().parse(mimeType.getSchema());
        JsonElement title = p.get("title");
        if (title != null && title.getAsString() != null) {
          return title.getAsString();
        }
      } catch (Exception ex) {
        // incorrect value
        return null;
      }
    }
    return getShortMimeType(mimeType) +
      (isBlank(mimeType.getSchema()) ? mimeType.hashCode() : mimeType.getSchema().hashCode());
  }
}