/*
 * Copyright (C) 2015 emartino.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package dk.profundo.ivybridge;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

/**
 * Creates a pom.xml file based on an ivy file. Example
 * <code>mvn -Dpomtemplate=../pom.template dk.profundo.ivybridge:ivybridge-maven-plugin:LATEST:makepom -Divyrepo='ivybridge?ivysettings=file:/home/coder/development/ivy/ivysettings-development.xml&confscope.test.unit=test&rule.def=rev:latest.integration=1.0-SNAPSHOT'</code>
 */
@Mojo(name = "makepom", defaultPhase = LifecyclePhase.NONE, requiresProject = false)
public class IvyBridgeMakePomMojo extends AbstractIvyBridgeMojo {

    // mvn dk.profundo.ivybridge:ivybridge-maven-plugin:LATEST:makepom

    @Parameter(property = "ivyfile", defaultValue = "ivy.xml")
    String ivyfile;

    @Parameter(property = "pomfile", defaultValue = "pom.xml")
    String pomfile;
    
    @Parameter(property = "ivysettings", required=false)
    String ivysettings;
    
    @Parameter(property = "pomtemplate")
    String template;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            IvyBridgeOptions options = IvyBridgeOptions.newOptionsFromUri(ivyrepo);
            if (template != null) {
                options.setPomtemplate(template);
            }
            if (ivysettings != null) {
            	options.setIvysettings(ivysettings);
            }
            IvyBridge bridge = new IvyBridgeImpl(options);
            final URI ivf = Paths.get(ivyfile).toAbsolutePath().toUri();
            //MavenRepositoryProxy proxy = new MavenRepositoryProxy(options);
            URI pom = bridge.makePom(ivf);

            IOUtil.copy(pom.toURL().openStream(), new FileOutputStream(new File(pomfile)));
        } catch (URISyntaxException | IntrospectionException | IllegalArgumentException | ParseException | IOException ex) {
            throw new MojoFailureException("makepom failed", ex);
        }

    }

}
