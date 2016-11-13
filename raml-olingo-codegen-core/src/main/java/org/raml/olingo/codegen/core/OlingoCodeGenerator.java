package org.raml.olingo.codegen.core;

import com.sun.codemodel.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.apache.olingo.commons.api.edm.provider.*;
import org.apache.olingo.commons.api.ex.ODataException;
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
import org.apache.olingo.server.api.uri.queryoption.CustomQueryOption;
import org.apache.olingo.server.api.uri.queryoption.SystemQueryOption;
import org.raml.model.parameter.QueryParameter;

import java.util.*;
import java.util.logging.Logger;

public class OlingoCodeGenerator {

  private static final String INIT_METHOD = "init";
  private static final String READ_ENTITY_COLLECTION = "readEntityCollection";

  private static final String READ_ENTITY = "readEntity";
  private static final String CREATE_ENTITY = "createEntity";
  private static final String UPDATE_ENTITY = "updateEntity";
  private static final String DELETE_ENTITY = "deleteEntity";

  private static final List<String> systemTraits = Arrays.asList("orderby", "top", "skip", "filter",
    "expand", "select", "inlinecount", "apply", "search");

  private static final String[] standardEntityMethods = new String[]{"readEntity", "createEntity", "updateEntity", "deleteEntity"};
  private static final String[] standardEntityCollectionMethods = new String[]{"readEntityCollection"};

  public static String[] getStandardEntityMethods() {
    return standardEntityMethods;
  }

  public static String[] getStandardEntityCollectionMethods() {
    return standardEntityCollectionMethods;
  }

  public static void generateInitMethod(JDefinedClass resourceInterface, Context context,
                                        Types types) {
    resourceInterface.field(JMod.PROTECTED, OData.class, "oData");
    resourceInterface.field(JMod.PROTECTED, ServiceMetadata.class, "serviceMetadata");
    resourceInterface.field(JMod.PRIVATE, Logger.class, "LOG",
      JExpr.direct("Logger.getLogger(" + resourceInterface.name() + ".class.getName())"));

    JMethod initMethod = context.createResourceMethod(resourceInterface, INIT_METHOD,
      types.getGeneratorType(void.class), 0);
    context.addOverrideAnnotationToResourceMethod(initMethod);

    List<Pair<String, Class<?>>> initParams = new ArrayList<Pair<String, Class<?>>>();
    initParams.add(Pair.<String, Class<?>>of("oData", OData.class));
    initParams.add(Pair.<String, Class<?>>of("serviceMetadata", ServiceMetadata.class));
    context.addParamsToResourceMethod(initMethod, initParams);

    context.addStmtToResourceMethodBody(initMethod, "this.oData = oData;");
    context.addStmtToResourceMethodBody(initMethod, "this.serviceMetadata = serviceMetadata;");
  }

