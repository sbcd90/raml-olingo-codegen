package org.raml.olingo.codegen.core;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.raml.model.MimeType;
import org.raml.model.Resource;

public class Names {

  public static String getShortMimeType(final MimeType mimeType) {
    if (mimeType == null) {
      return "";
    }
    String subType = StringUtils.substringAfter(mimeType.getType()
      .toLowerCase(Constants.DEFAULT_LOCALE), "/");

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
                                                  Configuration configuration,
                                                  String... suffix) {
    final String resourceInterfaceName = buildJavaFriendlyName(
      StringUtils.defaultIfBlank(resource.getDisplayName(), resource.getRelativeUri()));

    String interfaceNameSuffix = configuration.getInterfaceNameSuffix();
    if (suffix.length > 0)
      interfaceNameSuffix = suffix[0];

    return StringUtils.isBlank(resourceInterfaceName) ? "Root" :
      resourceInterfaceName.concat(interfaceNameSuffix);
  }

  public static String buildJavaFriendlyName(final String source) {
    final String baseName = source.replaceAll("[\\W_]", " ");

    String friendlyName = WordUtils.capitalize(baseName).replaceAll("[\\W_]", "");

    if (NumberUtils.isDigits(StringUtils.left(friendlyName, 1))) {
      friendlyName = "_" + friendlyName;
    }

    return friendlyName;
  }
}