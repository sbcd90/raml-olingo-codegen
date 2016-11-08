package org.raml.olingo.codegen.core;

import com.sun.codemodel.JDefinedClass;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.olingo.codegen.core.utils.ResponseWrapper;

import java.util.*;

public class Generator extends AbstractGenerator {

  @Override
  protected void addResourceMethod(JDefinedClass resourceInterface, Resource resource,
                                   Action action, MimeType bodyMimeType,
                                   boolean addBodyMimeTypeInMethodName,
                                   Collection<MimeType> uniqueResponseMimeTypes) throws Exception {

    String httpMethod = action.getType().name();
    String description = action.getDescription();

    /**
     * consider request body
     */
    Set<String> reqMimeTypes = new HashSet<String>();
    if (action.hasBody()) {
      Map<String, MimeType> body = action.getBody();
      for (Map.Entry<String, MimeType> mimeType: body.entrySet()) {
        reqMimeTypes.add(mimeType.getKey());
      }
    }

    /**
     * consider response status code & body
     */
    int statusCode = 0;
    Map<String, Response> responses = action.getResponses();
    ResponseWrapper responseWrapper = new ResponseWrapper(responses);
    responseWrapper.parseResponses();

    Set<String> mimeTypes = responseWrapper.getMimeTypes();
    List<Integer> statusCodes = responseWrapper.getStatusCodes();


    for (Map.Entry<String, Response> response: responses.entrySet()) {
      statusCode = NumberUtils.toInt(response.getKey());
    }

    if (httpMethod.equals("GET")) {

      OlingoCodeGenerator.generateInitMethod(resourceInterface, context, types);

      OlingoCodeGenerator.generateReadEntityCollectionProcessorMethod(resourceInterface, context,
        types, description, statusCode, statusCodes, mimeTypes);
    } else if (httpMethod.equals("POST")) {
      if (statusCode == 0) {
        statusCode = HttpStatusCode.CREATED.getStatusCode();
      }

      OlingoCodeGenerator.generateCreateEntityMethod(resourceInterface, context,
        types, description, statusCode, statusCodes, reqMimeTypes, mimeTypes);
    } else if (httpMethod.equals("PUT") || httpMethod.equals("POST")) {
      if (statusCode == 0) {
        statusCode = HttpStatusCode.NO_CONTENT.getStatusCode();
      }

      OlingoCodeGenerator.generateUpdateEntityMethod(resourceInterface, context,
        types, description, statusCode, action.getType().name(), statusCodes, reqMimeTypes, mimeTypes);
    } else if (httpMethod.equals("DELETE")) {
      if (statusCode == 0) {
        statusCode = HttpStatusCode.NO_CONTENT.getStatusCode();
      }

      OlingoCodeGenerator.generateDeleteEntityMethod(resourceInterface, context,
        types, description, statusCode, statusCodes);
    }
  }
}