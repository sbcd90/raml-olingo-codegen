package org.apache.olingo;

import org.apache.olingo.resource.CommonEntityCollectionProcessor;
import org.apache.olingo.resource.CommonEntityProcessor;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

public class MainTemplateServlet extends HttpServlet {

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    OData oData = OData.newInstance();
    ServiceMetadata serviceMetadata = oData.createServiceMetadata(new EdmProvider(), new ArrayList<>());
    ODataHttpHandler handler = oData.createHandler(serviceMetadata);
    handler.register(new CommonEntityCollectionProcessor());
    handler.register(new CommonEntityProcessor());

    handler.process(req, resp);
  }
}