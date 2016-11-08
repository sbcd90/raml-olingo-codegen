package org.raml.olingo.codegen.core.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.raml.model.MimeType;
import org.raml.model.Response;

import java.util.*;

public class ResponseWrapper {
  private Map<String, Response> responses;

  private Set<String> mimeTypes;
  private List<Integer> statusCodes;

  public Set<String> getMimeTypes() {
    return mimeTypes;
  }

  public List<Integer> getStatusCodes() {
    return statusCodes;
  }

  public ResponseWrapper(Map<String, Response> responses) {
    this.responses = responses;

    this.mimeTypes = new HashSet<String>();
    this.statusCodes = new ArrayList<Integer>();
  }

  public void parseResponses() {
    for (Map.Entry<String, Response> response: responses.entrySet()) {
      int statusCode = NumberUtils.toInt(response.getKey());
      statusCodes.add(statusCode);

      if (response.getValue().hasBody()) {
        Map<String, MimeType> body = response.getValue().getBody();

        for (Map.Entry<String, MimeType> mimeType: body.entrySet()) {
          mimeTypes.add(mimeType.getKey());
        }
      }
    }
  }
}