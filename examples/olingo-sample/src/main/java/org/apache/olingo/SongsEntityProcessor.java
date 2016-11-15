package org.apache.olingo;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.resource.AbstractSongsEntityProcessor;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;

import java.util.List;
import java.util.Map;

public class SongsEntityProcessor extends AbstractSongsEntityProcessor {
  @Override
  public Entity createEntityData(EdmEntitySet edmEntitySet, Entity requestEntity,
                                 Map<String, SystemQueryOption> systemQueryParameters,
                                 Map<String, CustomQueryOption> customQueryParameters) throws Exception {
    return requestEntity;
  }

  @Override
  public Entity getData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates) throws Exception {
    Entity entity = new Entity();
    entity.addProperty(new Property(null, "test", ValueType.PRIMITIVE, 1));
    entity.addProperty(new Property(null, "songTitle", ValueType.PRIMITIVE, "check"));
    entity.addProperty(new Property(null, "albumId", ValueType.PRIMITIVE, "Anu Malik"));
    return entity;
  }
}