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

import java.io.File;
import java.net.URI;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 *
 * @author emartino
 */
@Mojo(name = "makepom", defaultPhase = LifecyclePhase.NONE)
public class IvyBridgeMakePomMojo extends AbstractIvyBridgeMojo {

    @Parameter(property = "ivyfile", defaultValue = "ivy.xml")
    URI ivyfile;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        IvyBridgeOptions options = IvyBridgeOptions.newOptionsFromUri(ivyrepo);
        MavenRepositoryProxy proxy = new MavenRepositoryProxy(options);
        URI pom = proxy.makePom(ivyfile);
        
    }

}
