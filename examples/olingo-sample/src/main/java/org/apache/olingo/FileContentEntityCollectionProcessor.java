package org.apache.olingo;

import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.resource.AbstractFileContentEntityCollectionProcessor;
import org.apache.olingo.server.api.uri.UriInfo;

public class FileContentEntityCollectionProcessor extends AbstractFileContentEntityCollectionProcessor {
  @Override
  public EntityCollection getData(EdmEntitySet edmEntitySet, UriInfo uriInfo) throws Exception {
    EntityCollection data = new EntityCollection();

    Entity entity = new Entity();
    entity.addProperty(new Property(null, "name", ValueType.PRIMITIVE, "test"));
    entity.addProperty(new Property(null, "content", ValueType.PRIMITIVE, "hello world"));
    data.getEntities().add(entity);

    return data;
  }
}