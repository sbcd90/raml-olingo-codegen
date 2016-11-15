
package org.apache.olingo.resource.metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainer;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityContainerInfo;
import org.apache.olingo.commons.api.edm.provider.CsdlEntitySet;
import org.apache.olingo.commons.api.edm.provider.CsdlEntityType;
import org.apache.olingo.commons.api.edm.provider.CsdlProperty;
import org.apache.olingo.commons.api.edm.provider.CsdlPropertyRef;
import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.apache.olingo.commons.api.ex.ODataException;

public abstract class AbstractEdmProvider
    extends CsdlAbstractEdmProvider
{

    public final static String NAMESPACE = "OData.Demo";
    public final static String CONTAINER_NAME = "Container";
    public final static FullQualifiedName CONTAINER = new FullQualifiedName("OData.Demo", "Container");
    public final static String ET_SONGS_NAME = "Song";
    public final static FullQualifiedName ET_SONGS_FQN = new FullQualifiedName("OData.Demo", "Song");
    public final static String ES_SONGS_NAME = "Songs";
    public final static String ET_FILECONTENT_NAME = "FileContent";
    public final static FullQualifiedName ET_FILECONTENT_FQN = new FullQualifiedName("OData.Demo", "FileContent");
    public final static String ES_FILECONTENT_NAME = "FileContents";

    @Override
    public CsdlEntityType getEntityType(FullQualifiedName entityTypeName)
        throws ODataException
    {
        
		if (entityTypeName.equals(ET_SONGS_FQN)) {
			CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_SONGS_NAME);
			entityType.setProperties(getSongsProperties());
			entityType.setKey(getSongsKeys());

			return entityType;
		}

		if (entityTypeName.equals(ET_FILECONTENT_FQN)) {
			CsdlEntityType entityType = new CsdlEntityType();
			entityType.setName(ET_FILECONTENT_NAME);
			entityType.setProperties(getFilecontentProperties());
			entityType.setKey(getFilecontentKeys());

			return entityType;
		}
		return null;
    }

    public List<CsdlProperty> getSongsProperties() {
        
		CsdlProperty test = new CsdlProperty().setName("test").setType(EdmPrimitiveTypeKind.Int32.getFullQualifiedName());
		CsdlProperty songTitle = new CsdlProperty().setName("songTitle").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());
		CsdlProperty albumId = new CsdlProperty().setName("albumId").setType(EdmPrimitiveTypeKind.String.getFullQualifiedName());

		return Arrays.asList(test, songTitle, albumId);

    }

    public List<CsdlPropertyRef> getSongsKeys() {
        
		CsdlPropertyRef test = new CsdlPropertyRef();
		test.setName("test");

		CsdlPropertyRef songTitle = new CsdlPropertyRef();
		songTitle.setName("songTitle");


		return Arrays.asList(test, songTitle);

    }

    public abstract List<CsdlProperty> getFilecontentProperties();

    public abstract List<CsdlPropertyRef> getFilecontentKeys();

    @Override
    public CsdlEntitySet getEntitySet(FullQualifiedName entityContainer, String entitySetName)
        throws ODataException
    {
        
		if (entityContainer.equals(CONTAINER)) {
			if (entitySetName.equals(ES_SONGS_NAME)) {
				CsdlEntitySet entitySet = new CsdlEntitySet();
				entitySet.setName(ES_SONGS_NAME);
				entitySet.setType(ET_SONGS_FQN);

				return entitySet;
			}
			if (entitySetName.equals(ES_FILECONTENT_NAME)) {
				CsdlEntitySet entitySet = new CsdlEntitySet();
				entitySet.setName(ES_FILECONTENT_NAME);
				entitySet.setType(ET_FILECONTENT_FQN);

				return entitySet;
			}
		}

		return null;

    }

    @Override
    public CsdlEntityContainer getEntityContainer()
        throws ODataException
    {
        
		List<CsdlEntitySet> entitySets = new ArrayList<>();
		entitySets.add(getEntitySet(CONTAINER, ES_SONGS_NAME));

		entitySets.add(getEntitySet(CONTAINER, ES_FILECONTENT_NAME));

		CsdlEntityContainer entityContainer = new CsdlEntityContainer();
		entityContainer.setName(CONTAINER_NAME);
		entityContainer.setEntitySets(entitySets);

		return entityContainer;

    }

    @Override
    public List<CsdlSchema> getSchemas()
        throws ODataException
    {
        
		CsdlSchema schema = new CsdlSchema();
		schema.setNamespace(NAMESPACE);

		List<CsdlEntityType> entityTypes = new ArrayList<>();
		entityTypes.add(getEntityType(ET_SONGS_FQN));
		entityTypes.add(getEntityType(ET_FILECONTENT_FQN));
		schema.setEntityTypes(entityTypes);

		schema.setEntityContainer(getEntityContainer());

		List<CsdlSchema> schemas = new ArrayList<>();
		schemas.add(schema);

		return schemas;

    }

    @Override
    public CsdlEntityContainerInfo getEntityContainerInfo(FullQualifiedName entityContainerName)
        throws ODataException
    {
        
		if (entityContainerName == null || entityContainerName.equals(CONTAINER)) {
			CsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();
			entityContainerInfo.setContainerName(CONTAINER);
			return entityContainerInfo;
		}
		return null;
    }

}
