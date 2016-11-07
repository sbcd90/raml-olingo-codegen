package org.raml.olingo.codegen.core;

import com.sun.codemodel.*;
import org.apache.http.HttpStatus;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpMethod;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OlingoCodeGenerator {

  private static final String INIT_METHOD = "init";
  private static final String READ_ENTITY_COLLECTION = "readEntityCollection";

  private static final String CREATE_ENTITY = "createEntity";
  private static final String UPDATE_ENTITY = "updateEntity";
  private static final String DELETE_ENTITY = "deleteEntity";

  public static void generateInitMethod(JDefinedClass resourceInterface, Context context,
                                        Types types) {
    resourceInterface.field(JMod.PROTECTED, OData.class, "oData");
    resourceInterface.field(JMod.PROTECTED, ServiceMetadata.class, "serviceMetadata");

    JMethod initMethod = context.createResourceMethod(resourceInterface, INIT_METHOD,
      types.getGeneratorType(void.class), 0);
    context.addOverrideAnnotationToResourceMethod(initMethod);

    Map<String, Class<?>> initParams = new HashMap<String, Class<?>>();
    initParams.put("oData", OData.class);
    initParams.put("serviceMetadata", ServiceMetadata.class);
    context.addParamsToResourceMethod(initMethod, initParams);

    context.addStmtToResourceMethodBody(initMethod, "this.oData = oData;");
    context.addStmtToResourceMethodBody(initMethod, "this.serviceMetadata = serviceMetadata;");
  }

  public static void generateReadEntityCollectionProcessorMethod(JDefinedClass resourceInterface,
                                                             Context context, Types types, String description,
                                                                 int statusCode) {
    JMethod readMethod = context.createResourceMethod(resourceInterface, READ_ENTITY_COLLECTION,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(readMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(readMethod, ODataLibraryException.class);

    Map<String, Class<?>> params = new HashMap<String, Class<?>>();
    params.put("oDataRequest", ODataRequest.class);
    params.put("oDataResponse", ODataResponse.class);
    params.put("uriInfo", UriInfo.class);
    params.put("contentType", ContentType.class);
    context.addParamsToResourceMethod(readMethod, params);

    context.addOverrideAnnotationToResourceMethod(readMethod);

    JMethod getDataMethod = context.createResourceMethod(resourceInterface, "getData",
      types.getGeneratorType(EntityCollection.class), JMod.ABSTRACT);

    Map<String, Class<?>> getDataParams = new HashMap<String, Class<?>>();
    getDataParams.put("edmEntitySet", EdmEntitySet.class);
    getDataParams.put("uriInfo", UriInfo.class);
    context.addParamsToResourceMethod(getDataMethod, getDataParams);

    if (description != null) {
      getDataMethod.javadoc().add(description);
    }

    generateReadEntityCollectionCode(readMethod, context, statusCode);
  }

  private static void generateReadEntityCollectionCode(JMethod method, Context context, int statusCode) {
    if (statusCode == 0) {
      statusCode = HttpStatusCode.OK.getStatusCode();
    }

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tEntityCollection entitySet = getData(uriInfo, edmEntitySet);\n\n" +
      "\t\tODataSerializer serializer = oData.createSerializer(contentType);\n\n" +
      "\t\tEdmEntityType edmEntityType = edmEntitySet.getEntityType();\n" +
      "\t\tContextURL contextURL = ContextURL.with().entitySet(edmEntitySet).build();\n\n" +
      "\t\tfinal String id = oDataRequest.getRawBaseUri() + \"/\" + edmEntitySet.getName();\n" +
      "\t\tEntityCollectionSerializerOptions opts = EntityCollectionSerializerOptions.with().id(id).contextURL(contextURL).build();\n" +
      "\t\tSerializerResult serializerResult = serializer.entityCollection(serviceMetadata, edmEntityType, entitySet, opts);\n" +
      "\t\tInputStream serializedContent = serializerResult.getContent();\n\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");\n" +
      "\t\toDataResponse.setContent(serializedContent);\n" +
      "\t\toDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());\n";
    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateCreateEntityMethod(JDefinedClass resourceInterface,
                                                Context context, Types types, String description,
                                                int statusCode) {
    JMethod createMethod = context.createResourceMethod(resourceInterface, CREATE_ENTITY,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(createMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(createMethod, ODataLibraryException.class);

    Map<String, Class<?>> params = new HashMap<String, Class<?>>();
    params.put("oDataRequest", ODataRequest.class);
    params.put("oDataResponse", ODataResponse.class);
    params.put("uriInfo", UriInfo.class);
    params.put("requestFormat", ContentType.class);
    params.put("responseFormat", ContentType.class);
    context.addParamsToResourceMethod(createMethod, params);

    context.addOverrideAnnotationToResourceMethod(createMethod);

    JMethod createEntityDataMethod = context.createResourceMethod(resourceInterface, "createEntityData",
      types.getGeneratorType(void.class), JMod.ABSTRACT);

    Map<String, Class<?>> createEntityDataParams = new HashMap<String, Class<?>>();
    createEntityDataParams.put("edmEntitySet", EdmEntitySet.class);
    createEntityDataParams.put("requestEntity", Entity.class);
    context.addParamsToResourceMethod(createEntityDataMethod, createEntityDataParams);

    if (description != null) {
      createEntityDataMethod.javadoc().add(description);
    }

    generateCreateEntityCode(createMethod, context, statusCode);
  }

  private static void generateCreateEntityCode(JMethod method, Context context, int statusCode) {
    if (statusCode == 0) {
      statusCode = HttpStatusCode.CREATED.getStatusCode();
    }

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tEdmEntityType edmEntityType = entitySet.getEntityType();\n\n" +
      "\t\tInputStream requestInputStream = oDataRequest.getBody();\n" +
      "\t\tODataDeserializer deserializer = oData.createDeserializer(requestFormat);\n" +
      "\t\tDeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);\n" +
      "\t\tEntity requestEntity = result.getEntity();\n\n" +
      "\t\tcreateEntityData(edmEntitySet, requestEntity);\n\n" +
      "\t\tContextURL contextURL = ContextURL.with().entitySet(entitySet).build();\n" +
      "\t\tEntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextURL).build();\n\n" +
      "\t\tODataSerializer serializer = oData.createSerializer(responseFormat);\n" +
      "\t\tSerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, opts);\n\n" +
      "\t\toDataResponse.setContent(serializedResponse.getContent());\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");\n" +
      "\t\toDataResponse.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());\n";
    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateUpdateEntityMethod(JDefinedClass resourceInterface,
                                                Context context, Types types, String description,
                                                int statusCode, String methodName) {
    JMethod updateMethod = context.createResourceMethod(resourceInterface, UPDATE_ENTITY,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(updateMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(updateMethod, ODataLibraryException.class);

    Map<String, Class<?>> params = new HashMap<String, Class<?>>();
    params.put("oDataRequest", ODataRequest.class);
    params.put("oDataResponse", ODataResponse.class);
    params.put("uriInfo", UriInfo.class);
    params.put("requestFormat", ContentType.class);
    params.put("responseFormat", ContentType.class);
    context.addParamsToResourceMethod(updateMethod, params);

    context.addOverrideAnnotationToResourceMethod(updateMethod);

    JMethod updateEntityDataMethod = context.createResourceMethod(resourceInterface, "updateEntityData",
      types.getGeneratorType(void.class), JMod.ABSTRACT);

    Map<String, Class<?>> updateEntityDataParams = new HashMap<String, Class<?>>();
    updateEntityDataParams.put("edmEntitySet", EdmEntitySet.class);

    Map<String, JType> updateEntityDataParamsWithTypes = new HashMap<String, JType>();
    JClass listClass = context.getCodeModel().ref(List.class);
    JType listOfUriParamsType = listClass.narrow(UriParameter.class).unboxify();
    updateEntityDataParamsWithTypes.put("keyPredicates", listOfUriParamsType);

    updateEntityDataParams.put("requestEntity", Entity.class);
    updateEntityDataParams.put("httpMethod", HttpMethod.class);

    context.addParamsToResourceMethod(updateEntityDataMethod, updateEntityDataParams);
    context.addParamsToResourceMethod(updateEntityDataMethod, updateEntityDataParamsWithTypes, true);

    if (description != null) {
      updateEntityDataMethod.javadoc().add(description);
    }

    generateUpdateEntityCode(updateMethod, context, statusCode, methodName);
  }

  private static void generateUpdateEntityCode(JMethod method,
                                               Context context, int statusCode, String methodName) {
    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n" +
      "\t\tEdmEntityType edmEntityType = edmEntitySet.getEntityType();\n\n" +
      "\t\tInputStream requestInputStream = oDataRequest.getBody();\n" +
      "\t\tODataDeserializer deserializer = oData.createDeserializer(requestFormat);\n" +
      "\t\tDeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);\n" +
      "\t\tEntity requestEntity = result.getEntity();\n\n" +
      "\t\tList<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();\n" +
      "\t\tHttpMethod httpMethod = oDataRequest.getMethod();\n\n" +
      "\t\tif (!httpMethod.name().equals(\"" + methodName + "\")) {\n" +
      "\t\t\tthrow new ODataApplicationException(\"The HTTP Method doesn't match with Raml defined method\");\n" +
      "\t\t}\n\n" +
      "\t\tupdateEntityData(httpMethod, requestEntity, edmEntitySet, keyPredicates);\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");";
    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateDeleteEntityMethod(JDefinedClass resourceInterface,
                                                Context context, Types types, String description,
                                                int statusCode) {
    JMethod deleteMethod = context.createResourceMethod(resourceInterface, DELETE_ENTITY,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(deleteMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(deleteMethod, ODataLibraryException.class);

    Map<String, Class<?>> params = new HashMap<String, Class<?>>();
    params.put("oDataRequest", ODataRequest.class);
    params.put("oDataResponse", ODataResponse.class);
    params.put("uriInfo", UriInfo.class);
    context.addParamsToResourceMethod(deleteMethod, params);

    context.addOverrideAnnotationToResourceMethod(deleteMethod);

    JMethod deleteEntityDataMethod = context.createResourceMethod(resourceInterface, "deleteEntityData",
      types.getGeneratorType(void.class), JMod.ABSTRACT);

    Map<String, Class<?>> deleteEntityDataParams = new HashMap<String, Class<?>>();
    deleteEntityDataParams.put("edmEntitySet", EdmEntitySet.class);

    Map<String, JType> deleteEntityDataParamsWithTypes = new HashMap<String, JType>();
    JClass listClass = context.getCodeModel().ref(List.class);
    JType listOfUriParamsType = listClass.narrow(UriParameter.class).unboxify();
    deleteEntityDataParamsWithTypes.put("keyPredicates", listOfUriParamsType);

    context.addParamsToResourceMethod(deleteEntityDataMethod, deleteEntityDataParams);
    context.addParamsToResourceMethod(deleteEntityDataMethod, deleteEntityDataParamsWithTypes, true);

    if (description != null) {
      deleteEntityDataMethod.javadoc().add(description);
    }

    generateDeleteEntityCode(deleteMethod, context, statusCode);
  }

  private static void generateDeleteEntityCode(JMethod method,
                                               Context context, int statusCode) {
    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tList<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();\n\n" +
      "\t\tdeleteEntityData(edmEntitySet, keyPredicates);\n\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");\n";

    context.addStmtToResourceMethodBody(method, code);

  }
}