
package org.apache.olingo.resource;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpMethod;
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

public abstract class AbstractFileContentEntityProcessor
    implements EntityProcessor
{

    protected OData oData;
    protected ServiceMetadata serviceMetadata;
    private Logger LOG = (Logger.getLogger(AbstractFileContentEntityProcessor.class.getName()));

    @Override
    public void init(OData oData, ServiceMetadata serviceMetadata) {
        this.oData = oData;
        this.serviceMetadata = serviceMetadata;
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
			createdEntity = createEntityData(edmEntitySet, requestEntity);
		} catch(Exception e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		}

		ContextURL contextURL = ContextURL.with().entitySet(edmEntitySet).build();
		EntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextURL).build();

		if (!"".contains(responseFormat.toContentTypeString())) {
			LOG.log(Level.SEVERE, "The response Format is not supported");
		}

		ODataSerializer serializer = oData.createSerializer(responseFormat);
		SerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, opts);

		oDataResponse.setContent(serializedResponse.getContent());
		oDataResponse.setStatusCode(201);
		oDataResponse.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());

    }

    public abstract Entity createEntityData(EdmEntitySet edmEntitySet, Entity requestEntity)
        throws Exception
    ;

    @Override
    public void updateEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();
		EdmEntityType edmEntityType = edmEntitySet.getEntityType();

		InputStream requestInputStream = oDataRequest.getBody();

		if (!"".contains(requestFormat.toContentTypeString())) {
			LOG.log(Level.SEVERE, "The request Format is not supported");
		}

		ODataDeserializer deserializer = oData.createDeserializer(requestFormat);
		DeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);
		Entity requestEntity = result.getEntity();

		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();
		HttpMethod httpMethod = oDataRequest.getMethod();

		if (!httpMethod.name().equals("PUT")) {
			throw new ODataApplicationException("The HTTP Method doesn't match with Raml defined method", 500, Locale.ENGLISH);
		}

		try {
			updateEntityData(edmEntitySet, requestEntity, httpMethod, keyPredicates);
		} catch(Exception e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		}

		oDataResponse.setStatusCode(204);
    }

    public abstract void updateEntityData(EdmEntitySet edmEntitySet, Entity requestEntity, HttpMethod httpMethod, List<UriParameter> keyPredicates)
        throws Exception
    ;

    @Override
    public void deleteEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo)
        throws ODataApplicationException, ODataLibraryException
    {
        
		List<UriResource> resourcePaths = uriInfo.getUriResourceParts();

		UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);
		EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

		List<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();

		try {
			deleteEntityData(edmEntitySet, keyPredicates);
		} catch(Exception e) {
			throw new ODataApplicationException(e.getMessage(), 500, Locale.ENGLISH);
		}

		oDataResponse.setStatusCode(204);

    }

    public abstract void deleteEntityData(EdmEntitySet edmEntitySet, List<UriParameter> keyPredicates)
        throws Exception
    ;

    @Override
    public void readEntity(ODataRequest oDataRequest, ODataResponse oDataResponse, UriInfo uriInfo, ContentType contentType)
        throws ODataApplicationException, ODataLibraryException
    {
    }

}
