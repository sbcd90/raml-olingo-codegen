package org.raml.olingo.codegen.core.extn;

import org.raml.model.Resource;

public interface InterfaceNameBuilderExtension extends GeneratorExtension {

  String buildResourceInterfaceName(final Resource resource);
}