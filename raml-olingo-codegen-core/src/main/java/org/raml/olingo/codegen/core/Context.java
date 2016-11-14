package org.raml.olingo.codegen.core;

import com.google.common.io.Files;
import com.sun.codemodel.*;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.ErrorReceiver;
import com.sun.tools.xjc.ModelLoader;
import com.sun.tools.xjc.model.Model;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.util.ErrorReceiverFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.olingo.commons.api.edm.FullQualifiedName;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.GenerationConfig;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.AnnotatorFactory;
import org.jsonschema2pojo.CompositeAnnotator;
import org.jsonschema2pojo.rules.RuleFactory;
import org.raml.model.Raml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.AbstractMap;

public class Context {
  private final Configuration configuration;
  private final Raml raml;
  private JCodeModel codeModel;
  private final Map<String, Set<String>> resourcesMethods;
  private final Map<String, Pair<String, String>> metadataEntities;

  private final SchemaMapper schemaMapper;

  private final File globalSchemaStore;
  private JDefinedClass currentEntityCollectionResourceInterface;
  private JDefinedClass currentEntityResourceInterface;

  private static final Logger LOGGER = LoggerFactory.getLogger(Context.class);

  public Context(final Configuration configuration,
                 final Raml raml) throws IOException {
    Validate.notNull(configuration, "configuration can't be null");
    Validate.notNull(raml, "raml can't be null");

    this.configuration = configuration;
    this.raml = raml;

    codeModel = new JCodeModel();

    resourcesMethods = new HashMap<String, Set<String>>();
    metadataEntities = new HashMap<String, Pair<String, String>>();

    globalSchemaStore = Files.createTempDir();

    for (Map.Entry<String, String> nameAndSchema:
      raml.getConsolidatedSchemas().entrySet()) {
      final File schemaFile = new File(globalSchemaStore, nameAndSchema.getKey());
      FileUtils.writeStringToFile(schemaFile, nameAndSchema.getValue(),
        Charset.defaultCharset(), false);
    }

    final GenerationConfig jsonSchemaGenerationConfig = configuration.createJsonSchemaGenerationConfig();
    schemaMapper = new SchemaMapper(new RuleFactory(jsonSchemaGenerationConfig,
      getAnnotator(jsonSchemaGenerationConfig), new SchemaStore()), new SchemaGenerator());
  }

  private static Annotator getAnnotator(GenerationConfig config) {
    Annotator coreAnnotator = new AnnotatorFactory().getAnnotator(config.getAnnotationStyle());
    if (config.getCustomAnnotator() != null) {
      Annotator customAnnotator = new AnnotatorFactory().getAnnotator(config.getCustomAnnotator());
      return new CompositeAnnotator(coreAnnotator, customAnnotator);
    }
    return coreAnnotator;
  }

  public JCodeModel getCodeModel() {
    return codeModel;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  public JClass generateClassFromJsonSchema(final String className, final URL schemaUrl)
    throws IOException {
    return schemaMapper.generate(new JCodeModel(), className, getModelPackage(), schemaUrl).boxify();
  }

  public Map<String, Pair<String, String>> getMetadataEntities() {
    return metadataEntities;
  }

  public Set<String> generate() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(baos);
    codeModel.build(configuration.getOutputDirectory(), ps);
    ps.close();

    final Set<String> generatedFiles = new HashSet<String>();
    generatedFiles.addAll(Arrays.asList(StringUtils.split(baos.toString())));

    FileUtils.deleteDirectory(globalSchemaStore);
    return generatedFiles;
  }

  public Map<String, JClass> generateClassesFromXmlSchemas(
    Map<String, File> schemaFiles) {
    Map<String, JClass> result = new HashMap<String, JClass>();
    if (schemaFiles == null || schemaFiles.isEmpty()) {
      return result;
    }

    Map<String, String> classNameToKeyMap = new HashMap<String, String>();
    for (Map.Entry<String, File> entry: schemaFiles.entrySet()) {
      String key = entry.getKey();
      File file = entry.getValue();

      List<JDefinedClass> classes = generateClassesFromXmlSchemas(new JCodeModel(), file);

      if (classes == null || classes.isEmpty()) {
        continue;
      }
      String className = classes.get(0).name();
      if (className != null) {
        classNameToKeyMap.put(className, key);
      }
    }

    File[] fileArray = schemaFiles.values().toArray(new File[schemaFiles.size()]);
    List<JDefinedClass> classList = generateClassesFromXmlSchemas(codeModel, fileArray);
    for (JDefinedClass cl: classList) {
      String name = cl.name();
      String key = classNameToKeyMap.get(name);
      if (key == null)
        continue;
      result.put(key, cl);
    }
    return result;
  }

