/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package APalladiumclinicws;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

/**
 *
 * @author neptune
 */
/*Class to initialise and start Jetty Server*/
public class JettyServer {

    Server jettyServer;

    /*Size in Bytes*/
    private static final int outputBufferSize = 65536;
    private static final int requestHeaderSize = 16384;
    private static final int responseHeaderSize = 16384;

    public void startServer() {
        try {

            int port = 9090;

            jettyServer = new Server(new QueuedThreadPool(2000));

            jettyServer.setDumpAfterStart(false);
            jettyServer.setDumpBeforeStop(false);
            jettyServer.setStopAtShutdown(true);

            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setSecurePort(port);
            http_config.setOutputBufferSize(outputBufferSize);
            http_config.setRequestHeaderSize(requestHeaderSize);
            http_config.setResponseHeaderSize(responseHeaderSize);
            http_config.setSendServerVersion(false);
            http_config.setSendDateHeader(false);

            ServerConnector httpConnector = new ServerConnector(jettyServer, new HttpConnectionFactory(http_config));
            httpConnector.setIdleTimeout(2 * 60000);
            httpConnector.setPort(port);
            httpConnector.setReuseAddress(true);
            jettyServer.addConnector(httpConnector);

            // Create Handler Colletion
            ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
            // Create Context Handlers
            ContextHandler palladiumRequestHandler = new ContextHandler();
            palladiumRequestHandler.setContextPath("/palladiumWS");
            palladiumRequestHandler.setHandler(new RequestsHandler());
            // Populate All Handlers
            Handler[] handlers = {palladiumRequestHandler};
            // Add Handlers to handler collection
            contextHandlerCollection.setHandlers(handlers);
            // Add all handlers to Server
            jettyServer.setHandler(contextHandlerCollection);

            jettyServer.start();
            Logger.doPrint("Server", "Server Started");
            //jettyServer.dumpStdErr();
//            jettyServer.join();
//            Logger.doPrint("Server", "Server Joined");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            jettyServer.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] po) {
        JettyServer server = new JettyServer();
        server.startServer();
    }

}
