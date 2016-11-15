package org.apache.olingo;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.resource.AbstractFileContentEntityProcessor;
import org.apache.olingo.server.api.uri.UriParameter;

import java.util.List;

public class FileContentEntityProcessor extends AbstractFileContentEntityProcessor {

  @Override
  public void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) throws Exception {

  }

  @Override
  public Entity createEntityData(EdmEntitySet edmEntitySet, Entity requestEntity) throws Exception {
    return requestEntity;
  }

  @Override
  public void updateEntityData(EdmEntitySet edmEntitySet, Entity requestEntity, HttpMethod httpMethod,
                               List<UriParameter> keyPredicates) throws Exception {

  }
}