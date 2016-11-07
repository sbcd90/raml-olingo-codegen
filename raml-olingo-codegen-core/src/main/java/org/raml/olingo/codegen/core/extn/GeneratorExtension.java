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

public interface GeneratorExtension {

  public void onCreateResourceInterface(final JDefinedClass resourceInterface, Resource resource);

  public void onAddResourceMethod(final JMethod method, final Action action,
                                  final MimeType bodyMimeType, final Collection<MimeType> uniqueResponseMimeTypes);

  public boolean AddParameterFilter(final String name, final AbstractParam parameter,
                                    final Class<? extends Annotation> annotationClass, final JMethod method);

  void setRaml(Raml raml);

  void setCodeModel(JCodeModel codeModel);
}