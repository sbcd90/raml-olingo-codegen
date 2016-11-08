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

  private File outputDirectory;
  private String basePackageName;
  private String modelPackageName;
  private boolean useJsr303Annotations = false;
  private AnnotationStyle jsonMapper = AnnotationStyle.JACKSON1;
  private File sourceDirectory;
  private Class<? extends Throwable> methodThrowException = Exception.class;
  private Map<String, String> jsonMapperConfiguration;
  private boolean emptyResponseReturnVoid;
  private Class customAnnotator = NoopAnnotator.class;
  private String restIFPackageName = "resource";
  private String interfaceNameSuffix = "EntityCollection";

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

  public Class getCustomAnnotator() {
    return customAnnotator;
  }

  public void setCustomAnnotator(Class customAnnotator) {
    this.customAnnotator = customAnnotator;
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

  public boolean isEmptyResponseReturnVoid() {
    return emptyResponseReturnVoid;
  }

  public void setEmptyResponseReturnVoid(boolean emptyResponseReturnVoid) {
    this.emptyResponseReturnVoid = emptyResponseReturnVoid;
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

  public String getInterfaceNameSuffix() {
    return interfaceNameSuffix;
  }

  public void setInterfaceNameSuffix(String interfaceNameSuffix) {
    this.interfaceNameSuffix = interfaceNameSuffix;
  }
}