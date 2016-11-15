@echo off

if exist raml-olingo-codegen-core-1.0-SNAPSHOT-jar-with-dependencies.jar (
  java -cp raml-olingo-codegen-core-1.0-SNAPSHOT-jar-with-dependencies.jar org.raml.olingo.codegen.core.Launcher -basePackageName org.apache.olingo -outputDirectory src\main\generated -sourceDirectory src\main\raml -entityCollectionInterfaceNameSuffix EntityCollectionProcessor -entityInterfaceNameSuffix EntityProcessor
) else (
  echo *** run mvn assembly:assembly -DdescriptorId=jar-with-dependencies
  echo *** in raml-olingo-codegen-core first to make jar
)