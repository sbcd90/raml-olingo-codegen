package org.raml.olingo.codegen.core;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.Action;
import org.raml.model.Response;
import org.raml.model.MimeType;
import org.raml.olingo.codegen.core.extn.GeneratorExtension;
import org.raml.olingo.codegen.core.extn.InterfaceNameBuilderExtension;
import org.raml.parser.loader.ResourceLoader;
import org.raml.parser.loader.UrlResourceLoader;
import org.raml.parser.loader.ClassPathResourceLoader;
import org.raml.parser.loader.FileResourceLoader;
import org.raml.parser.loader.CompositeResourceLoader;
import org.raml.parser.rule.ValidationResult;
import org.raml.parser.visitor.RamlDocumentBuilder;
import org.raml.parser.visitor.RamlValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Reader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

public abstract class AbstractGenerator {

  private List<String> entityTypes;
  protected Context context;
  protected Types types;
  protected List<GeneratorExtension> extensions;

  protected Map<String, JClass> schemas;

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGenerator.class);

  private ResourceLoader[] prepareResourceLoaders(final Configuration configuration,
                                                  final String location) {
    File sourceDirectory = configuration.getSourceDirectory();
    List<ResourceLoader> loaderList = new ArrayList<ResourceLoader>(
      Arrays.asList(new UrlResourceLoader(), new ClassPathResourceLoader()));
    if (sourceDirectory != null) {
      String sourceDirAbsPath = sourceDirectory.getAbsolutePath();
      loaderList.add(new FileResourceLoader(sourceDirAbsPath));
    }

    if (location != null && location.length() > 0) {
      String sourceDirAbsPath = sourceDirectory.getAbsolutePath();
      String fl = new File(location).getParent();

      if (sourceDirAbsPath.endsWith(fl)) {
        sourceDirAbsPath = sourceDirAbsPath.substring(0, sourceDirAbsPath.length() - fl.length());
        loaderList.add(new FileResourceLoader(sourceDirAbsPath));
        loaderList.add(new FileResourceLoader(sourceDirectory) {
          @Override
          public InputStream fetchResource(String resourceName) {
            File includedFile = new File(resourceName);
            FileInputStream inputStream = null;

            try {
              return new FileInputStream(includedFile);
            } catch (FileNotFoundException e) {
              // ignore
            }

            return inputStream;
          }
        });
      } else {
        loaderList.add(new FileResourceLoader(location));
        loaderList.add(new FileResourceLoader(""));
      }
    }
    ResourceLoader[] loaderArray = loaderList
      .toArray(new ResourceLoader[loaderList.size()]);
    return loaderArray;
  }

  protected Set<String> run(final Raml raml,
                            final Configuration configuration) throws Exception {
    validate(configuration);
    extensions = configuration.getExtensions();
    context = new Context(configuration, raml);
    types = new Types(context);
    schemas = new HashMap<String, JClass>();

    entityTypes = new ArrayList<String>();

    for (GeneratorExtension extension: extensions) {
      extension.setRaml(raml);
      extension.setCodeModel(context.getCodeModel());
    }

    Collection<Resource> resources = raml.getResources().values();
    types.generateClassesFromXmlSchemas(resources);

    for (final Resource resource: resources) {
      entityTypes.add(Names.buildJavaFriendlyName(
        StringUtils.defaultIfBlank(resource.getDisplayName(), resource.getRelativeUri())));
      createResourceInterface(resource, raml, configuration);
    }
    Set<String> generatedFiles = context.generate();

    context.resetCodeModel();
    createMetadataInterface(context, entityTypes);
    context.generateMetadataCode();

    return generatedFiles;
  }

  protected void createResourceInterface(final Resource resource,
                                         final Raml raml, Configuration configuration) throws Exception {
    String resourceInterfaceName = null;
    for (GeneratorExtension e: extensions) {
      if (e instanceof InterfaceNameBuilderExtension) {
        InterfaceNameBuilderExtension inbe = (InterfaceNameBuilderExtension) e;
        resourceInterfaceName = inbe.buildResourceInterfaceName(resource);
        if (resourceInterfaceName != null) {
          break;
        }
      }
    }

    if (resourceInterfaceName == null) {
      resourceInterfaceName = Names.buildResourceInterfaceName(resource, configuration);
    }

    final JDefinedClass resourceInterface = context.createResourceInterface(resourceInterfaceName,
      EntityCollectionProcessor.class);
    context.setCurrentEntityCollectionResourceInterface(resourceInterface);
    OlingoCodeGenerator.generateInitMethod(resourceInterface, context, types);

    if (StringUtils.isNotBlank(resource.getDescription())) {
      resourceInterface.javadoc().add(resource.getDescription());
    }

    addResourceMethods(resource, configuration, resourceInterface);

    for (GeneratorExtension e: extensions) {
      e.onCreateResourceInterface(resourceInterface, resource);
    }

    Collection<JMethod> methods = context.getCurrentEntityCollectionResourceInterface().methods();

    List<String> methodNames = new ArrayList<String>();
    for (JMethod method: methods) {
      methodNames.add(method.name());
    }

    /**
     * generate unimplemented methods
     */
    for (String standardMethod: OlingoCodeGenerator.getStandardEntityCollectionMethods()) {
      if (!methodNames.contains(standardMethod)) {
        switch (standardMethod) {
          case "readEntityCollection":
            OlingoCodeGenerator.generateReadEntityCollectionProcessorMethod(context.getCurrentEntityCollectionResourceInterface(),
              context, types, null, 0, null, null, false, false);
        }
      }
    }
  }

  protected void addResourceMethods(final Resource resource,
                                    final Configuration configuration,
                                    final JDefinedClass resourceInterface) throws Exception {
    for (final Action action: resource.getActions().values()) {
      if (action.getType().name().equals("GET")) {
        addResourceMethods(resourceInterface, resource, action, null, false);
      } else if (action.getType().name().equals("POST") ||
        action.getType().name().equals("PUT") ||
        action.getType().name().equals("PATCH") ||
        action.getType().name().equals("DELETE")) {

        if (context.getCurrentEntityResourceInterface() != null) {
          JDefinedClass entityResourceInterface = context.getCurrentEntityResourceInterface();

          addResourceMethods(entityResourceInterface, resource, action, null, false);
        } else {
          String resourceInterfaceName = Names.buildResourceInterfaceName(resource, configuration, "Entity");
          final JDefinedClass entityResourceInterface = context.createResourceInterface(resourceInterfaceName,
            EntityProcessor.class);
          context.setCurrentEntityResourceInterface(entityResourceInterface);

          OlingoCodeGenerator.generateInitMethod(entityResourceInterface, context, types);

          addResourceMethods(entityResourceInterface, resource, action, null, false);
        }

      }
    }

    if (resource.getResources().values().size() == 1) {
      if (context.getCurrentEntityResourceInterface() != null) {
        JDefinedClass entityResourceInterface = context.getCurrentEntityResourceInterface();

        addResourceMethods(resource.getResources().values().iterator().next(), configuration, entityResourceInterface);
      } else {
        String resourceInterfaceName = Names.buildResourceInterfaceName(resource, configuration, "Entity");
        final JDefinedClass entityResourceInterface = context.createResourceInterface(resourceInterfaceName,
          EntityProcessor.class);
        context.setCurrentEntityResourceInterface(entityResourceInterface);

        OlingoCodeGenerator.generateInitMethod(entityResourceInterface, context, types);
        addResourceMethods(resource.getResources().values().iterator().next(), configuration, entityResourceInterface);
      }
    } else if (resource.getResources().values().size() > 1) {
      throw new RuntimeException("cannot have more than one child resource");
    }

    if (context.getCurrentEntityResourceInterface() != null) {
      Collection<JMethod> methods = context.getCurrentEntityResourceInterface().methods();

      List<String> methodNames = new ArrayList<String>();
      for (JMethod method : methods) {
        methodNames.add(method.name());
      }

      /**
       * generate unimplemented methods
       */
      for (String standardMethod : OlingoCodeGenerator.getStandardEntityMethods()) {
        if (!methodNames.contains(standardMethod)) {
          switch (standardMethod) {
            case "readEntity":
              OlingoCodeGenerator.generateReadEntityMethod(context.getCurrentEntityResourceInterface(), context, types,
                null, 0, null, null, false, false);
              break;
            case "createEntity":
              OlingoCodeGenerator.generateCreateEntityMethod(context.getCurrentEntityResourceInterface(), context, types,
                null, 0, null, null, null, false, false);
              break;
            case "updateEntity":
              OlingoCodeGenerator.generateUpdateEntityMethod(context.getCurrentEntityResourceInterface(), context, types,
                null, 0, null, null, null, null, false, false);
              break;
            case "deleteEntity":
              OlingoCodeGenerator.generateDeleteEntityMethod(context.getCurrentEntityResourceInterface(), context, types,
                null, 0, null, false, false);
              break;
          }
        }
      }
    }

    context.setCurrentEntityResourceInterface(null);
  }

  protected abstract void addResourceMethod(final JDefinedClass resourceInterface,
                                            final Resource resource,
                                            final Action action,
                                            final MimeType bodyMimeType,
                                            final boolean addBodyMimeTypeInMethodName,
                                            final Collection<MimeType> uniqueResponseMimeTypes)
    throws Exception;

  protected abstract void createMetadataInterface(final Context context, final List<String> entityTypes)
    throws Exception;

  protected Collection<MimeType> getUniqueResponseMimeTypes(final Action action) {
    final Map<String, MimeType> responseMimeTypes = new HashMap<String, MimeType>();
    for (final Response response: action.getResponses().values()) {
      if (response.hasBody()) {
        for (final MimeType responseMimeType: response.getBody().values()) {
          if (responseMimeType != null) {
            responseMimeTypes.put(responseMimeType.getType(), responseMimeType);
          }
        }
      }
    }
    return responseMimeTypes.values();
  }

  private void addResourceMethods(final JDefinedClass resourceInterface,
                                  final Resource resource, final Action action, final MimeType bodyMimeType,
                                  final boolean addBodyMimeTypeInMethodName) throws Exception {
    final Collection<MimeType> uniqueResponseMimeTypes = getUniqueResponseMimeTypes(action);

    addResourceMethod(resourceInterface, resource, action,
      bodyMimeType, addBodyMimeTypeInMethodName, uniqueResponseMimeTypes);
  }

  private void validate(final Configuration configuration) {
    Validate.notNull(configuration, "configuration can't be null");

    final File outputDirectory = configuration.getOutputDirectory();
    Validate.notNull(outputDirectory, "output Directory can't be null");

    Validate.isTrue(outputDirectory.isDirectory(), outputDirectory +
      " is not a pre-existing directory");
    Validate.isTrue(outputDirectory.canWrite(), outputDirectory +
      " can't be written to");

    if (outputDirectory.listFiles().length > 0) {
      LOGGER.warn("Directory " +
        outputDirectory +
        " is not empty, generation will work but pre-existing files may remain and produce unexpected results");
    }

    Validate.notEmpty(configuration.getBasePackageName(),
      "base package name can't be empty");
  }

  protected static String toDetailedString(ValidationResult item) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("\t");
    stringBuilder.append(item.getLevel());
    stringBuilder.append(" ");
    stringBuilder.append(item.getMessage());
    if (item.getLine() != ValidationResult.UNKNOWN) {
      stringBuilder.append(" (line ");
      stringBuilder.append(item.getLine());
      if (item.getStartColumn() != ValidationResult.UNKNOWN) {
        stringBuilder.append(", col ");
        stringBuilder.append(item.getStartColumn());
        if (item.getEndColumn() != item.getStartColumn()) {
          stringBuilder.append(" to ");
          stringBuilder.append(item.getEndColumn());
        }
      }
      stringBuilder.append(")");
    }
    return stringBuilder.toString();
  }

  public Set<String> run(final Reader ramlReader,
                         final Configuration configuration, String readerLocation)
    throws Exception {
    final String ramlBuffer = IOUtils.toString(ramlReader);
    String folder = new File(readerLocation).getParent();
    ResourceLoader[] loaderArray = prepareResourceLoaders(configuration, folder);

    final List<ValidationResult> results = RamlValidationService
      .createDefault(new CompositeResourceLoader(loaderArray))
      .validate(ramlBuffer, readerLocation);
    if (ValidationResult.areValid(results)) {
      return run(new RamlDocumentBuilder(
        new CompositeResourceLoader(loaderArray)).build(ramlBuffer, readerLocation),
        configuration);
    } else {
      final List<String> validationErrors = Lists.transform(results,
        new Function<ValidationResult, String>() {
          @Nullable
          @Override
          public String apply(@Nullable ValidationResult input) {
            return toDetailedString(input);
          }
        });

      throw new IllegalArgumentException("Invalid RAML definition:\n" +
        StringUtils.join(validationErrors, "\n"));
    }
  }
}