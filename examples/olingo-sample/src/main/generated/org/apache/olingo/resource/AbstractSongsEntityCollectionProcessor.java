
package org.apache.olingo.resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.serializer.EntityCollectionSerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;


/**
 * Collection of available songs in Jukebox.
 * 
 */
public abstract class AbstractSongsEntityCollectionProcessor
    implements EntityCollectionProcessor
{

    protected OData oData;
    protected ServiceMetadata serviceMetadata;
    private Logger LOG = (Logger.getLogger(AbstractSongsEntityCollectionProcessor.class.getName()));

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.oData = oData;
        this.serviceMetadata = serviceMetadata;
    }

    public Map<String, SystemQueryOption> getSystemQueryParameters(UriInfo uriInfo) {
        
		Map<String, SystemQueryOption> systemQueryParams = new HashMap<String, SystemQueryOption>();

		systemQueryParams.put("$orderby", uriInfo.getOrderByOption());

		return systemQueryParams;
    }

    public Map<String, CustomQueryOption> getCustomQueryParameters(UriInfo uriInfo) {
        
		Map<String, CustomQueryOption> customQueryParams = new HashMap<String, CustomQueryOption>();

		List<CustomQueryOption> customQueryOptions = uriInfo.getCustomQueryOptions();

		if (customQueryOptions.indexOf("songTitle") != -1) {
			customQueryParams.put("songTitle", customQueryOptions.get(customQueryOptions.indexOf("songTitle")));
		}

		return customQueryParams;
    }

    @Override
    public void readEntityCollection(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		EntityCollection entitySet;
		try {
			entitySet = getData(edmEntitySet, uriInfo, getSystemQueryParameters(uriInfo), getCustomQueryParameters(uriInfo));
		} catch(Exception e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		}

		if (!"application/xml,application/json".contains(contentType.toContentTypeString())) {
			LOG.log(Level.SEVERE, "The content-type is not supported");
		}

		ODataSerializer serializer = oData.createSerializer(contentType);

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		ContextURL contextURL = ContextURL.with().entitySet(edmEntitySet).build();

		final String id = oDataRequest.getRawBaseUri() + "/" + edmEntitySet.getName();
		EntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextURL).build();
		SerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet, opts);
		InputStream serializedContent = serializerResult.getContent();

		oDataResponse.setStatusCode(200);
		oDataResponse.setContent(serializedContent);
		oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());

    }

    /**
     * Get a list of songs.
     * 
     */
    public abstract EntityCollection getData(EdmEntitySet edmEntitySet, UriInfo uriInfo, Map<String, SystemQueryOption> systemQueryParameters, Map<String, CustomQueryOption> customQueryParameters)
        throws Exception
    ;

}
