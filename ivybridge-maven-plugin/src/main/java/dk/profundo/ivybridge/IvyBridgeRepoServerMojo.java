package dk.profundo.ivybridge;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

@Mojo(name = "repo-server", defaultPhase = LifecyclePhase.NONE)
public class IvyBridgeRepoServerMojo
    extends AbstractMojo {

    /**
     * The port number of the Maven repository
     */
    @Parameter(property = "port", defaultValue = "8159")
    private int port;

    @Parameter(property = "ivyrepo", required = true)
    private String ivyrepo;

    HttpServer server;
    private Thread mainThread;

    /* 
     * mvn dk.profundo.ivybridge:ivybridge-maven-plugin:LATEST:repo-server
     */
    @Override
    public void execute()
        throws MojoExecutionException {

        try {

            server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), port), port);
            server.createContext("/", new HttpHandler() {

                @Override
                public void handle(HttpExchange he) throws IOException {
                    he.getResponseHeaders().add("Content-Type", "text/html;charset=utf8");
                    final OutputStream out = he.getResponseBody();
                    final byte[] response = ("<html><body>"
                        + "<h1>IvyBridge Repository Frontend</h1>"
                        + "<a href='repo'>repo</a>"
                        + "<hr>"
                        + "").getBytes("UTF-8");
                    he.sendResponseHeaders(200, response.length);
                    out.write(response);
                    out.close();
                    he.close();
                }
            });
            server.createContext("/stop", new HttpHandler() {

                @Override
                public void handle(HttpExchange he) throws IOException {
                    he.getResponseHeaders().add("Content-Type", "text/html;charset=utf8");
                    final OutputStream out = he.getResponseBody();
                    final byte[] response = ("<html><body>"
                        + "<h1>IvyBridge Repository Frontend</h1>"
                        + "<p>Stopping server ...</p>"
                        + "").getBytes("UTF-8");
                    he.sendResponseHeaders(200, response.length);
                    out.write(response);
                    out.close();
                    he.close();
                    stop();
                }

            });
            server.createContext("/repo", new HttpHandler() {

                @Override
                public void handle(HttpExchange he) throws IOException {
                    try {
                        serve(he);
                    } catch (Exception ex) {
                        getLog().error(ex);
                        sendNotFound(he);
                    }
                }

            });
            server.start();
            getLog().info("Started at port: " + port);

            mainThread = Thread.currentThread();

            Thread.sleep(Long.MAX_VALUE);
        } catch (UnknownHostException ex) {
            throw new MojoExecutionException("unknown host", ex);
        } catch (IOException ex) {
            throw new MojoExecutionException("open server", ex);
        } catch (InterruptedException ex) {
            stop();
        }
    }

    void serve(HttpExchange exchange) throws URISyntaxException, IntrospectionException, ParseException, IOException {
        String name = exchange.getRequestURI().getPath();
        if (name.startsWith("/repo/")) {
            name = name.substring("/repo/".length());
        }

        final IvyBridgeOptions options = IvyBridgeOptions.newOptionsFromUri(ivyrepo);
        MavenRepositoryProxy proxy = new MavenRepositoryProxy(options);
        final URI artifactUri = proxy.resolveArtifact(name);

        if (artifactUri == null) {
            sendNotFound(exchange);
            return;
        }

        final long fileLength = proxy.fileLength(artifactUri);
        exchange.sendResponseHeaders(200, fileLength);
        if (name.endsWith(".pom")) {
            exchange.getResponseHeaders().add("Content-Type", "application/xml;charset=utf8");
        } else {
            exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
        }

        IOUtil.copy(proxy.toInputStream(artifactUri), exchange.getResponseBody());

        exchange.getResponseBody().close();
        exchange.close();
    }

    private void sendNotFound(HttpExchange exchange) throws IOException, UnsupportedEncodingException {
        byte[] errorMessage = "<html><body><h1>Artifact not found</h1><p>name</p>".getBytes("UTF-8");
        exchange.getResponseHeaders().add("Content-Type", "text/html;charset=utf8");
        exchange.sendResponseHeaders(404, errorMessage.length);
        final OutputStream out = exchange.getResponseBody();
        out.write(errorMessage);
        out.close();
        exchange.close();
    }

    public void stop() {
        server.stop(0);
        mainThread.interrupt();
    }
}
