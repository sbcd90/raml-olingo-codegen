
package org.apache.olingo.resource;

import java.util.List;
import java.util.logging.Logger;

import org.apache.olingo.FileContentEntityCollectionProcessor;
import org.apache.olingo.SongsEntityCollectionProcessor;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;

import static org.apache.olingo.resource.metadata.AbstractEdmProvider.ES_FILECONTENT_NAME;
import static org.apache.olingo.resource.metadata.AbstractEdmProvider.ES_SONGS_NAME;

public class CommonEntityCollectionProcessor
    implements EntityCollectionProcessor
{

    protected OData oData;
    protected ServiceMetadata serviceMetadata;
    private Logger LOG = (Logger.getLogger(CommonEntityCollectionProcessor.class.getName()));

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.oData = oData;
        this.serviceMetadata = serviceMetadata;
    }

    @Override
    public void readEntityCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		switch (edmEntitySet.getName()) {
			case ES_SONGS_NAME : 
				SongsEntityCollectionProcessor SongsEntityCollectionProcessor = new SongsEntityCollectionProcessor();
				SongsEntityCollectionProcessor.init(oData, serviceMetadata);
				SongsEntityCollectionProcessor.readEntityCollection(oDataRequest, oDataResponse, uriInfo, contentType);
				break;
			case ES_FILECONTENT_NAME : 
				FileContentEntityCollectionProcessor FileContentEntityCollectionProcessor = new FileContentEntityCollectionProcessor();
				FileContentEntityCollectionProcessor.init(oData, serviceMetadata);
				FileContentEntityCollectionProcessor.readEntityCollection(oDataRequest, oDataResponse, uriInfo, contentType);
				break;
		}

    }

}
