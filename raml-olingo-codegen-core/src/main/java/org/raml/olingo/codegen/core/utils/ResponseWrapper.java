package org.raml.olingo.codegen.core.utils;

import com.sun.codemodel.JClass;
import org.apache.commons.lang3.math.NumberUtils;
import org.raml.model.MimeType;
import org.raml.model.Response;
import org.raml.olingo.codegen.core.Types;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;


public class ResponseWrapper {
  private Map<String, Response> responses;

  private String resourceName;

  private Set<String> mimeTypes;
  private List<Integer> statusCodes;

  public Set<String> getMimeTypes() {
    return mimeTypes;
  }

  public List<Integer> getStatusCodes() {
    return statusCodes;
  }

  public ResponseWrapper(String resourceName, Map<String, Response> responses) {
    this.responses = responses;

    this.mimeTypes = new HashSet<String>();
    this.statusCodes = new ArrayList<Integer>();

    this.resourceName = resourceName;
  }

  public void parseResponses(Types types, Map<String, JClass> schemas) throws IOException {
    for (Map.Entry<String, Response> response: responses.entrySet()) {
      int statusCode = NumberUtils.toInt(response.getKey());
      statusCodes.add(statusCode);

      if (response.getValue().hasBody()) {
        Map<String, MimeType> body = response.getValue().getBody();

        for (Map.Entry<String, MimeType> mimeType: body.entrySet()) {
          mimeTypes.add(mimeType.getKey());

          if (mimeType.getValue().getSchema() != null) {
            schemas.put(resourceName,
              types.getSchemaClass(mimeType.getValue()));
          }
        }
      }
    }
  }
}