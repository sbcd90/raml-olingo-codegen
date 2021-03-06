package org.raml.olingo.codegen.core;

import org.jsonschema2pojo.AnnotationStyle;
import org.jsonschema2pojo.DefaultGenerationConfig;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.NoopAnnotator;
import org.raml.olingo.codegen.core.extn.GeneratorExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Configuration {

  /**
   * output Directory to put the generated files.
   */
  private File outputDirectory;

  /**
   * base package name under which generated files are put.
   */
  private String basePackageName;
  private String modelPackageName;
  private boolean useJsr303Annotations = false;

  /**
   * annotation style for schemas.
   */
  private AnnotationStyle jsonMapper = AnnotationStyle.JACKSON1;

  /**
   * source directory to read the raml files from.
   */
  private File sourceDirectory;

  /**
   * default exception class to be used in generated methods
   */
  private Class<? extends Throwable> methodThrowException = Exception.class;

  /**
   * additional configuration for jsonMapper
   */
  private Map<String, String> jsonMapperConfiguration;
  private Class customAnnotator = NoopAnnotator.class;

  /**
   * package under base package where Entity Collections & Entities are generated.
   */
  private String restIFPackageName = "resource";

  /**
   * suffix for Entity Collection classes both generated & implemented.
   */
  private String entityCollectionInterfaceNameSuffix = "EntityCollection";

  /**
   * suffix for Entity classes both generated & implemented.
   */
  private String entityInterfaceNameSuffix = "Entity";

  /**
   * OData namespace
   */
  private String namespace = "OData.Demo";

  /**
   * OData container name
   */
  private String containerName = "Container";

  private List<String> ignoredParameterNames = new ArrayList<String>();

  public void setIgnoredParameterNames(List<String> ignoredParameterNames) {
    this.ignoredParameterNames = ignoredParameterNames;
  }

  public List<String> getIgnoredParameterNames() {
    return ignoredParameterNames;
  }

  private boolean useTitlePropertyWhenPossible;

  public boolean isUseTitlePropertyWhenPossible() {
    return useTitlePropertyWhenPossible;
  }

  public void setUseTitlePropertyWhenPossible(boolean useTitlePropertyWhenPossible) {
    this.useTitlePropertyWhenPossible = useTitlePropertyWhenPossible;
  }

  private List<GeneratorExtension> extensions = new ArrayList<GeneratorExtension>();

  public GenerationConfig createJsonSchemaGenerationConfig() {
    return new DefaultGenerationConfig() {
      @Override
      public AnnotationStyle getAnnotationStyle() {
        return jsonMapper;
      }

      @Override
      public boolean isIncludeJsr303Annotations() {
        return useJsr303Annotations;
      }

      @Override
      public boolean isUseCommonsLang3() {
        return getConfiguredValue("useCommonsLang3", false);
      }

      @Override
      public boolean isGenerateBuilders() {
        return getConfiguredValue("generateBuilders", true);
      }

      @Override
      public boolean isIncludeHashcodeAndEquals() {
        return getConfiguredValue("includeHashcodeAndEquals", false);
      }

      @Override
      public Class getCustomAnnotator() {
        return customAnnotator;
      }

      @Override
      public boolean isIncludeToString() {
        return getConfiguredValue("includeToString", false);
      }

      @Override
      public boolean isUseLongIntegers() {
        return getConfiguredValue("useLongIntegers", false);
      }

      @Override
      public boolean isIncludeConstructors() {
        return getConfiguredValue("includeConstructors", super.isIncludeConstructors());
      }

      @Override
      public boolean isConstructorsRequiredPropertiesOnly() {
        return getConfiguredValue("constructorsRequiredPropertiesOnly",
          super.isConstructorsRequiredPropertiesOnly());
      }

      @Override
      public boolean isIncludeAccessors() {
        return getConfiguredValue("includeAccessors", super.isIncludeAccessors());
      }

      @Override
      public boolean isUseJodaDates() {
        return getConfiguredValue("useJodaDates", false);
      }

      @Override
      public boolean isUseJodaLocalDates() {
        return getConfiguredValue("useJodaLocalDates", false);
      }

      @Override
      public boolean isUseJodaLocalTimes() {
        return getConfiguredValue("useJodaLocalTimes", false);
      }

      @Override
      public String getDateTimeType() {
        return getConfiguredValueStr("dateTimeType", null);
      }

      @Override
      public String getDateType() {
        return getConfiguredValueStr("dateType", null);
      }

      @Override
      public String getTimeType() {
        return getConfiguredValueStr("timeType", null);
      }

      private boolean getConfiguredValue(final String key, final boolean def) {
        if (jsonMapperConfiguration == null || jsonMapperConfiguration.isEmpty()) {
          return def;
        }
        final String val = jsonMapperConfiguration.get(key);
        return val != null ? Boolean.parseBoolean(val): def;
      }

      private String getConfiguredValueStr(final String key, final String def) {
        if (jsonMapperConfiguration == null || jsonMapperConfiguration.isEmpty()) {
          return def;
        }
        final String val = jsonMapperConfiguration.get(key);
        return val != null ? val: def;
      }
    };
  }

  public File getOutputDirectory() {
    return outputDirectory;
  }

  public void setOutputDirectory(File outputDirectory) {
    this.outputDirectory = outputDirectory;
  }

  public String getBasePackageName() {
    return basePackageName;
  }

  public void setBasePackageName(String basePackageName) {
    this.basePackageName = basePackageName;
  }

  public String getModelPackageName() {
    return modelPackageName;
  }

  public void setModelPackageName(String modelPackageName) {
    this.modelPackageName = modelPackageName;
  }

  public boolean isUseJsr303Annotations() {
    return useJsr303Annotations;
  }

  public void setUseJsr303Annotations(boolean useJsr303Annotations) {
    this.useJsr303Annotations = useJsr303Annotations;
  }

  public AnnotationStyle getJsonMapper() {
    return jsonMapper;
  }

  public void setJsonMapper(AnnotationStyle jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  public Class<? extends Throwable> getMethodThrowException() {
    return methodThrowException;
  }

  public void setMethodThrowException(Class<? extends Throwable> methodThrowException) {
    this.methodThrowException = methodThrowException;
  }

  public File getSourceDirectory() {
    return sourceDirectory;
  }

  public void setSourceDirectory(File sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  public Map<String, String> getJsonMapperConfiguration() {
    return jsonMapperConfiguration;
  }

  public void setJsonMapperConfiguration(Map<String, String> jsonMapperConfiguration) {
    this.jsonMapperConfiguration = jsonMapperConfiguration;
  }

  public List<GeneratorExtension> getExtensions() {
    return extensions;
  }

  public void setExtensions(List<GeneratorExtension> extensions) {
    this.extensions = extensions;
  }

  public String getRestIFPackageName() {
    return restIFPackageName;
  }

  public void setRestIFPackageName(String restIFPackageName) {
    this.restIFPackageName = restIFPackageName;
  }

  public String getEntityCollectionInterfaceNameSuffix() {
    return entityCollectionInterfaceNameSuffix;
  }

  public void setEntityCollectionInterfaceNameSuffix(String entityCollectionInterfaceNameSuffix) {
    this.entityCollectionInterfaceNameSuffix = entityCollectionInterfaceNameSuffix;
  }

  public String getEntityInterfaceNameSuffix() {
    return entityInterfaceNameSuffix;
  }

  public void setEntityInterfaceNameSuffix(String entityInterfaceNameSuffix) {
    this.entityInterfaceNameSuffix = entityInterfaceNameSuffix;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getContainerName() {
    return containerName;
  }

  public void setContainerName(String containerName) {
    this.containerName = containerName;
  }
}