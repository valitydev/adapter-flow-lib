package dev.vality.adapter.flow.lib.servlet;

import dev.vality.damsel.proxy_provider.ProviderProxySrv;
import dev.vality.woody.thrift.impl.http.THServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/adapter/${service.name}")
public class AdapterServlet extends GenericServlet {

    @Autowired
    private ProviderProxySrv.Iface serverHandlerLogDecorator;

    private Servlet servlet;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        servlet = new THServiceBuilder().build(ProviderProxySrv.Iface.class, serverHandlerLogDecorator);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        servlet.service(request, response);
    }

}

