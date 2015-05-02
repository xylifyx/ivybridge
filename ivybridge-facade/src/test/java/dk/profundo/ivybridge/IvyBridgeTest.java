/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import static dk.profundo.ivybridge.IvyTestRepo.opts;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

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

}
