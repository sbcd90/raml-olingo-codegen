
package org.apache.olingo.resource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
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
import org.apache.olingo.server.api.deserializer.DeserializerResult;
import org.apache.olingo.server.api.deserializer.ODataDeserializer;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;

public abstract class AbstractSongsEntityProcessor
    implements EntityProcessor
{

    protected OData oData;
    protected ServiceMetadata serviceMetadata;
    private Logger LOG = (Logger.getLogger(AbstractSongsEntityProcessor.class.getName()));

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.oData = oData;
        this.serviceMetadata = serviceMetadata;
    }

    public Map<String, SystemQueryOption> getSystemQueryParameters(UriInfo uriInfo) {
        
		Map<String, SystemQueryOption> systemQueryParams = new HashMap<String, SystemQueryOption>();


		return systemQueryParams;
    }

    public Map<String, CustomQueryOption> getCustomQueryParameters(UriInfo uriInfo) {
        
		Map<String, CustomQueryOption> customQueryParams = new HashMap<String, CustomQueryOption>();

		List<CustomQueryOption> customQueryOptions = uriInfo.getCustomQueryOptions();

		if (customQueryOptions.indexOf("access_token") != -1) {
			customQueryParams.put("access_token", customQueryOptions.get(customQueryOptions.indexOf("access_token")));
		}

		return customQueryParams;
    }

    @Override
    public void createEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		InputStream requestInputStream = oDataRequest.getBody();

		if (!"application/json".contains(requestFormat.toContentTypeString())) {
			LOG.log(Level.SEVERE, "The request Format is not supported");
		}

		ODataDeserializer deserializer = oData.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();

		Entity createdEntity = null;
		try {
			createdEntity = createEntityData(edmEntitySet, requestEntity, getSystemQueryParameters(uriInfo), getCustomQueryParameters(uriInfo));
		} catch(Exception e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		}

		ContextURL contextURL = ContextURL.with().entitySet(edmEntitySet).build();
		EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextURL).build();

		if (!"application/json".contains(responseFormat.toContentTypeString())) {
			LOG.log(Level.SEVERE, "The response Format is not supported");
		}

		ODataSerializer serializer = oData.createSerializer(responseFormat);
		SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, opts);

		oDataResponse.setContent(serializedResponse.getContent());
		oDataResponse.setStatusCode(200);
		oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

    }

    /**
     * Add a new song to Jukebox.
     * 
     * 
     */
    public abstract Entity createEntityData(EdmEntitySet edmEntitySet, Entity requestEntity, Map<String, SystemQueryOption> systemQueryParameters, Map<String, CustomQueryOption> customQueryParameters)
        throws Exception
    ;

    @Override
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();
		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		Entity entity;
		try {
			entity = getData(edmEntitySet, keyPredicates);
		} catch(Exception e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		}

		if (!"application/json".contains(contentType.toContentTypeString())) {
			LOG.log(Level.SEVERE, "The content-type is not supported");
		}

		ODataSerializer serializer = oData.createSerializer(contentType);

		EdmEntityType edmEntityType = edmEntitySet.getEntityType();
		ContextURL contextURL = ContextURL.with().entitySet(edmEntitySet).build();

		EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextURL).build();
		SerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, opts);
		InputStream serializedContent = serializerResult.getContent();

		oDataResponse.setStatusCode(404);
		oDataResponse.setContent(serializedContent);
		oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());

    }

    /**
     * Get the song with `songId = {songId}`
     * 
     */
    public abstract Entity getData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
        throws Exception
    ;

    @Override
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, ODataLibraryException
    {
    }

    @Override
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo)
        throws ODataApplicationException, ODataLibraryException
    {
    }

}
