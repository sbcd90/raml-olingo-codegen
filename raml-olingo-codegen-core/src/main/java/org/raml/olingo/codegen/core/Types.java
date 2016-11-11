package org.raml.olingo.codegen.core;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Resource;
import org.raml.model.Response;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.TEXT_XML;

public class Types {
  private final Context context;
  private final Map<String, JClass> schemaClasses;

  public Types(final Context context) {
    Validate.notNull(context, "context can't be null");

    this.context = context;
    this.schemaClasses = new HashMap<String, JClass>();
  }

  public void generateClassesFromXmlSchemas(Collection<Resource> resources) {
    if (resources == null) {
      return;
    }
    HashMap<String, File> schemaFiles = new HashMap<String, File>();
    for (Resource r: resources) {
      collectXmlSchemaFiles(r, schemaFiles);
    }
    schemaClasses.putAll(context.generateClassesFromXmlSchemas(schemaFiles));
  }

  private boolean isCompatibleWith(final MimeType mt, final String... mediaTypes) {
    final String mimeType = mt.getType();

    if (StringUtils.isBlank(mimeType)) {
      return false;
    }

    for (final String mediaType: mediaTypes) {
      if (mediaType.equals(mimeType)) {
        return true;
      }

      final String primaryType = StringUtils.substringBefore(mimeType, "/");
      if (StringUtils.substringBefore(mediaType, "/").equals(primaryType)) {
        final String subType = StringUtils.defaultIfBlank(
          StringUtils.substringAfterLast(mimeType, "+"),
          StringUtils.substringAfter(mimeType, "/"));

        if (StringUtils.substringAfter(mediaType, "/").equals(subType)) {
          return true;
        }
      }
    }
    return false;
  }

  private String buildSchemaKey(final MimeType mimeType) {
    return Names.getShortMimeType(mimeType) + "@" + mimeType.getSchema().hashCode();
  }

  public JClass getSchemaClass(final MimeType mimeType) throws IOException {
    final String schemaNameOrContent = mimeType.getSchema();
    if (StringUtils.isBlank(schemaNameOrContent)) {
      return null;
    }

    final String buildSchemaKey = buildSchemaKey(mimeType);

    final JClass existingClass = schemaClasses.get(buildSchemaKey);
    if (existingClass != null) {
      return existingClass;
    }

    if (isCompatibleWith(mimeType, APPLICATION_XML, TEXT_XML)) {
      //at this point all classes generated from XSDs are contained in the schemaClasses map;
      return null;
    } else if (isCompatibleWith(mimeType, APPLICATION_JSON)) {
      final Map.Entry<File, String> schemaNameAndFile = context.getSchemaFile(schemaNameOrContent);
      if (StringUtils.isBlank(schemaNameAndFile.getValue())) {
        schemaNameAndFile.setValue(Names.buildNestedSchemaName(mimeType, context.getConfiguration()));
      }

      final String className = Names.buildJavaFriendlyName(schemaNameAndFile.getValue());
      final JClass generatedClass = context.generateClassFromJsonSchema(className,
        schemaNameAndFile.getKey().toURI().toURL());
      schemaClasses.put(buildSchemaKey, generatedClass);

      return generatedClass;
    } else {
      return null;
    }
  }

  public void collectXmlSchemaFiles(Resource resource,
                                    Map<String, File> schemaFiles) {
    Collection<Action> actions = resource.getActions().values();
    for (Action a: actions) {
      Map<String, Response> responses = a.getResponses();
      if (responses != null) {
        for (Response resp: responses.values()) {
          Map<String, MimeType> body = resp.getBody();
          if (body != null) {
            for (MimeType mt: body.values()) {
              try {
                collectXmlSchemaFiles(mt, schemaFiles);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        }
      }
      Map<String, MimeType> body = a.getBody();
      if (body != null) {
        for (MimeType mt: body.values()) {
          try {
            collectXmlSchemaFiles(mt, schemaFiles);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
    }
    Collection<Resource> resources = resource.getResources().values();
    for (Resource r: resources) {
      collectXmlSchemaFiles(r, schemaFiles);
    }
  }

  private void collectXmlSchemaFiles(MimeType mimeType,
                                     Map<String, File> schemaFiles) throws IOException {
    if (!isCompatibleWith(mimeType, APPLICATION_XML, TEXT_XML)) {
      return;
    }

    final String schemaNameOrContent = mimeType.getSchema();
    if (StringUtils.isBlank(schemaNameOrContent)) {
      return;
    }
    final String buildSchemaKey = buildSchemaKey(mimeType);
    final Map.Entry<File, String> schemaNameAndFile = context.getSchemaFile(schemaNameOrContent);
    schemaFiles.put(buildSchemaKey, schemaNameAndFile.getKey());
  }

  public JType getGeneratorType(final Class<?> clazz) {
    return context.getGeneratorType(clazz);
  }
}