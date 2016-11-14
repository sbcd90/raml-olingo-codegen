package org.raml.olingo.codegen.core;

import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.olingo.commons.api.edm.provider.CsdlAbstractEdmProvider;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.uri.UriInfo;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;
import org.raml.model.parameter.QueryParameter;
import org.raml.olingo.codegen.core.utils.ResponseWrapper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

public class Generator extends AbstractGenerator {

  @Override
  protected void addResourceMethod(JDefinedClass resourceInterface, Resource resource,
                                   Action action, MimeType bodyMimeType,
                                   boolean addBodyMimeTypeInMethodName,
                                   Collection<MimeType> uniqueResponseMimeTypes) throws Exception {

    String httpMethod = action.getType().name();
    String description = action.getDescription();

    /**
     * get Query parameters
     */
    boolean queryParamExists = false;
    Map<String, QueryParameter> queryParameters = action.getQueryParameters();
    if (queryParameters.entrySet().size() > 0) {

      boolean methodExists = false;
      Collection<JMethod> methods = resourceInterface.methods();
      for (JMethod method: methods) {
        if (method.name().equals("getSystemQueryParameters")) {
          methodExists = true;
        }
        if (method.name().equals("getCustomQueryParameters")) {
          methodExists = true;
        }
      }

      if (!methodExists) {
        OlingoCodeGenerator.generateGetQueryParameters(resourceInterface, context, queryParameters);
      }
      queryParamExists = true;
    }

    /**
     * consider request body
     */
    Set<String> reqMimeTypes = new HashSet<String>();
    if (action.hasBody()) {
      Map<String, MimeType> body = action.getBody();
      for (Map.Entry<String, MimeType> mimeType: body.entrySet()) {
        reqMimeTypes.add(mimeType.getKey());

        if (mimeType.getValue().getSchema() != null) {
          schemas.put(Names.buildJavaFriendlyName(
            StringUtils.defaultIfBlank(resource.getDisplayName(), resource.getRelativeUri())),
            types.getSchemaClass(mimeType.getValue()));
        }
      }
    }

    /**
     * consider response status code & body
     */
    int statusCode = 0;
    Map<String, Response> responses = action.getResponses();
    ResponseWrapper responseWrapper = new ResponseWrapper(Names.buildJavaFriendlyName(
      StringUtils.defaultIfBlank(resource.getDisplayName(), resource.getRelativeUri())),
      responses);
    responseWrapper.parseResponses(types, schemas);

    Set<String> mimeTypes = responseWrapper.getMimeTypes();
    List<Integer> statusCodes = responseWrapper.getStatusCodes();


    for (Map.Entry<String, Response> response: responses.entrySet()) {
      statusCode = NumberUtils.toInt(response.getKey());
    }

    if (httpMethod.equals("GET")) {
      if (resourceInterface.name().contains(context.getConfiguration().getEntityCollectionInterfaceNameSuffix())) {
        OlingoCodeGenerator.generateReadEntityCollectionProcessorMethod(resourceInterface, context,
          types, description, statusCode, statusCodes, mimeTypes, true, queryParamExists);
      } else {
        OlingoCodeGenerator.generateReadEntityMethod(resourceInterface, context, types, description,
          statusCode, statusCodes, mimeTypes, true, queryParamExists);
      }

    } else if (httpMethod.equals("POST")) {
      if (statusCode == 0) {
        statusCode = HttpStatusCode.CREATED.getStatusCode();
      }

      OlingoCodeGenerator.generateCreateEntityMethod(resourceInterface, context,
        types, description, statusCode, statusCodes, reqMimeTypes, mimeTypes, true, queryParamExists);
    } else if (httpMethod.equals("PUT") || httpMethod.equals("POST")) {
      if (statusCode == 0) {
        statusCode = HttpStatusCode.NO_CONTENT.getStatusCode();
      }

      OlingoCodeGenerator.generateUpdateEntityMethod(resourceInterface, context,
        types, description, statusCode, action.getType().name(), statusCodes, reqMimeTypes, mimeTypes, true, queryParamExists);
    } else if (httpMethod.equals("DELETE")) {
      if (statusCode == 0) {
        statusCode = HttpStatusCode.NO_CONTENT.getStatusCode();
      }

      OlingoCodeGenerator.generateDeleteEntityMethod(resourceInterface, context,
        types, description, statusCode, statusCodes, true, queryParamExists);
    }
  }

  @Override
  protected void createMetadataInterface(final Context context, final List<String> entityTypes) throws Exception {
    JDefinedClass edmProviderClass = context.createResourceInterface("AbstractEdmProvider", CsdlAbstractEdmProvider.class,
      context.getMetadataPackage(), entityTypes);

    OlingoCodeGenerator.generateGetEntityType(edmProviderClass, context, types, schemas);
    OlingoCodeGenerator.generateGetEntitySet(edmProviderClass, context, types);
    OlingoCodeGenerator.generateGetEntityContainer(edmProviderClass, context, types);
    OlingoCodeGenerator.generateGetSchemas(edmProviderClass, context, types);
    OlingoCodeGenerator.generateGetEntityContainerInfo(edmProviderClass, context, types);
  }

