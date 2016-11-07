package org.raml.olingo.codegen.core;

import com.sun.codemodel.JDefinedClass;
import org.apache.commons.lang3.math.NumberUtils;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;

import java.util.Collection;
import java.util.Map;

public class Generator extends AbstractGenerator {

  @Override
  protected void addResourceMethod(JDefinedClass resourceInterface, Resource resource,
                                   Action action, MimeType bodyMimeType,
                                   boolean addBodyMimeTypeInMethodName,
                                   Collection<MimeType> uniqueResponseMimeTypes) throws Exception {

    String httpMethod = action.getType().name();
    String description = action.getDescription();

    int statusCode = 0;
    Map<String, Response> responses = action.getResponses();

    for (Map.Entry<String, Response> response: responses.entrySet()) {
      statusCode = NumberUtils.toInt(response.getKey());
    }

    if (httpMethod.equals("GET")) {
      OlingoCodeGenerator.generateInitMethod(resourceInterface, context, types);

      OlingoCodeGenerator.generateReadEntityCollectionProcessorMethod(resourceInterface, context,
        types, description, statusCode);
    }
  }
}