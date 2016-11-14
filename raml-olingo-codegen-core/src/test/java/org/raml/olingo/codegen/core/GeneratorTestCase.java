package org.raml.olingo.codegen.core;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GeneratorTestCase {
  private static final String TEST_BASE_PACKAGE = "org.raml.jaxrs.test";
  private static final Logger LOG = LoggerFactory.getLogger(GeneratorTestCase.class);



  @Rule
  public TemporaryFolder codegenOutputFolder = new TemporaryFolder();

  @Test
  public void runBasicRamlFile() {
    String testSpecificPkg = "raml.basic";

    String[] filesToBeGenerated = new String[]{
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntity.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityCollectionProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractFileContentEntity.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractFileContentEntityCollection.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntityCollection.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\metadata\\AbstractEdmProvider.java")
    };

    run(testSpecificPkg,
      this.getClass().getClassLoader().getResource("base").getPath(), filesToBeGenerated);
  }

  @Test
  public void runRamlFileWithBodyParams() {
    String testSpecificPkg = "raml.with.body.params";

    String[] filesToBeGenerated = new String[]{
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractFileContentEntity.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractFileContentEntityCollection.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityCollectionProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\metadata\\AbstractEdmProvider.java")
    };

    run(testSpecificPkg,
      this.getClass().getClassLoader().getResource("bodyparams").getPath(), filesToBeGenerated);
  }

  @Test
  public void runRamlFileWithSchemas() {
    String testSpecificPkg = "raml.with.schemas";

    String[] filesToBeGenerated = new String[] {
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntity.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityCollectionProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntityCollection.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\metadata\\AbstractEdmProvider.java")
    };

    run(testSpecificPkg,
      this.getClass().getClassLoader().getResource("schemas").getPath(), filesToBeGenerated);
  }

  @Test
  public void runRamlFileWithResourceTypes() {
    String testSpecificPkg = "raml.with.resource.types";

    String[] filesToBeGenerated = new String[] {
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntity.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityCollectionProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntityCollection.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\metadata\\AbstractEdmProvider.java")
    };

    run(testSpecificPkg,
      this.getClass().getClassLoader().getResource("resourcetypes").getPath(), filesToBeGenerated);
  }

  @Test
  public void runRamlFileWithParameters() {
    String testSpecificPkg = "raml.with.parameters";

    String[] filesToBeGenerated = new String[] {
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityCollectionProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntityCollection.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\metadata\\AbstractEdmProvider.java")
    };

    run(testSpecificPkg,
      this.getClass().getClassLoader().getResource("parameters").getPath(), filesToBeGenerated);
  }

  @Test
  public void runRamlFileWithTraits() {
    String testSpecificPkg = "raml.with.traits";

    String[] filesToBeGenerated = new String[] {
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityCollectionProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\AbstractSongsEntityCollection.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\CommonEntityProcessor.java"),
      TEST_BASE_PACKAGE.replace(".", "\\").concat("\\" + testSpecificPkg.replace(".", "\\"))
        .concat("\\resource\\metadata\\AbstractEdmProvider.java")
    };

    run(testSpecificPkg,
      this.getClass().getClassLoader().getResource("traits").getPath(), filesToBeGenerated);
  }

  private void run(String testCaseName, String sourceDirectory, String[] expected) {
    Set<String> generatedSources = new HashSet<String>();

    Configuration configuration = new Configuration();

    File sourceDir = new File(sourceDirectory);

    configuration.setSourceDirectory(new File(sourceDirectory));
    configuration.setOutputDirectory(codegenOutputFolder.getRoot());
    configuration.setBasePackageName(TEST_BASE_PACKAGE + "." + testCaseName);

    Collection<File> ramlFiles = FileUtils.listFiles(sourceDir,
      new String[]{"raml", "yaml"}, true);

    try {
      Generator generator = new Generator();

      for (final File file : ramlFiles) {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        reader.close();
        if (line.startsWith("#%RAML")) {
          generatedSources =
            generator.run(new FileReader(file), configuration, file.getAbsolutePath());
          for (String expectedData: expected) {
            Assert.assertTrue(generatedSources.contains(expectedData));
          }
        } else {
          LOG.error(file.getAbsolutePath() + " is not a valid raml file");
          Assert.fail();
        }
      }
    } catch (Exception e) {
      Assert.fail();
    }

  }
}