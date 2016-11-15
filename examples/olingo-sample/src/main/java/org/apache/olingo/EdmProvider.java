package org.apache.olingo;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.resource.metadata.AbstractEdmProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class EdmProvider extends AbstractEdmProvider {
  @Override
  public List<CsdlProperty> getFilecontentProperties() {
    CsdlProperty name = new CsdlProperty().setName("name").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
    CsdlProperty content = new CsdlProperty().setName("content").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

    return Arrays.asList(name, content);
  }

  @Override
  public List<CsdlPropertyRef> getFilecontentKeys() {
    CsdlPropertyRef propertyRef = new CsdlPropertyRef();
    propertyRef.setName("name");

    return Collections.singletonList(propertyRef);
  }
}