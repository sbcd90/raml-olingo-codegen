package org.raml.olingo.codegen.core;

import org.apache.commons.io.FileUtils;
import org.jsonschema2pojo.AnnotationStyle;
import org.raml.olingo.codegen.core.extn.GeneratorExtension;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class Launcher {

  public static void main(String[] args) {
    Map<String, String> argMap = createArgMap(args);

    Configuration configuration = createConfiguration(argMap);

    boolean removeOldOutput = false;
    String removeOldOutputStringValue = argMap.get("removeOldOutput");
    if (removeOldOutputStringValue != null) {
      removeOldOutput = Boolean.parseBoolean(removeOldOutputStringValue);
    }

    Collection<File> ramlFiles = getRamlFiles(argMap);
    if (ramlFiles.isEmpty()) {
      return;
    }

    if (removeOldOutput) {
      try {
        FileUtils.cleanDirectory(configuration.getOutputDirectory());
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    final Generator generator = new Generator();
    for (final File ramlFile: ramlFiles) {
      try {
        generator.run(new FileReader(ramlFile), configuration, ramlFile.getAbsolutePath());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private static Collection<File> getRamlFiles(Map<String, String> argMap) {
    String sourcePaths = argMap.get("sourcePaths");
    String sourceDirectoryPath = argMap.get("sourceDirectory");

    if (!isEmptyString(sourcePaths)) {
      List<File> sourceFiles = new ArrayList<File>();
      String[] split = sourcePaths.split(System.getProperty("path.separator"));
      for (String str: split) {
        sourceFiles.add(new File(str));
      }
      return sourceFiles;
    } else {
      File sourceDirectory = new File(sourceDirectoryPath);
      if (!sourceDirectory.isDirectory()) {
        throw new RuntimeException("The provided path doesn't refer to a valid directory: "+ sourceDirectory);
      }
      return FileUtils.listFiles(sourceDirectory, new String[]{"raml", "yaml"}, false);
    }
  }

  private static Configuration createConfiguration(Map<String, String> argMap) {
    Configuration configuration = new Configuration();

    File rootDirectory = new File(System.getProperty("user.dir"));
    File outputDirectory = new File(rootDirectory, "generated-sources/raml-olingo");
    File sourceDirectory = new File(rootDirectory, "src/main/raml");

    String basePackageName = null;
    String jsonMapper = "jackson1";

    boolean useTitlePropertyForSchemaNames = false;
    String modelPackageName = "model";
    String namespace = null;
    String containerName = null;
    List<GeneratorExtension> extensions = null;
    Map<String, String> jsonMapperConfiguration = new HashMap<String, String>();

    String restIFPackageName = "resource";
    String entityCollectionInterfaceNameSuffix = "EntityCollection";
    String entityInterfaceNameSuffix = "Entity";

    for (Map.Entry<String, String> arg: argMap.entrySet()) {
      String argName = arg.getKey();
      String argValue = arg.getValue();

      if (argName.equals("outputDirectory")) {
        outputDirectory = new File(argValue);
      }

      if (argName.equals("sourceDirectory")) {
        sourceDirectory = new File(argValue);
      }

      if (argName.equals("basePackageName")) {
        basePackageName = argValue;
      }

      if (argName.equals("jsonMapper")) {
        jsonMapper = argValue;
      }

      if (argName.equals("useTitlePropertyForSchemaNames")) {
        useTitlePropertyForSchemaNames = Boolean.parseBoolean(argValue);
      }

      if (argName.equals("modelPackageName")) {
        modelPackageName = argValue;
      }

      if (argName.equals("restIFPackageName")) {
        restIFPackageName = argValue;
      }

      if (argName.equals("entityCollectionInterfaceNameSuffix")) {
        entityCollectionInterfaceNameSuffix = argValue;
      }

      if (argName.equals("entityInterfaceNameSuffix")) {
        entityInterfaceNameSuffix = argValue;
      }

      if (argName.equals("namespace")) {
        namespace = argValue;
      }

      if (argName.equals("containerName")) {
        containerName = argValue;
      }

      if (argName.equals("extensions")) {
        extensions = new ArrayList<GeneratorExtension>();
        String[] extensionClasses = argValue.split(",");
        for (String s: extensionClasses) {
          s = s.trim();

          try {
            extensions.add((GeneratorExtension) Class.forName(s).newInstance());
          } catch (Exception e) {
            throw new RuntimeException("unknown extension " + e);
          }
        }
      }

      if (argName.startsWith("jsonschema2pojo.")) {
        String name = argName.substring("jsonschema2pojo.".length());
        jsonMapperConfiguration.put(name, argValue);
      }
    }

    if (basePackageName == null) {
      throw new RuntimeException("Base package must be specified.");
    }

    if (!outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    if (!outputDirectory.isDirectory()) {
      throw new RuntimeException("Output destination must be a directory: " + outputDirectory);
    }

    configuration.setBasePackageName(basePackageName);
    configuration.setOutputDirectory(outputDirectory);
    configuration.setJsonMapper(AnnotationStyle.valueOf(jsonMapper.toUpperCase()));
    configuration.setSourceDirectory(sourceDirectory);
    configuration.setUseTitlePropertyWhenPossible(useTitlePropertyForSchemaNames);
    configuration.setModelPackageName(modelPackageName);
    configuration.setRestIFPackageName(restIFPackageName);
    configuration.setEntityCollectionInterfaceNameSuffix(entityCollectionInterfaceNameSuffix);
    configuration.setEntityInterfaceNameSuffix(entityInterfaceNameSuffix);

    if (namespace != null)
      configuration.setNamespace(namespace);
    if (containerName != null)
      configuration.setContainerName(containerName);

    if (extensions != null) {
      configuration.setExtensions(extensions);
    }

    if (jsonMapperConfiguration.isEmpty()) {
      configuration.setJsonMapperConfiguration(jsonMapperConfiguration);
    }

    return configuration;
  }

  private static Map<String, String> createArgMap(String[] args) {
    Map<String, String> map = new HashMap<String, String>();
    for (int i = 0; i < args.length; i++) {
      if (args[i].startsWith("-")) {
        String argName = args[i].substring(1);
        if ((i+1) < args.length) {
          String argValue = args[i+1];
          if (!argValue.startsWith("-")) {
            map.put(argName, argValue);
            i++;
          }
        }
      }
    }
    return map;
  }

  private static boolean isEmptyString(String str) {
    return str == null || str.trim().isEmpty();
  }
}