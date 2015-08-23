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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.ivy.Ivy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.IOUtil;

/**
 * Extracts the default pom.template file from ivy
 */
@Mojo(name = "get-pomtemplate", defaultPhase = LifecyclePhase.NONE, requiresProject = false)
public class IvyBridgeGetPomTemplate extends AbstractMojo {
    @Parameter(property = "templatefile", defaultValue = "pom.template")
    String pomtemplate;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            URL pom = Ivy.class.getResource("/org/apache/ivy/plugins/parser/m2/pom.template");
            IOUtil.copy(pom.openStream(), new FileOutputStream(new File(pomtemplate)));
        } catch (IOException ex) {
            Logger.getLogger(IvyBridgeGetPomTemplate.class.getName()).log(Level.SEVERE, null, ex);
            throw new MojoExecutionException("get-pomtemplate");
        }
    }

}
