@echo off

if exist ..\..\raml-olingo-codegen-core\target\raml-olingo-codegen-core-1.0-SNAPSHOT-jar-with-dependencies.jar (
  java -cp ..\..\raml-olingo-codegen-core\target\raml-olingo-codegen-core-1.0-SNAPSHOT-jar-with-dependencies.jar org.raml.olingo.codegen.core.Launcher -basePackageName org.raml.olingo -outputDirectory output -sourceDirectory raml
) else (
  echo *** run mvn assembly:assembly -DdescriptorId=jar-with-dependencies
  echo *** in raml-olingo-codegen-core first to make jar
)