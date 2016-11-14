raml-olingo-codegen
===================

The `raml-olingo-codegen` project provides a tool to generate a scaffolding for an `Apache Olingo` application from a given set of `Raml` file. 

- [Raml](http://raml.org/) provides an api modelling language which makes it easy to manage the whole api lifecycle from design to sharing. 
- [Apache Olingo](https://olingo.apache.org/) provides a Java library that implements the Open Data Protocol(OData).

## Compatibility

Works with Raml 0.8 & Apache Olingo 4.2+

## Installation & Usage

### Installation from source

- Clone the project from github & build the jars using maven

```
mvn clean install -DskipTests
```

- Use the command line to generate the scaffolding for the `Apache Olingo` application.

```
java -cp raml-olingo-codegen-core-1.0-jar-with-dependencies.jar org.raml.olingo.codegen.core.Launcher -basePackageName com.somecompany.sample -outputDirectory /tmp/output -sourceDirectory /tmp/source
```

## List of supported command line options

- `outputDirectory` - output Directory to put the generated files.

- `basePackageName` - base package name under which generated files are put.

- `jsonMapper` - json mapper for raml schema to pojo mapping.

- `sourceDirectory` - source directory to read the raml files from.

- `methodThrowException` - default exception class to be used in generated methods.

- `entityCollectionInterfaceNameSuffix` - suffix for Entity Collection classes both generated & implemented.

- `entityInterfaceNameSuffix` - suffix for Entity classes both generated & implemented.

- `namespace` - OData namespace.

- `containerName` - OData container name.
 
 