  public Map.Entry<File, String> getSchemaFile(
    final String schemaNameOrContent) throws IOException {
    if (raml.getConsolidatedSchemas().containsKey(schemaNameOrContent)) {
      return new AbstractMap.SimpleEntry<File, String>(new File(globalSchemaStore, schemaNameOrContent),
        schemaNameOrContent);
    } else {
      final String schemaFileName = "schema" + schemaNameOrContent.hashCode();
      final File schemaFile = new File(globalSchemaStore, schemaFileName);
      FileUtils.writeStringToFile(schemaFile, schemaNameOrContent, Charset.defaultCharset(), false);
      return new AbstractMap.SimpleEntry<File, String>(schemaFile, null);
    }
  }

  private List<JDefinedClass> generateClassesFromXmlSchemas(JCodeModel codeModel, File... schemaFiles) {
    List<JDefinedClass> classList = new ArrayList<JDefinedClass>();

    List<String> argList = new ArrayList<String>();
    argList.add("-mark-generated");
    argList.add("-p");
    argList.add(getModelPackage());
    for (File f: schemaFiles) {
      argList.add(f.getAbsolutePath());
    }

    String[] args = argList.toArray(new String[argList.size()]);

    final Options opt = new Options();
    opt.setSchemaLanguage(Language.XMLSCHEMA);
    try {
      opt.parseArguments(args);
    } catch (BadCommandLineException e) {

    }

    ErrorReceiver receiver = new ErrorReceiverFilter() {
      @Override
      public void info(SAXParseException exception) {
        if (opt.verbose) {
          super.info(exception);
        }
      }

      @Override
      public void warning(SAXParseException exception) {
        if (!opt.quiet) {
          super.warning(exception);
        }
      }
    };

    try {
      Model model = ModelLoader.load(opt, codeModel, receiver);
      Outline outline = model.generateCode(opt, receiver);
      for (ClassOutline co: outline.getClasses()) {
        JDefinedClass cl = co.implClass;
        if (cl.outer() == null) {
          classList.add(cl);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return classList;
  }

  private String getModelPackage() {
    return configuration.getBasePackageName() +
      "." +
      configuration.getModelPackageName();
  }

  public String getMetadataPackage() {
    return configuration.getBasePackageName() +
      "." + configuration.getRestIFPackageName() + ".metadata";
  }

  public JDefinedClass createResourceInterface(final String name,
                                               final Class<?> overridingClass)
    throws Exception {
    return createResourceInterface(name, overridingClass, JMod.PUBLIC | JMod.ABSTRACT);
  }

  public JDefinedClass createResourceInterface(final String name,
                                               final Class<?> overridingClass,
                                               final int jMod)
    throws Exception {
    String actualName;
    int i = -1;
    while (true) {
      actualName = name + (++i == 0 ? "" : Integer.toString(i));

      if (!resourcesMethods.containsKey(actualName)) {
        resourcesMethods.put(actualName, new HashSet<String>());
        break;
      }
    }

    final JPackage pkg = codeModel._package(configuration.getBasePackageName() +
      "." + configuration.getRestIFPackageName());
    return pkg._class(jMod, actualName)
      ._implements(overridingClass);
  }

  public JDefinedClass createResourceInterface(final String name,
                                               final Class<?> overridingClass,
                                               final String packageName,
                                               final List<String> entityTypes)
    throws Exception {
    String actualName;
    int i = -1;
    while (true) {
      actualName = name + (++i == 0 ? "" : Integer.toString(i));

      if (!resourcesMethods.containsKey(actualName)) {
        resourcesMethods.put(actualName, new HashSet<String>());
        break;
      }
    }

    final JPackage pkg = codeModel._package(packageName);
    JDefinedClass metadataClass = pkg._class(JMod.PUBLIC | JMod.ABSTRACT, actualName)
      ._extends(overridingClass);

    metadataClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, "NAMESPACE",
      JExpr.lit(configuration.getNamespace()));
    metadataClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class, "CONTAINER_NAME",
      JExpr.lit(configuration.getContainerName()));
    metadataClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, FullQualifiedName.class, "CONTAINER",
      JExpr._new(codeModel.ref(FullQualifiedName.class))
        .arg(configuration.getNamespace()).arg(configuration.getContainerName()));

    for (String entityType: entityTypes) {
      metadataClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class,
        "ET_" + entityType.toUpperCase() + "_NAME",
        JExpr.lit(entityType.endsWith("s") ? entityType.substring(0, entityType.length() - 1) : entityType));
      metadataClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, FullQualifiedName.class,
        "ET_" + entityType.toUpperCase() + "_FQN",
        JExpr._new(codeModel.ref(FullQualifiedName.class)).arg(configuration.getNamespace())
          .arg(entityType.endsWith("s") ? entityType.substring(0, entityType.length() - 1) : entityType));
      metadataClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, String.class,
        "ES_" + entityType.toUpperCase() + "_NAME",
        JExpr.lit(entityType.endsWith("s") ? entityType : entityType.concat("s")));

      metadataEntities.put("ET_" + entityType.toUpperCase() + "_NAME", Pair.<String, String>of("ET_" + entityType.toUpperCase() + "_FQN",
        "ES_" + entityType.toUpperCase() + "_NAME"));
    }
    return metadataClass;
  }

  public void setCurrentEntityCollectionResourceInterface(final JDefinedClass currentEntityCollectionResourceInterface) {
    this.currentEntityCollectionResourceInterface = currentEntityCollectionResourceInterface;
  }

  public JDefinedClass getCurrentEntityCollectionResourceInterface() {
    return currentEntityCollectionResourceInterface;
  }

  public void setCurrentEntityResourceInterface(final JDefinedClass currentEntityResourceInterface) {
    this.currentEntityResourceInterface = currentEntityResourceInterface;
  }

  public JDefinedClass getCurrentEntityResourceInterface() {
    return currentEntityResourceInterface;
  }

  public JMethod createResourceMethod(final JDefinedClass resourceInterface,
                                      final String methodName,
                                      final JType returnType,
                                      int isAbstract) {
    final Set<String> existingMethodNames = resourcesMethods.get(resourceInterface.name());

    if (!existingMethodNames.contains(methodName)) {
      existingMethodNames.add(methodName);

      if (isAbstract > 0) {
        return resourceInterface.method(JMod.PUBLIC | isAbstract, returnType, methodName);
      } else {
        return resourceInterface.method(JMod.PUBLIC, returnType, methodName);
      }
    } else {
      LOGGER.warn("method already exists");
      return null;
    }
  }

  public void addOverrideAnnotationToResourceMethod(JMethod method) {
    method.annotate(codeModel.ref(Override.class));
  }

  public void addParamsToResourceMethod(JMethod method, List<Pair<String, Class<?>>> params) {
    for (Pair<String, Class<?>> param: params) {
      method.param(param.getValue(), param.getKey());
    }
  }

  public void addParamsToResourceMethod(JMethod method, List<Pair<String, JType>> params, boolean isJTypeUsed) {
    for (Pair<String, JType> param: params) {
      method.param(param.getValue(), param.getKey());
    }
  }

  public void addStmtToResourceMethodBody(JMethod method, String statement) {
    JBlock block = method.body();
    block.directStatement(statement);
  }

  public void addExceptionToResourceMethod(JMethod method, Class<? extends Throwable> exception) {
    method._throws(exception);
  }

  public void addDefaultExceptionToResourceMethod(JMethod method) {
    method._throws(configuration.getMethodThrowException());
  }

  public JType getGeneratorType(final Class<?> clazz) {
    return clazz.isPrimitive() ? JType.parse(codeModel, clazz.getSimpleName()) : codeModel.ref(clazz);
  }

  public void resetCodeModel() {
    this.codeModel = new JCodeModel();
  }

  public List<String> generateCode() throws IOException {
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    final PrintStream ps = new PrintStream(baos);

    codeModel.build(configuration.getOutputDirectory(), ps);
    ps.close();
    return Arrays.asList(StringUtils.split(baos.toString()));
  }
}