  @Override
  protected void createCommonEntityCollection(Context context, List<String> entityTypes) throws Exception {
    JDefinedClass commonEntityCollection =
      context.createResourceInterface("CommonEntityCollectionProcessor", EntityCollectionProcessor.class, JMod.PUBLIC);
    OlingoCodeGenerator.generateInitMethod(commonEntityCollection, context, types);

    JMethod readEntityCollection = context.createResourceMethod(commonEntityCollection, "readEntityCollection",
      types.getGeneratorType(void.class), 0);
    List<Pair<String, Class<?>>> params = new ArrayList<Pair<String, Class<?>>>();
    params.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    params.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    params.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    params.add(Pair.<String, Class<?>>of("contentType", ContentType.class));
    context.addParamsToResourceMethod(readEntityCollection, params);
    context.addExceptionToResourceMethod(readEntityCollection, ODataApplicationException.class);
    context.addExceptionToResourceMethod(readEntityCollection, ODataLibraryException.class);

    String code = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tswitch (edmEntitySet.getName()) {\n";

    int count = 0;
    Map<String, Pair<String, String>> metadataEntities = context.getMetadataEntities();
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      code = code + "\t\t\tcase " + metadataEntity.getValue().getRight() + " : \n" +
        "\t\t\t\t" + WordUtils.capitalize(entityTypes.get(count))
        .concat(context.getConfiguration().getEntityCollectionInterfaceNameSuffix()) + " " +
        entityTypes.get(count).concat(context.getConfiguration().getEntityCollectionInterfaceNameSuffix()) + " = new " +
        WordUtils.capitalize(entityTypes.get(count))
          .concat(context.getConfiguration().getEntityCollectionInterfaceNameSuffix()).concat("();\n") +
        "\t\t\t\t" + entityTypes.get(count)
        .concat(context.getConfiguration().getEntityCollectionInterfaceNameSuffix()) + ".init(oData, serviceMetadata);\n" +
        "\t\t\t\t" + entityTypes.get(count).concat(context.getConfiguration().getEntityCollectionInterfaceNameSuffix()) +
        ".readEntityCollection(oDataRequest, oDataResponse, uriInfo, contentType);\n" +
        "\t\t\t\tbreak;\n";
      count++;
    }
    code = code + "\t\t}\n";
    context.addStmtToResourceMethodBody(readEntityCollection, code);
    context.addOverrideAnnotationToResourceMethod(readEntityCollection);
  }

