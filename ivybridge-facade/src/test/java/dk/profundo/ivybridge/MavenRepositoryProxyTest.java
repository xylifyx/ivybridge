/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.junit.Before;
import org.junit.Test;

/**
 * @author emartino
 *        
 */
public class MavenRepositoryProxyTest {
    MavenRepositoryProxy proxy;
    
    @Before
    public void init() {
        
        proxy = new MavenRepositoryProxy(IvyTestRepo.opts());
    }
    
    @Test
    public void resolveJar() throws ParseException, IOException, URISyntaxException {
        String repositoryPath = "dk.profundo.maven.ivyrepo/main-module/I-1.0.0/main-module-I-1.0.0.jar";
        URI resolveArtifact = proxy.resolveArtifact(repositoryPath);
        String uri = resolveArtifact.toASCIIString();
        assertTrue(uri.endsWith("dk.profundo.maven.ivyrepo/main-module/trunk/jars/main-module-I-1.0.0.jar"));
    }
    
    @Test
    public void resolvePom() throws ParseException, IOException, URISyntaxException {
        String repositoryPath = "dk.profundo.maven.ivyrepo/main-module/I-1.0.0/main-module-I-1.0.0.pom";
        URI resolveArtifact = proxy.resolveArtifact(repositoryPath);
        String uri = resolveArtifact.toASCIIString();
        String pom = new String(DataUri.fromURI(uri));
        System.out.println(pom);
    }
}
