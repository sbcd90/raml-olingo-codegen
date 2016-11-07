package org.raml.olingo.codegen.core.extn;

import org.raml.model.Resource;
import org.raml.olingo.codegen.core.Configuration;
import org.raml.olingo.codegen.core.Names;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceInterfaceNameWithVersion extends AbstractGeneratorExtension implements InterfaceNameBuilderExtension {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceInterfaceNameWithVersion.class);

  @Override
  public String buildResourceInterfaceName(Resource resource) {
    Configuration configuration = new Configuration();
    configuration.setInterfaceNameSuffix("I");
    String resourceName = Names.buildResourceInterfaceName(resource, configuration);
    String version = getRaml().getVersion();
    if (version != null) {
      resourceName += version.toUpperCase();
      LOGGER.debug("Generated resource name: " + resourceName);
      return resourceName;
    }
    String message = "No version found in RAML.";
    LOGGER.warn(message);
    throw new IllegalArgumentException(message);
  }
}