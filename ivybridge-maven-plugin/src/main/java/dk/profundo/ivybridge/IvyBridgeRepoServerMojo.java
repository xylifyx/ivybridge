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
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "repo-server", defaultPhase = LifecyclePhase.NONE)
public class IvyBridgeRepoServerMojo
    extends AbstractMojo {

    /**
     * The port number of the Maven repository
     */
    @Parameter(property = "port", defaultValue = "8159")
    private int port;

    /* 
     * mvn dk.profundo.ivybridge:ivybridge-maven-plugin:LATEST:repo-server
     */
    @Override
    public void execute()
        throws MojoExecutionException {

        try {

            final HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), port), port);
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
                    out.flush();
                }
            });

            server.createContext("/repo", new HttpHandler() {

                @Override
                public void handle(HttpExchange he) throws IOException {
                    he.getResponseHeaders().add("Content-Type", "text/html;charset=utf8");
                    final OutputStream out = he.getResponseBody();
                    final byte[] response = "<html><body><h1>IvyBridge Repository</h1>".getBytes("UTF-8");
                    he.sendResponseHeaders(200, response.length);
                    out.write(response);
                    out.flush();
                }

            });
            server.start();
            getLog().info("Started at port: " + port);

            Thread.sleep(Long.MAX_VALUE);
            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    getLog().info("Stopping server");
                    server.stop(0);
                }

            });
        } catch (UnknownHostException ex) {
            throw new MojoExecutionException("unknown host", ex);
        } catch (IOException ex) {
            throw new MojoExecutionException("open server", ex);
        } catch (InterruptedException ex) {
            getLog().info("Interrupted");
        }
    }
}
