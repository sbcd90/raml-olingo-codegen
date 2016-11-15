
package org.apache.olingo.resource;

import java.util.List;
import java.util.logging.Logger;

import org.apache.olingo.FileContentEntityProcessor;
import org.apache.olingo.SongsEntityProcessor;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import static org.apache.olingo.resource.metadata.AbstractEdmProvider.ES_FILECONTENT_NAME;
import static org.apache.olingo.resource.metadata.AbstractEdmProvider.ES_SONGS_NAME;

public class CommonEntityProcessor
    implements EntityProcessor
{

    protected OData oData;
    protected ServiceMetadata serviceMetadata;
    private Logger LOG = (Logger.getLogger(CommonEntityProcessor.class.getName()));

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.oData = oData;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		switch (edmEntitySet.getName()) {
			case ES_SONGS_NAME : 
				SongsEntityProcessor SongsEntityProcessor = new SongsEntityProcessor();
				SongsEntityProcessor.init(oData, serviceMetadata);
				SongsEntityProcessor.readEntity(oDataRequest, oDataResponse, uriInfo, contentType);
				break;
			case ES_FILECONTENT_NAME : 
				FileContentEntityProcessor FileContentEntityProcessor = new FileContentEntityProcessor();
				FileContentEntityProcessor.init(oData, serviceMetadata);
				FileContentEntityProcessor.readEntity(oDataRequest, oDataResponse, uriInfo, contentType);
				break;
		}

    }

    @Override
    public void createEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		switch (edmEntitySet.getName()) {
			case ES_SONGS_NAME : 
				SongsEntityProcessor SongsEntityProcessor = new SongsEntityProcessor();
				SongsEntityProcessor.init(oData, serviceMetadata);
				SongsEntityProcessor.createEntity(oDataRequest, oDataResponse, uriInfo, requestFormat, responseFormat);
				break;
			case ES_FILECONTENT_NAME : 
				FileContentEntityProcessor FileContentEntityProcessor = new FileContentEntityProcessor();
				FileContentEntityProcessor.init(oData, serviceMetadata);
				FileContentEntityProcessor.createEntity(oDataRequest, oDataResponse, uriInfo, requestFormat, responseFormat);
				break;
		}

    }

    @Override
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		switch (edmEntitySet.getName()) {
			case ES_SONGS_NAME : 
				SongsEntityProcessor SongsEntityProcessor = new SongsEntityProcessor();
				SongsEntityProcessor.init(oData, serviceMetadata);
				SongsEntityProcessor.updateEntity(oDataRequest, oDataResponse, uriInfo, requestFormat, responseFormat);
				break;
			case ES_FILECONTENT_NAME : 
				FileContentEntityProcessor FileContentEntityProcessor = new FileContentEntityProcessor();
				FileContentEntityProcessor.init(oData, serviceMetadata);
				FileContentEntityProcessor.updateEntity(oDataRequest, oDataResponse, uriInfo, requestFormat, responseFormat);
				break;
		}

    }

    @Override
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		switch (edmEntitySet.getName()) {
			case ES_SONGS_NAME : 
				SongsEntityProcessor SongsEntityProcessor = new SongsEntityProcessor();
				SongsEntityProcessor.init(oData, serviceMetadata);
				SongsEntityProcessor.deleteEntity(oDataRequest, oDataResponse, uriInfo);
				break;
			case ES_FILECONTENT_NAME : 
				FileContentEntityProcessor FileContentEntityProcessor = new FileContentEntityProcessor();
				FileContentEntityProcessor.init(oData, serviceMetadata);
				FileContentEntityProcessor.deleteEntity(oDataRequest, oDataResponse, uriInfo);
				break;
		}

    }

}