  public static void generateReadEntityCollectionProcessorMethod(JDefinedClass resourceInterface,
                                                             Context context, Types types, String description,
                                                                 int statusCode, List<Integer> statusCodes,
                                                                 Set<String> mimeTypes, boolean generateCode,
                                                                 boolean queryParamExists) {
    JMethod readMethod = context.createResourceMethod(resourceInterface, READ_ENTITY_COLLECTION,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(readMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(readMethod, ODataLibraryException.class);

    List<Pair<String, Class<?>>> params = new ArrayList<Pair<String, Class<?>>>();
    params.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    params.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    params.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    params.add(Pair.<String, Class<?>>of("contentType", ContentType.class));
    context.addParamsToResourceMethod(readMethod, params);

    context.addOverrideAnnotationToResourceMethod(readMethod);

    if (generateCode) {
      JMethod getDataMethod = context.createResourceMethod(resourceInterface, "getData",
        types.getGeneratorType(EntityCollection.class), JMod.ABSTRACT);

      List<Pair<String, Class<?>>> getDataParams = new ArrayList<Pair<String, Class<?>>>();
      getDataParams.add(Pair.<String, Class<?>>of("edmEntitySet", EdmEntitySet.class));
      getDataParams.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
      context.addParamsToResourceMethod(getDataMethod, getDataParams);

      if (queryParamExists) {
        JClass mapClass = context.getCodeModel().ref(Map.class);

        JType mapOfSystemQueryParamsType = mapClass.narrow(String.class).narrow(SystemQueryOption.class).unboxify();
        JType mapOfCustomQueryParamsType = mapClass.narrow(String.class).narrow(CustomQueryOption.class).unboxify();
        List<Pair<String, JType>> getDataParamsTypes = new ArrayList<Pair<String, JType>>();
        getDataParamsTypes.add(Pair.<String, JType>of("systemQueryParameters", mapOfSystemQueryParamsType));
        getDataParamsTypes.add(Pair.<String, JType>of("customQueryParameters", mapOfCustomQueryParamsType));
        context.addParamsToResourceMethod(getDataMethod, getDataParamsTypes, true);
      }
      context.addDefaultExceptionToResourceMethod(getDataMethod);

      if (description != null) {
        getDataMethod.javadoc().add(description);
      }

      generateReadEntityCollectionCode(readMethod, context, statusCode, statusCodes, mimeTypes, queryParamExists);
    }
  }

  private static void generateReadEntityCollectionCode(JMethod method, Context context, int statusCode,
                                                       List<Integer> statusCodes, Set<String> mimeTypes,
                                                       boolean queryParamExists) {
    if (statusCode == 0) {
      statusCode = HttpStatusCode.OK.getStatusCode();
    }

    int errorStatusCode = statusCodes.contains(HttpStatusCode.BAD_REQUEST.getStatusCode()) ?
      HttpStatusCode.BAD_REQUEST.getStatusCode() : HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    String[] mimeTypeArray = mimeTypes.toArray(new String[mimeTypes.size()]);
    String mimeTypeString = StringUtils.join(mimeTypeArray, ",");

    String callGetData = "\t\t\tentitySet = getData(edmEntitySet, uriInfo);\n";
    if (queryParamExists) {
      callGetData = "\t\t\tentitySet = getData(edmEntitySet, uriInfo, getSystemQueryParameters(uriInfo), getCustomQueryParameters(uriInfo));\n";
    }

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tEntityCollection entitySet;\n" +
      "\t\ttry {\n" +
       callGetData +
      "\t\t} catch(Exception e) {\n" +
      "\t\t\tthrow new ODataApplicationException(e.getMessage(), " + errorStatusCode + ", Locale.ENGLISH);\n" +
      "\t\t}\n\n" +
      "\t\tif (!\"" + mimeTypeString + "\".contains(contentType.toContentTypeString())) {\n" +
      "\t\t\tLOG.log(Level.SEVERE, \"The content-type is not supported\");\n" +
      "\t\t}\n\n" +
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

  public static void generateReadEntityMethod(JDefinedClass resourceInterface,
                                              Context context, Types types, String description,
                                              int statusCode, List<Integer> statusCodes,
                                              Set<String> mimeTypes, boolean generateCode,
                                              boolean queryParamExists) {
    JMethod readMethod = context.createResourceMethod(resourceInterface, READ_ENTITY,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(readMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(readMethod, ODataLibraryException.class);

    List<Pair<String, Class<?>>> params = new ArrayList<Pair<String, Class<?>>>();
    params.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    params.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    params.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    params.add(Pair.<String, Class<?>>of("contentType", ContentType.class));
    context.addParamsToResourceMethod(readMethod, params);

    context.addOverrideAnnotationToResourceMethod(readMethod);

    if (generateCode) {
      JMethod getDataMethod = context.createResourceMethod(resourceInterface, "getData",
        types.getGeneratorType(Entity.class), JMod.ABSTRACT);
      List<Pair<String, Class<?>>> getDataParams = new ArrayList<Pair<String, Class<?>>>();
      getDataParams.add(Pair.<String, Class<?>>of("edmEntitySet", EdmEntitySet.class));

      List<Pair<String, JType>> getDataParamsWithTypes = new ArrayList<Pair<String, JType>>();
      JClass listClass = context.getCodeModel().ref(List.class);
      JType listOfUriParamsType = listClass.narrow(UriParameter.class).unboxify();
      getDataParamsWithTypes.add(Pair.<String, JType>of("keyPredicates", listOfUriParamsType));

      context.addParamsToResourceMethod(getDataMethod, getDataParams);
      context.addParamsToResourceMethod(getDataMethod, getDataParamsWithTypes, true);

      if (queryParamExists) {
        JClass mapClass = context.getCodeModel().ref(Map.class);

        JType mapOfSystemQueryParamsType = mapClass.narrow(String.class).narrow(SystemQueryOption.class).unboxify();
        JType mapOfCustomQueryParamsType = mapClass.narrow(String.class).narrow(CustomQueryOption.class).unboxify();
        List<Pair<String, JType>> getDataParamsTypes = new ArrayList<Pair<String, JType>>();
        getDataParamsTypes.add(Pair.<String, JType>of("systemQueryParameters", mapOfSystemQueryParamsType));
        getDataParamsTypes.add(Pair.<String, JType>of("customQueryParameters", mapOfCustomQueryParamsType));
        context.addParamsToResourceMethod(getDataMethod, getDataParamsTypes, true);
      }

      context.addDefaultExceptionToResourceMethod(getDataMethod);

      if (description != null) {
        getDataMethod.javadoc().add(description);
      }

      generateReadEntityCode(readMethod, context, statusCode, statusCodes, mimeTypes, queryParamExists);
    }
  }

  private static void generateReadEntityCode(JMethod method, Context context, int statusCode,
                                                       List<Integer> statusCodes, Set<String> mimeTypes,
                                             boolean queryParamExists) {
    if (statusCode == 0) {
      statusCode = HttpStatusCode.OK.getStatusCode();
    }

    int errorStatusCode = statusCodes.contains(HttpStatusCode.BAD_REQUEST.getStatusCode()) ?
      HttpStatusCode.BAD_REQUEST.getStatusCode() : HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    String[] mimeTypeArray = mimeTypes.toArray(new String[mimeTypes.size()]);
    String mimeTypeString = StringUtils.join(mimeTypeArray, ",");

    String callGetData = "\t\t\tentity = getData(edmEntitySet, keyPredicates);\n";
    if (queryParamExists) {
      callGetData = "\t\t\tentity = getData(edmEntitySet, keyPredicates, getSystemQueryParameters(uriInfo), getCustomQueryParameters(uriInfo));\n";
    }

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tList<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();\n" +
      "\t\tEntity entity;\n" +
      "\t\ttry {\n" +
      callGetData +
      "\t\t} catch(Exception e) {\n" +
      "\t\t\tthrow new ODataApplicationException(e.getMessage(), " + errorStatusCode + ", Locale.ENGLISH);\n" +
      "\t\t}\n\n" +
      "\t\tif (!\"" + mimeTypeString + "\".contains(contentType.toContentTypeString())) {\n" +
      "\t\t\tLOG.log(Level.SEVERE, \"The content-type is not supported\");\n" +
      "\t\t}\n\n" +
      "\t\tODataSerializer serializer = oData.createSerializer(contentType);\n\n" +
      "\t\tEdmEntityType edmEntityType = edmEntitySet.getEntityType();\n" +
      "\t\tContextURL contextURL = ContextURL.with().entitySet(edmEntitySet).build();\n\n" +
      "\t\tEntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextURL).build();\n" +
      "\t\tSerializerResult serializerResult = serializer.entity(serviceMetadata, edmEntityType, entity, opts);\n" +
      "\t\tInputStream serializedContent = serializerResult.getContent();\n\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");\n" +
      "\t\toDataResponse.setContent(serializedContent);\n" +
      "\t\toDataResponse.setHeader(HttpHeader.CONTENT_TYPE, contentType.toContentTypeString());\n";
    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateCreateEntityMethod(JDefinedClass resourceInterface,
                                                Context context, Types types, String description,
                                                int statusCode, List<Integer> statusCodes,
                                                Set<String> reqMimeTypes, Set<String> respMimeTypes,
                                                boolean generateCode, boolean queryParamExists) {
    JMethod createMethod = context.createResourceMethod(resourceInterface, CREATE_ENTITY,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(createMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(createMethod, ODataLibraryException.class);

    List<Pair<String, Class<?>>> params = new ArrayList<Pair<String, Class<?>>>();
    params.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    params.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    params.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    params.add(Pair.<String, Class<?>>of("requestFormat", ContentType.class));
    params.add(Pair.<String, Class<?>>of("responseFormat", ContentType.class));
    context.addParamsToResourceMethod(createMethod, params);

    context.addOverrideAnnotationToResourceMethod(createMethod);

    if (generateCode) {
      JMethod createEntityDataMethod = context.createResourceMethod(resourceInterface, "createEntityData",
        types.getGeneratorType(Entity.class), JMod.ABSTRACT);

      List<Pair<String, Class<?>>> createEntityDataParams = new ArrayList<Pair<String, Class<?>>>();
      createEntityDataParams.add(Pair.<String, Class<?>>of("edmEntitySet", EdmEntitySet.class));
      createEntityDataParams.add(Pair.<String, Class<?>>of("requestEntity", Entity.class));
      context.addParamsToResourceMethod(createEntityDataMethod, createEntityDataParams);

      if (queryParamExists) {
        JClass mapClass = context.getCodeModel().ref(Map.class);

        JType mapOfSystemQueryParamsType = mapClass.narrow(String.class).narrow(SystemQueryOption.class).unboxify();
        JType mapOfCustomQueryParamsType = mapClass.narrow(String.class).narrow(CustomQueryOption.class).unboxify();
        List<Pair<String, JType>> createEntityDataParamsTypes = new ArrayList<Pair<String, JType>>();
        createEntityDataParamsTypes.add(Pair.<String, JType>of("systemQueryParameters", mapOfSystemQueryParamsType));
        createEntityDataParamsTypes.add(Pair.<String, JType>of("customQueryParameters", mapOfCustomQueryParamsType));
        context.addParamsToResourceMethod(createEntityDataMethod, createEntityDataParamsTypes, true);
      }

      context.addDefaultExceptionToResourceMethod(createEntityDataMethod);

      if (description != null) {
        createEntityDataMethod.javadoc().add(description);
      }

      generateCreateEntityCode(createMethod, context, statusCode, statusCodes, reqMimeTypes, respMimeTypes, queryParamExists);
    }
  }

  private static void generateCreateEntityCode(JMethod method, Context context, int statusCode,
                                               List<Integer> statusCodes, Set<String> reqMimeTypes,
                                               Set<String> respMimeTypes, boolean queryParamTypes) {
    if (statusCode == 0) {
      statusCode = HttpStatusCode.CREATED.getStatusCode();
    }

    int errorStatusCode = statusCodes.contains(HttpStatusCode.BAD_REQUEST.getStatusCode()) ?
      HttpStatusCode.BAD_REQUEST.getStatusCode() : HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    String[] reqMimeTypeArray = reqMimeTypes.toArray(new String[reqMimeTypes.size()]);
    String reqMimeTypeString = StringUtils.join(reqMimeTypeArray, ",");

    String[] respMimeTypeArray = respMimeTypes.toArray(new String[respMimeTypes.size()]);
    String respMimeTypeString = StringUtils.join(respMimeTypeArray, ",");

    String createEntityData = "\t\t\tcreatedEntity = createEntityData(edmEntitySet, requestEntity);\n";
    if (queryParamTypes) {
      createEntityData = "\t\t\tcreatedEntity = createEntityData(edmEntitySet, requestEntity, getSystemQueryParameters(uriInfo), getCustomQueryParameters(uriInfo));\n";
    }

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tEdmEntityType edmEntityType = edmEntitySet.getEntityType();\n\n" +
      "\t\tInputStream requestInputStream = oDataRequest.getBody();\n\n" +
      "\t\tif (!\"" + reqMimeTypeString + "\".contains(requestFormat.toContentTypeString())) {\n" +
      "\t\t\tLOG.log(Level.SEVERE, \"The request Format is not supported\");\n" +
      "\t\t}\n\n" +
      "\t\tODataDeserializer deserializer = oData.createDeserializer(requestFormat);\n" +
      "\t\tDeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);\n" +
      "\t\tEntity requestEntity = result.getEntity();\n\n" +
      "\t\tEntity createdEntity = null;\n" +
      "\t\ttry {\n" +
      createEntityData +
      "\t\t} catch(Exception e) {\n" +
      "\t\t\tthrow new ODataApplicationException(e.getMessage(), " + errorStatusCode + ", Locale.ENGLISH);\n" +
      "\t\t}\n\n" +
      "\t\tContextURL contextURL = ContextURL.with().entitySet(edmEntitySet).build();\n" +
      "\t\tEntitySerializerOptions opts = EntitySerializerOptions.with().contextURL(contextURL).build();\n\n" +
      "\t\tif (!\"" + respMimeTypeString + "\".contains(responseFormat.toContentTypeString())) {\n" +
      "\t\t\tLOG.log(Level.SEVERE, \"The response Format is not supported\");\n" +
      "\t\t}\n\n" +
      "\t\tODataSerializer serializer = oData.createSerializer(responseFormat);\n" +
      "\t\tSerializerResult serializedResponse = serializer.entity(serviceMetadata, edmEntityType, createdEntity, opts);\n\n" +
      "\t\toDataResponse.setContent(serializedResponse.getContent());\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");\n" +
      "\t\toDataResponse.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());\n";
    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateUpdateEntityMethod(JDefinedClass resourceInterface,
                                                Context context, Types types, String description,
                                                int statusCode, String methodName,
                                                List<Integer> statusCodes,
                                                Set<String> reqMimeTypes, Set<String> respMimeTypes,
                                                boolean generateCode, boolean queryParamExists) {
    JMethod updateMethod = context.createResourceMethod(resourceInterface, UPDATE_ENTITY,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(updateMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(updateMethod, ODataLibraryException.class);

    List<Pair<String, Class<?>>> params = new ArrayList<Pair<String, Class<?>>>();
    params.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    params.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    params.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    params.add(Pair.<String, Class<?>>of("requestFormat", ContentType.class));
    params.add(Pair.<String, Class<?>>of("responseFormat", ContentType.class));
    context.addParamsToResourceMethod(updateMethod, params);

    context.addOverrideAnnotationToResourceMethod(updateMethod);

    if (generateCode) {
      JMethod updateEntityDataMethod = context.createResourceMethod(resourceInterface, "updateEntityData",
        types.getGeneratorType(void.class), JMod.ABSTRACT);

      List<Pair<String, Class<?>>> updateEntityDataParams = new ArrayList<Pair<String, Class<?>>>();
      updateEntityDataParams.add(Pair.<String, Class<?>>of("edmEntitySet", EdmEntitySet.class));

      List<Pair<String, JType>> updateEntityDataParamsWithTypes = new ArrayList<Pair<String, JType>>();
      JClass listClass = context.getCodeModel().ref(List.class);
      JType listOfUriParamsType = listClass.narrow(UriParameter.class).unboxify();
      updateEntityDataParamsWithTypes.add(Pair.<String, JType>of("keyPredicates", listOfUriParamsType));

      updateEntityDataParams.add(Pair.<String, Class<?>>of("requestEntity", Entity.class));
      updateEntityDataParams.add(Pair.<String, Class<?>>of("httpMethod", HttpMethod.class));

      context.addParamsToResourceMethod(updateEntityDataMethod, updateEntityDataParams);
      context.addParamsToResourceMethod(updateEntityDataMethod, updateEntityDataParamsWithTypes, true);

      if (queryParamExists) {
        JClass mapClass = context.getCodeModel().ref(Map.class);

        JType mapOfSystemQueryParamsType = mapClass.narrow(String.class).narrow(SystemQueryOption.class).unboxify();
        JType mapOfCustomQueryParamsType = mapClass.narrow(String.class).narrow(CustomQueryOption.class).unboxify();
        List<Pair<String, JType>> updateEntityDataParamsTypes = new ArrayList<Pair<String, JType>>();
        updateEntityDataParamsTypes.add(Pair.<String, JType>of("systemQueryParameters", mapOfSystemQueryParamsType));
        updateEntityDataParamsTypes.add(Pair.<String, JType>of("customQueryParameters", mapOfCustomQueryParamsType));
        context.addParamsToResourceMethod(updateEntityDataMethod, updateEntityDataParamsTypes, true);
      }
      context.addDefaultExceptionToResourceMethod(updateEntityDataMethod);

      if (description != null) {
        updateEntityDataMethod.javadoc().add(description);
      }

      generateUpdateEntityCode(updateMethod, context, statusCode, methodName, statusCodes,
        reqMimeTypes, respMimeTypes, queryParamExists);
    }
  }

  private static void generateUpdateEntityCode(JMethod method,
                                               Context context, int statusCode, String methodName,
                                               List<Integer> statusCodes,
                                               Set<String> reqMimeTypes, Set<String> respMimeTypes,
                                               boolean queryParamExists) {
    if (statusCode == 0) {
      statusCode = HttpStatusCode.NO_CONTENT.getStatusCode();
    }

    int errorStatusCode = statusCodes.contains(HttpStatusCode.BAD_REQUEST.getStatusCode()) ?
      HttpStatusCode.BAD_REQUEST.getStatusCode() : HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    String[] reqMimeTypeArray = reqMimeTypes.toArray(new String[reqMimeTypes.size()]);
    String reqMimeTypeString = StringUtils.join(reqMimeTypeArray, ",");

    String[] respMimeTypeArray = respMimeTypes.toArray(new String[respMimeTypes.size()]);
    String respMimeTypeString = StringUtils.join(respMimeTypeArray, ",");

    String updateEntityData = "\t\t\tupdateEntityData(edmEntitySet, requestEntity, httpMethod, keyPredicates);\n";
    if (queryParamExists) {
      updateEntityData = "\t\t\tupdateEntityData(edmEntitySet, requestEntity, httpMethod, keyPredicates, getSystemQueryParameters(uriInfo), getCustomQueryParameters(uriInfo));\n";
    }

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n" +
      "\t\tEdmEntityType edmEntityType = edmEntitySet.getEntityType();\n\n" +
      "\t\tInputStream requestInputStream = oDataRequest.getBody();\n\n" +
      "\t\tif (!\"" + reqMimeTypeString + "\".contains(requestFormat.toContentTypeString())) {\n" +
      "\t\t\tLOG.log(Level.SEVERE, \"The request Format is not supported\");\n" +
      "\t\t}\n\n" +
      "\t\tODataDeserializer deserializer = oData.createDeserializer(requestFormat);\n" +
      "\t\tDeserializerResult result = deserializer.entity(requestInputStream, edmEntityType);\n" +
      "\t\tEntity requestEntity = result.getEntity();\n\n" +
      "\t\tList<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();\n" +
      "\t\tHttpMethod httpMethod = oDataRequest.getMethod();\n\n" +
      "\t\tif (!httpMethod.name().equals(\"" + methodName + "\")) {\n" +
      "\t\t\tthrow new ODataApplicationException(\"The HTTP Method doesn't match with Raml defined method\", " + errorStatusCode + ", Locale.ENGLISH);\n" +
      "\t\t}\n\n" +
      "\t\ttry {\n" +
      updateEntityData +
      "\t\t} catch(Exception e) {\n" +
      "\t\t\tthrow new ODataApplicationException(e.getMessage(), " + errorStatusCode + ", Locale.ENGLISH);\n" +
      "\t\t}\n\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");";
    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateDeleteEntityMethod(JDefinedClass resourceInterface,
                                                Context context, Types types, String description,
                                                int statusCode, List<Integer> statusCodes,
                                                boolean generateCode, boolean queryParamExists) {
    JMethod deleteMethod = context.createResourceMethod(resourceInterface, DELETE_ENTITY,
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(deleteMethod, ODataApplicationException.class);
    context.addExceptionToResourceMethod(deleteMethod, ODataLibraryException.class);

    List<Pair<String, Class<?>>> params = new ArrayList<Pair<String, Class<?>>>();
    params.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    params.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    params.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    context.addParamsToResourceMethod(deleteMethod, params);

    context.addOverrideAnnotationToResourceMethod(deleteMethod);

    if (generateCode) {
      JMethod deleteEntityDataMethod = context.createResourceMethod(resourceInterface, "deleteEntityData",
        types.getGeneratorType(void.class), JMod.ABSTRACT);

      List<Pair<String, Class<?>>> deleteEntityDataParams = new ArrayList<Pair<String, Class<?>>>();
      deleteEntityDataParams.add(Pair.<String, Class<?>>of("edmEntitySet", EdmEntitySet.class));

      List<Pair<String, JType>> deleteEntityDataParamsWithTypes = new ArrayList<Pair<String, JType>>();
      JClass listClass = context.getCodeModel().ref(List.class);
      JType listOfUriParamsType = listClass.narrow(UriParameter.class).unboxify();
      deleteEntityDataParamsWithTypes.add(Pair.of("keyPredicates", listOfUriParamsType));

      context.addParamsToResourceMethod(deleteEntityDataMethod, deleteEntityDataParams);
      context.addParamsToResourceMethod(deleteEntityDataMethod, deleteEntityDataParamsWithTypes, true);

      if (queryParamExists) {
        JClass mapClass = context.getCodeModel().ref(Map.class);

        JType mapOfSystemQueryParamsType = mapClass.narrow(String.class).narrow(SystemQueryOption.class).unboxify();
        JType mapOfCustomQueryParamsType = mapClass.narrow(String.class).narrow(CustomQueryOption.class).unboxify();
        List<Pair<String, JType>> deleteEntityDataParamsTypes = new ArrayList<Pair<String, JType>>();
        deleteEntityDataParamsTypes.add(Pair.<String, JType>of("systemQueryParameters", mapOfSystemQueryParamsType));
        deleteEntityDataParamsTypes.add(Pair.<String, JType>of("customQueryParameters", mapOfCustomQueryParamsType));
        context.addParamsToResourceMethod(deleteEntityDataMethod, deleteEntityDataParamsTypes, true);
      }
      context.addDefaultExceptionToResourceMethod(deleteEntityDataMethod);

      if (description != null) {
        deleteEntityDataMethod.javadoc().add(description);
      }

      generateDeleteEntityCode(deleteMethod, context, statusCode, statusCodes, queryParamExists);
    }
  }

  private static void generateDeleteEntityCode(JMethod method,
                                               Context context, int statusCode,
                                               List<Integer> statusCodes, boolean queryParamExists) {
    if (statusCode == 0) {
      statusCode = HttpStatusCode.NO_CONTENT.getStatusCode();
    }

    int errorStatusCode = statusCodes.contains(HttpStatusCode.BAD_REQUEST.getStatusCode()) ?
      HttpStatusCode.BAD_REQUEST.getStatusCode() : HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode();

    String deleteEntityData = "\t\t\tdeleteEntityData(edmEntitySet, keyPredicates);\n";
    if (queryParamExists) {
      deleteEntityData = "\t\t\tdeleteEntityData(edmEntitySet, keyPredicates, getSystemQueryParameters(uriInfo), getCustomQueryParameters(uriInfo));\n";
    }

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tList<UriParameter> keyPredicates = uriResourceEntitySet.getKeyPredicates();\n\n" +
      "\t\ttry {\n" +
      deleteEntityData +
      "\t\t} catch(Exception e) {\n" +
      "\t\t\tthrow new ODataApplicationException(e.getMessage(), " + errorStatusCode + ", Locale.ENGLISH);\n" +
      "\t\t}\n\n" +
      "\t\toDataResponse.setStatusCode(" + statusCode + ");\n";

    context.addStmtToResourceMethodBody(method, code);

  }

  public static void generateGetEntityType(JDefinedClass resourceInterface, Context context,
                                           Types types, Map<String, JClass> schemas) {
    JMethod getEntityType = context.createResourceMethod(resourceInterface, "getEntityType",
      types.getGeneratorType(CsdlEntityType.class), 0);

    List<Pair<String, Class<?>>> getEntityTypeParams = new ArrayList<Pair<String, Class<?>>>();
    getEntityTypeParams.add(Pair.<String, Class<?>>of("entityTypeName", FullQualifiedName.class));
    context.addParamsToResourceMethod(getEntityType, getEntityTypeParams);

    context.addExceptionToResourceMethod(getEntityType, ODataException.class);
    context.addOverrideAnnotationToResourceMethod(getEntityType);

    generateGetEntityTypeCode(resourceInterface, getEntityType, context, schemas);
  }

  private static void generateGetEntityTypeCode(JDefinedClass resourceInterface, JMethod method, Context context,
                                                Map<String, JClass> schemas) {
    Map<String, Pair<String, String>> metadataEntities = context.getMetadataEntities();

    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      JClass listClass = context.getCodeModel().ref(List.class);
      JType listOfCsdlPropertyType = listClass.narrow(CsdlProperty.class).unboxify();

      // metadataEntity.getKey() has a known format ET_<entity>_NAME
      String entityType = WordUtils.capitalize(metadataEntity.getKey().split("_")[1].toLowerCase());
      JClass schema = schemas.get(entityType);

      if (schema != null) {
        JMethod propsMethod = context.createResourceMethod(resourceInterface,
          "get".concat(entityType).concat("Properties"),
          listOfCsdlPropertyType, 0);
        generateProperties(propsMethod, schema, context);

        JType listOfCsdlPropertyRefType = listClass.narrow(CsdlPropertyRef.class).unboxify();

        JMethod keyMethod = context.createResourceMethod(resourceInterface,
          "get".concat(entityType).concat("Keys"),
          listOfCsdlPropertyRefType, 0);
        generateKeys(keyMethod, schema, context);
      } else {
        context.createResourceMethod(resourceInterface,
          "get".concat(entityType).concat("Properties"),
          listOfCsdlPropertyType, JMod.ABSTRACT);

        JType listOfCsdlPropertyRefType = listClass.narrow(CsdlPropertyRef.class).unboxify();

        context.createResourceMethod(resourceInterface,
          "get".concat(entityType).concat("Keys"),
          listOfCsdlPropertyRefType, JMod.ABSTRACT);
      }

    }

    String codeTemplate = "\n\t\tif (entityTypeName.equals(<et_fqn>)) {\n" +
      "\t\t\tCsdlEntityType entityType = new CsdlEntityType();\n" +
      "\t\t\tentityType.setName(<et_name>);\n" +
      "\t\t\tentityType.setProperties(get<et_actual_name>Properties());\n" +
      "\t\t\tentityType.setKey(get<et_actual_name>Keys());\n\n" +
      "\t\t\treturn entityType;\n" +
      "\t\t}\n";

    String end = "\t\treturn null;";

    String code = "";
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      // metadataEntity.getKey() has a known format ET_<entity>_NAME
      String entityType = WordUtils.capitalize(metadataEntity.getKey().split("_")[1].toLowerCase());

      code = code + codeTemplate.replaceAll("<et_name>",
        metadataEntity.getKey())
      .replaceAll("<et_fqn>", metadataEntity.getValue().getLeft())
      .replaceAll("<et_actual_name>", entityType);
    }
    code = code + end;

    context.addStmtToResourceMethodBody(method, code);
  }

  private static void generateProperties(JMethod method, JClass schema, Context context) {
    JDefinedClass schemaClass = (JDefinedClass) schema;
    Map<String, JFieldVar> fields = schemaClass.fields();

    String codeTemplate = "\t\tCsdlProperty <field> = new CsdlProperty().setName(\"<field>\").setType(<type>);\n";
    String code = "\n";

    String fieldStr = "";
    for (Map.Entry<String, JFieldVar> field: fields.entrySet()) {
      if (!field.getKey().equals("additionalProperties") &&
        !field.getKey().equals("NOT_FOUND_VALUE")) {
        code = code + codeTemplate.replaceAll("<field>", field.getKey())
        .replaceAll("<type>", getTypeFromJType(context, field.getValue().type()));
        fieldStr = fieldStr + field.getKey() + ", ";
      }
    }

    code = code + "\n\t\treturn Arrays.asList(" + fieldStr.substring(0, fieldStr.length() - 2) + ");\n";
    context.addStmtToResourceMethodBody(method, code);
  }

  private static void generateKeys(JMethod method, JClass schema, Context context) {
    JDefinedClass schemaClass = (JDefinedClass) schema;
    Map<String, JFieldVar> fields = schemaClass.fields();

    String codeTemplate = "\t\tCsdlPropertyRef <field> = new CsdlPropertyRef();\n" +
      "\t\t<field>.setName(\"<field>\");\n\n";

    String code = "\n";
    String fieldStr = "";
    for (Map.Entry<String, JFieldVar> field: fields.entrySet()) {
      if (!field.getKey().equals("additionalProperties") &&
        !field.getKey().equals("NOT_FOUND_VALUE")) {
        if (field.getValue().javadoc().toString().contains("Required")) {
          code = code + codeTemplate.replaceAll("<field>", field.getKey());
          fieldStr = fieldStr + field.getKey() + ", ";
        }
      }
    }
    code = code + "\n\t\treturn Arrays.asList(" + fieldStr.substring(0, fieldStr.length() - 2) + ");\n";

    context.addStmtToResourceMethodBody(method, code);
  }

  private static String getTypeFromJType(Context context, JType type) {
    JType edmPrimitiveTypeKind = context.getCodeModel()._ref(EdmPrimitiveTypeKind.class);
    switch (type.name()) {
      case "String":
        return edmPrimitiveTypeKind.name() + ".String.getFullQualifiedName()";
      case "Integer":
        return edmPrimitiveTypeKind.name() + ".Int32.getFullQualifiedName()";
      case "Byte":
        return edmPrimitiveTypeKind.name() + ".Byte.getFullQualifiedName()";
      case "Boolean":
        return edmPrimitiveTypeKind.name() + ".Boolean.getFullQualifiedName()";
      case "Short":
        return edmPrimitiveTypeKind.name() + ".Int16.getFullQualifiedName()";
      case "Float":
        return edmPrimitiveTypeKind.name() + ".Single.getFullQualifiedName()";
      case "Long":
        return edmPrimitiveTypeKind.name() + ".Int64.getFullQualifiedName()";
      case "Double":
        return edmPrimitiveTypeKind.name() + ".Double.getFullQualifiedName()";
      case "Number":
        return edmPrimitiveTypeKind.name() + ".Decimal.getFullQualifiedName()";
      default: return null;
    }
  }

  public static void generateGetEntitySet(JDefinedClass resourceInterface, Context context,
                                          Types types) {
    JMethod getEntitySet = context.createResourceMethod(resourceInterface, "getEntitySet",
      types.getGeneratorType(CsdlEntitySet.class), 0);

    List<Pair<String, Class<?>>> getEntitySetParams = new ArrayList<Pair<String, Class<?>>>();
    getEntitySetParams.add(Pair.<String, Class<?>>of("entityContainer", FullQualifiedName.class));
    getEntitySetParams.add(Pair.<String, Class<?>>of("entitySetName", String.class));
    context.addParamsToResourceMethod(getEntitySet, getEntitySetParams);

    context.addExceptionToResourceMethod(getEntitySet, ODataException.class);
    context.addOverrideAnnotationToResourceMethod(getEntitySet);

    generateGetEntitySetCode(getEntitySet, context);
  }

  private static void generateGetEntitySetCode(JMethod method, Context context) {
    String beginner = "\n\t\tif (entityContainer.equals(CONTAINER)) {\n";
    String codeTemplate =  "\t\t\tif (entitySetName.equals(<es_name>)) {\n" +
      "\t\t\t\tCsdlEntitySet entitySet = new CsdlEntitySet();\n" +
      "\t\t\t\tentitySet.setName(<es_name>);\n" +
      "\t\t\t\tentitySet.setType(<et_fqn>);\n\n" +
      "\t\t\t\treturn entitySet;\n" +
      "\t\t\t}\n";

    String end = "\t\t}\n\n" +
      "\t\treturn null;\n";

    String code = beginner;
    Map<String, Pair<String, String>> metadataEntities = context.getMetadataEntities();
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      code = code + codeTemplate.replaceAll("<es_name>", metadataEntity.getValue().getRight())
        .replaceAll("<et_fqn>", metadataEntity.getValue().getLeft());
    }
    code = code + end;
    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateGetEntityContainer(JDefinedClass resourceInterface, Context context,
                                                Types types) {
    JMethod getEntityContainer = context.createResourceMethod(resourceInterface, "getEntityContainer",
      types.getGeneratorType(CsdlEntityContainer.class), 0);

    context.addExceptionToResourceMethod(getEntityContainer, ODataException.class);
    context.addOverrideAnnotationToResourceMethod(getEntityContainer);

    generateGetEntityContainerCode(getEntityContainer, context);
  }

  private static void generateGetEntityContainerCode(JMethod method, Context context) {
    String beginner = "\n\t\tList<CsdlEntitySet> entitySets = new ArrayList<>();\n";
    String codeTemplate = "\t\tentitySets.add(getEntitySet(CONTAINER, <es_name>));\n\n";
    String end = "\t\tCsdlEntityContainer entityContainer = new CsdlEntityContainer();\n" +
      "\t\tentityContainer.setName(CONTAINER_NAME);\n" +
      "\t\tentityContainer.setEntitySets(entitySets);\n\n" +
      "\t\treturn entityContainer;\n";

    String code = beginner;
    Map<String, Pair<String, String>> metadataEntities = context.getMetadataEntities();
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      code = code + codeTemplate.replaceAll("<es_name>", metadataEntity.getValue().getRight());
    }
    code = code + end;

    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateGetSchemas(JDefinedClass resourceInterface, Context context,
                                        Types types) {
    JClass listClass = context.getCodeModel().ref(List.class);
    JType listOfCsdlSchemaType = listClass.narrow(CsdlSchema.class).unboxify();

    JMethod getSchemas = context.createResourceMethod(resourceInterface, "getSchemas",
      listOfCsdlSchemaType, 0);

    context.addExceptionToResourceMethod(getSchemas, ODataException.class);
    context.addOverrideAnnotationToResourceMethod(getSchemas);

    generateGetSchemasCode(getSchemas, context);
  }

  private static void generateGetSchemasCode(JMethod method, Context context) {
    String beginner = "\n\t\tCsdlSchema schema = new CsdlSchema();\n" +
      "\t\tschema.setNamespace(NAMESPACE);\n\n" +
      "\t\tList<CsdlEntityType> entityTypes = new ArrayList<>();\n";

    String codeTemplate = "\t\tentityTypes.add(getEntityType(<et_fqn>));\n";

    String end = "\t\tschema.setEntityTypes(entityTypes);\n\n" +
      "\t\tschema.setEntityContainer(getEntityContainer());\n\n" +
      "\t\tList<CsdlSchema> schemas = new ArrayList<>();\n" +
      "\t\tschemas.add(schema);\n\n" +
      "\t\treturn schemas;\n";

    String code = beginner;
    Map<String, Pair<String, String>> metadataEntities = context.getMetadataEntities();
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      code = code + codeTemplate.replaceAll("<et_fqn>", metadataEntity.getValue().getLeft());
    }
    code = code + end;

    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateGetEntityContainerInfo(JDefinedClass resourceInterface, Context context,
                                                    Types types) {
    JMethod getEntityContainerInfo = context.createResourceMethod(resourceInterface, "getEntityContainerInfo",
      types.getGeneratorType(CsdlEntityContainerInfo.class), 0);

    List<Pair<String, Class<?>>> getEntityContainerInfoParams = new ArrayList<Pair<String, Class<?>>>();
    getEntityContainerInfoParams.add(Pair.<String, Class<?>>of("entityContainerName", FullQualifiedName.class));
    context.addParamsToResourceMethod(getEntityContainerInfo, getEntityContainerInfoParams);

    context.addExceptionToResourceMethod(getEntityContainerInfo, ODataException.class);
    context.addOverrideAnnotationToResourceMethod(getEntityContainerInfo);

    generateGetEntityContainerInfoCode(getEntityContainerInfo, context);
  }

  private static void generateGetEntityContainerInfoCode(JMethod method, Context context) {
    String code = "\n\t\tif (entityContainerName == null || entityContainerName.equals(CONTAINER)) {\n" +
      "\t\t\tCsdlEntityContainerInfo entityContainerInfo = new CsdlEntityContainerInfo();\n" +
      "\t\t\tentityContainerInfo.setContainerName(CONTAINER);\n" +
      "\t\t\treturn entityContainerInfo;\n" +
      "\t\t}\n" +
      "\t\treturn null;";

    context.addStmtToResourceMethodBody(method, code);
  }

  public static void generateGetQueryParameters(JDefinedClass resourceInterface, Context context,
                                                Map<String, QueryParameter> queryParameters) {
    List<String> systemQueryOptions = new ArrayList<String>();
    List<String> customQueryOptions = new ArrayList<String>();

    for (Map.Entry<String, QueryParameter> queryParameter: queryParameters.entrySet()) {
      if (systemTraits.contains(queryParameter.getKey())) {
        systemQueryOptions.add("$".concat(queryParameter.getKey()));
      } else {
        customQueryOptions.add(queryParameter.getKey());
      }
    }

    String systemQueryCode = "\n\t\tMap<String, SystemQueryOption> systemQueryParams = new HashMap<String, SystemQueryOption>();\n\n";

    for (String queryOption: systemQueryOptions) {
      switch (queryOption) {
        case "$orderby": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getOrderByOption());\n";
          break;
        case "$top": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getTopOption());\n";
          break;
        case "$skip": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getSkipOption());\n";
          break;
        case "$filter": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getFilterOption());\n";
          break;
        case "$expand": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getExpandOption());\n";
          break;
        case "$select": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getSelectOption());\n";
          break;
        case "$inlinecount": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getCountOption());\n";
          break;
        case "$apply": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getApplyOption());\n";
          break;
        case "$search": systemQueryCode = systemQueryCode + "\t\tsystemQueryParams.put(\"" + queryOption +
          "\", uriInfo.getSearchOption());\n";
          break;
      }
    }
    systemQueryCode = systemQueryCode + "\n\t\treturn systemQueryParams;";

    JClass mapClass = context.getCodeModel().ref(Map.class);

    JType mapOfSystemQueryParamsType = mapClass.narrow(String.class).narrow(SystemQueryOption.class).unboxify();
    JMethod systemQueryParamsMethod = context.createResourceMethod(resourceInterface, "getSystemQueryParameters",
      mapOfSystemQueryParamsType, 0);
    List<Pair<String, Class<?>>> params = new ArrayList<Pair<String, Class<?>>>();
    params.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));

    context.addParamsToResourceMethod(systemQueryParamsMethod, params);
    context.addStmtToResourceMethodBody(systemQueryParamsMethod, systemQueryCode);

    String customQueryCode = "\n\t\tMap<String, CustomQueryOption> customQueryParams = new HashMap<String, CustomQueryOption>();\n\n" +
      "\t\tList<CustomQueryOption> customQueryOptions = uriInfo.getCustomQueryOptions();\n\n";

    for (String queryOption: customQueryOptions) {
      customQueryCode = customQueryCode + "\t\tif (customQueryOptions.indexOf(\"" + queryOption + "\") != -1) {\n";
      customQueryCode = customQueryCode + "\t\t\tcustomQueryParams.put(\"" + queryOption + "\"," +
        " customQueryOptions.get(customQueryOptions.indexOf(\"" + queryOption + "\")));\n";
      customQueryCode = customQueryCode + "\t\t}\n";
    }
    customQueryCode = customQueryCode + "\n\t\treturn customQueryParams;";

    JType mapOfCustomQueryParamsType = mapClass.narrow(String.class).narrow(CustomQueryOption.class).unboxify();
    JMethod customQueryParamsMethod = context.createResourceMethod(resourceInterface, "getCustomQueryParameters",
      mapOfCustomQueryParamsType, 0);
    context.addParamsToResourceMethod(customQueryParamsMethod, params);
    context.addStmtToResourceMethodBody(customQueryParamsMethod, customQueryCode);
  }
}