  @Override
  protected void createCommonEntity(Context context, List<String> entityTypes) throws Exception {
    JDefinedClass commonEntity =
      context.createResourceInterface("CommonEntityProcessor", EntityProcessor.class, JMod.PUBLIC);
    OlingoCodeGenerator.generateInitMethod(commonEntity, context, types);

    JMethod readEntity = context.createResourceMethod(commonEntity, "readEntity",
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(readEntity, ODataApplicationException.class);
    context.addExceptionToResourceMethod(readEntity, ODataLibraryException.class);

    List<Pair<String, Class<?>>> readParams = new ArrayList<Pair<String, Class<?>>>();
    readParams.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    readParams.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    readParams.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    readParams.add(Pair.<String, Class<?>>of("contentType", ContentType.class));
    context.addParamsToResourceMethod(readEntity, readParams);

    String commonCode = "\n\t\tList<UriResource> resourcePaths = uriInfo.getUriResourceParts();\n" +
      "\t\tUriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourcePaths.get(0);\n" +
      "\t\tEdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();\n\n" +
      "\t\tswitch (edmEntitySet.getName()) {\n";

    int readCount = 0;
    Map<String, Pair<String, String>> metadataEntities = context.getMetadataEntities();
    String readCode = commonCode;
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      readCode = readCode + "\t\t\tcase " + metadataEntity.getValue().getRight() + " : \n" +
        "\t\t\t\t" + WordUtils.capitalize(entityTypes.get(readCount))
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " " +
        entityTypes.get(readCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " = new " +
        WordUtils.capitalize(entityTypes.get(readCount))
          .concat(context.getConfiguration().getEntityInterfaceNameSuffix()).concat("();\n") +
        "\t\t\t\t" + entityTypes.get(readCount)
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + ".init(oData, serviceMetadata);\n" +
        "\t\t\t\t" + entityTypes.get(readCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) +
        ".readEntity(oDataRequest, oDataResponse, uriInfo, contentType);\n" +
        "\t\t\t\tbreak;\n";
      readCount++;
    }
    readCode = readCode + "\t\t}\n";
    context.addStmtToResourceMethodBody(readEntity, readCode);
    context.addOverrideAnnotationToResourceMethod(readEntity);

    JMethod createEntity = context.createResourceMethod(commonEntity, "createEntity",
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(createEntity, ODataApplicationException.class);
    context.addExceptionToResourceMethod(createEntity, ODataLibraryException.class);

    List<Pair<String, Class<?>>> createParams = new ArrayList<Pair<String, Class<?>>>();
    createParams.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    createParams.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    createParams.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    createParams.add(Pair.<String, Class<?>>of("requestFormat", ContentType.class));
    createParams.add(Pair.<String, Class<?>>of("responseFormat", ContentType.class));
    context.addParamsToResourceMethod(createEntity, createParams);

    int createCount = 0;
    String createCode = commonCode;
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      createCode = createCode + "\t\t\tcase " + metadataEntity.getValue().getRight() + " : \n" +
        "\t\t\t\t" + WordUtils.capitalize(entityTypes.get(createCount))
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " " +
        entityTypes.get(createCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " = new " +
        WordUtils.capitalize(entityTypes.get(createCount))
          .concat(context.getConfiguration().getEntityInterfaceNameSuffix()).concat("();\n") +
        "\t\t\t\t" + entityTypes.get(createCount)
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + ".init(oData, serviceMetadata);\n" +
        "\t\t\t\t" + entityTypes.get(createCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) +
        ".createEntity(oDataRequest, oDataResponse, uriInfo, requestFormat, responseFormat);\n" +
        "\t\t\t\tbreak;\n";
      createCount++;
    }
    createCode = createCode + "\t\t}\n";
    context.addStmtToResourceMethodBody(createEntity, createCode);
    context.addOverrideAnnotationToResourceMethod(createEntity);

    JMethod updateEntity = context.createResourceMethod(commonEntity, "updateEntity",
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(updateEntity, ODataApplicationException.class);
    context.addExceptionToResourceMethod(updateEntity, ODataLibraryException.class);

    List<Pair<String, Class<?>>> updateParams = new ArrayList<Pair<String, Class<?>>>();
    updateParams.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    updateParams.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    updateParams.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    updateParams.add(Pair.<String, Class<?>>of("requestFormat", ContentType.class));
    updateParams.add(Pair.<String, Class<?>>of("responseFormat", ContentType.class));
    context.addParamsToResourceMethod(updateEntity, updateParams);

    int updateCount = 0;
    String updateCode = commonCode;
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      updateCode = updateCode + "\t\t\tcase " + metadataEntity.getValue().getRight() + " : \n" +
        "\t\t\t\t" + WordUtils.capitalize(entityTypes.get(updateCount))
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " " +
        entityTypes.get(updateCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " = new " +
        WordUtils.capitalize(entityTypes.get(updateCount))
          .concat(context.getConfiguration().getEntityInterfaceNameSuffix()).concat("();\n") +
        "\t\t\t\t" + entityTypes.get(updateCount)
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + ".init(oData, serviceMetadata);\n" +
        "\t\t\t\t" + entityTypes.get(updateCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) +
        ".updateEntity(oDataRequest, oDataResponse, uriInfo, requestFormat, responseFormat);\n" +
        "\t\t\t\tbreak;\n";
      updateCount++;
    }
    updateCode = updateCode + "\t\t}\n";
    context.addStmtToResourceMethodBody(updateEntity, updateCode);
    context.addOverrideAnnotationToResourceMethod(updateEntity);

    JMethod deleteEntity = context.createResourceMethod(commonEntity, "deleteEntity",
      types.getGeneratorType(void.class), 0);
    context.addExceptionToResourceMethod(deleteEntity, ODataApplicationException.class);
    context.addExceptionToResourceMethod(deleteEntity, ODataLibraryException.class);

    List<Pair<String, Class<?>>> deleteParams = new ArrayList<Pair<String, Class<?>>>();
    deleteParams.add(Pair.<String, Class<?>>of("oDataRequest", ODataRequest.class));
    deleteParams.add(Pair.<String, Class<?>>of("oDataResponse", ODataResponse.class));
    deleteParams.add(Pair.<String, Class<?>>of("uriInfo", UriInfo.class));
    context.addParamsToResourceMethod(deleteEntity, deleteParams);

    int deleteCount = 0;
    String deleteCode = commonCode;
    for (Map.Entry<String, Pair<String, String>> metadataEntity: metadataEntities.entrySet()) {
      deleteCode = deleteCode + "\t\t\tcase " + metadataEntity.getValue().getRight() + " : \n" +
        "\t\t\t\t" + WordUtils.capitalize(entityTypes.get(deleteCount))
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " " +
        entityTypes.get(deleteCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + " = new " +
        WordUtils.capitalize(entityTypes.get(deleteCount))
          .concat(context.getConfiguration().getEntityInterfaceNameSuffix()).concat("();\n") +
        "\t\t\t\t" + entityTypes.get(deleteCount)
        .concat(context.getConfiguration().getEntityInterfaceNameSuffix()) + ".init(oData, serviceMetadata);\n" +
        "\t\t\t\t" + entityTypes.get(deleteCount).concat(context.getConfiguration().getEntityInterfaceNameSuffix()) +
        ".deleteEntity(oDataRequest, oDataResponse, uriInfo);\n" +
        "\t\t\t\tbreak;\n";
      deleteCount++;
    }
    deleteCode = deleteCode + "\t\t}\n";
    context.addStmtToResourceMethodBody(deleteEntity, deleteCode);
    context.addOverrideAnnotationToResourceMethod(deleteEntity);
  }
}