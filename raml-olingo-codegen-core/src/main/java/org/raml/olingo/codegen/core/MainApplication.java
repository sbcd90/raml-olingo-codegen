package org.raml.olingo.codegen.core;

import org.apache.commons.io.FileUtils;
import org.jsonschema2pojo.AnnotationStyle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;

public class MainApplication {

  private File sourceDirectory;

  private File outputDirectory;

  private final boolean useTitlePropertyForSchemaNames = false;

  private final String basePackageName = "org.raml.test";

  private final String modelPackageName = "model";

  private final boolean useJsr303Annotations = false;

  private final String jsonMapper = "jackson2";

  public void execute() {
    if (sourceDirectory == null) {
      throw new IllegalArgumentException("source Directory is mandatory");
    }

    try {
      FileUtils.forceMkdir(outputDirectory);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      FileUtils.cleanDirectory(outputDirectory);
    } catch (IOException e) {
      e.printStackTrace();
    }

    final Configuration configuration = new Configuration();

    try {
      configuration.setUseTitlePropertyWhenPossible(useTitlePropertyForSchemaNames);
      configuration.setBasePackageName(basePackageName);
      configuration.setModelPackageName(modelPackageName);
      configuration.setOutputDirectory(outputDirectory);
      configuration.setUseJsr303Annotations(useJsr303Annotations);
      configuration.setJsonMapper(AnnotationStyle.valueOf(jsonMapper.toUpperCase()));
      configuration.setSourceDirectory(sourceDirectory);
      configuration.setJsonMapperConfiguration(null);
      configuration.setNamespace("com.sap.raml");
      configuration.setContainerName("test");
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      final Generator generator = new Generator();

      for (final File file: getRamlFiles()) {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        reader.close();
        if (line.startsWith("#%RAML")) {
          configuration.setBasePackageName(basePackageName.concat(computeSubPackageName(file)));
          generator.run(new FileReader(file), configuration, file.getAbsolutePath());
        } else {
          System.out.println(file.getAbsolutePath() + " is not a valid raml file");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Collection<File> getRamlFiles() throws RuntimeException {
    if (!sourceDirectory.isDirectory()) {
      throw new RuntimeException("source Directory is not a valid directory");
    }
    return FileUtils.listFiles(sourceDirectory, new String[]{"raml", "yaml"}, true);
  }

  private String computeSubPackageName(File file) {
    return "";
  }

  public static void main(String[] args) {
    MainApplication app = new MainApplication();
    app.sourceDirectory = new File("C:/Users/i076326/Documents/programs/raml-jaxrs/source");
    app.outputDirectory = new File("C:/Users/i076326/Documents/programs/raml-jaxrs/output");

    app.execute();
  }
}