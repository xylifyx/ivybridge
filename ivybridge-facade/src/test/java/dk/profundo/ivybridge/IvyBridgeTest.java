/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import static dk.profundo.ivybridge.IvyTestRepo.opts;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IvyBridgeTest {
    
    IvyBridge ivyBridge;
    
    @Before
    public void init() {
        ivyBridge = new IvyBridgeImpl(opts());
    }
    
    @Test
    public void testCreatePom() throws Exception {
        String pomContent = new String(ivyBridge.getPomContent("dk.profundo.maven.ivyrepo", "main-module", "I-1.0.0",
                "trunk", "runtime"));
        assertTrue(pomContent.contains("<artifactId>dependent-module</artifactId>"));
        assertTrue(pomContent.contains("<artifactId>junit</artifactId>"));
    }
    
    @Test
    public void testTestScopePom() throws Exception {
        ivyBridge.getOptions().getConfscope().put("test.unit", "test");
        String pomContent = new String(ivyBridge.getPomContent("dk.profundo.maven.ivyrepo", "main-module", "I-1.0.0",
                "trunk", "runtime"));
        assertTrue(pomContent.contains("<scope>test</scope>"));
    }
    
    @Test
    public void testGetBaseJar() throws Exception {
        URI artifact = ivyBridge.getArtifact("dk.profundo.maven.ivyrepo", "main-module", "I-1.0.0", "trunk",
                "base", "jar", "jar");
        assertNotNull(artifact);
        assertTrue(artifact.toASCIIString().endsWith("dk.profundo.maven.ivyrepo/main-module/trunk/jars/main-module-I-1.0.0.jar"));
    }
    
    @Test
    public void testGetCompileJar() throws Exception {
        URI artifact = ivyBridge.getArtifact("dk.profundo.maven.ivyrepo", "main-module", "I-1.0.0", "trunk",
                "compile", "jar", "jar");
        assertNotNull(artifact);
        assertTrue(artifact.toASCIIString().endsWith("dk.profundo.maven.ivyrepo/main-module/trunk/jars/main-module-I-1.0.0.jar"));
    }
    
    @Test
    public void testGetTestJar() throws Exception {
        URI artifact = ivyBridge.getArtifact("dk.profundo.maven.ivyrepo", "main-module", "I-1.0.0", "trunk",
                "test.unit", "jar", "jar");
        assertNotNull(artifact);
        String uri = artifact.toASCIIString();
        assertTrue(uri, uri.endsWith(
                "dk.profundo.maven.ivyrepo/main-module/trunk/jars/main-module-testutil-I-1.0.0.jar"));
    }
    
    @Test
    public void makePom() throws Exception {
        final IvyBridgeOptions opts = opts();
        opts.getToivy().put("1", "rev:I-1.0.0=1.0.0-SNAPSHOT");
        opts.getFromivy().put("1", "rev:1.0.0-SNAPSHOT=I-1.0.0");
        
        opts.getToivy().put("2", "rev:1.0.0-SNAPSHOT=I-1.0.0");
        opts.getFromivy().put("2", "rev:I-1.0.0=1.0.0-SNAPSHOT");
        ivyBridge = new IvyBridgeImpl(opts);
        byte[] pomContent = ivyBridge.getPomContent("dk.profundo.maven.ivyrepo", "main-module", "1.0.0-SNAPSHOT", "trunk",
                "runtime");
        System.out.println(new String(pomContent));
        
        MavenXpp3Reader mavenReader = new MavenXpp3Reader();
        Model model = mavenReader.read(new ByteArrayInputStream(pomContent));
        Assert.assertEquals("1.0.0-SNAPSHOT", model.getVersion());
        
        List<Dependency> dependencies = model.getDependencies();
        assertEquals(2,dependencies.size());
        
        Map<String,Dependency> deps = new TreeMap<String,Dependency>();
        for(Dependency d : dependencies) {
            deps.put(d.getArtifactId(), d);
        }
        
        assertEquals(deps.get("dependent-module").getVersion(),"1.0.0-SNAPSHOT");
        assertEquals(deps.get("junit").getScope(),"test");
        
    }
}
