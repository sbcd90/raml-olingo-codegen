package org.raml.olingo.codegen.core.extn;

import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import org.raml.model.Action;
import org.raml.model.MimeType;
import org.raml.model.Raml;
import org.raml.model.Resource;
import org.raml.model.parameter.AbstractParam;

import java.lang.annotation.Annotation;
import java.util.Collection;

public abstract class AbstractGeneratorExtension implements NestedSchemaNameComputer {

  private Raml raml;
  private JCodeModel codeModel;

  @Override
  public void onAddResourceMethod(JMethod method, Action action,
                                  MimeType bodyMimeType, Collection<MimeType> uniqueResponseMimeTypes) {

  }

  @Override
  public String computeNestedSchemaName(MimeType mime) {
    return null;
  }

  @Override
  public void onCreateResourceInterface(JDefinedClass resourceInterface, Resource resource) {

  }

  @Override
  public boolean AddParameterFilter(String name, AbstractParam parameter,
                                    Class<? extends Annotation> annotationClass, JMethod method) {
    return true;
  }

  @Override
  public void setRaml(Raml raml) {
    this.raml = raml;
  }

  public Raml getRaml() {
    return raml;
  }

  @Override
  public void setCodeModel(JCodeModel codeModel) {
    this.codeModel = codeModel;
  }

  public JCodeModel getCodeModel() {
    return codeModel;
  }
}