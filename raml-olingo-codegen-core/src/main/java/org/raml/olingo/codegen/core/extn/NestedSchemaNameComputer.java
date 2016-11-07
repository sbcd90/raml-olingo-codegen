package org.raml.olingo.codegen.core.extn;

import org.raml.model.MimeType;

public interface NestedSchemaNameComputer extends GeneratorExtension {

  String computeNestedSchemaName(MimeType mime);
}