package org.apache.olingo;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.resource.AbstractSongsEntityCollectionProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;

import java.util.List;
import java.util.Map;

public class SongsEntityCollectionProcessor extends AbstractSongsEntityCollectionProcessor {
  @Override
  public EntityCollection getData(EdmEntitySet edmEntitySet, UriInfo uriInfo, Map<String,
    SystemQueryOption> systemQueryParameters, Map<String, CustomQueryOption> customQueryParameters) throws Exception {
    EntityCollection data = new EntityCollection();

    List<Entity> entities = data.getEntities();

    Entity entity = new Entity();
    entity.addProperty(new Property(null, "test", ValueType.PRIMITIVE, 1));
    entity.addProperty(new Property(null, "songTitle", ValueType.PRIMITIVE, "check"));
    entity.addProperty(new Property(null, "albumId", ValueType.PRIMITIVE, "Anu Malik"));

    data.getEntities().add(entity);
    return data;
  }
}