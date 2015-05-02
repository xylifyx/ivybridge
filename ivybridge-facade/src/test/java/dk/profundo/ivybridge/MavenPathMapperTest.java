/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * @author emartino
 *
 */
public class MavenPathMapperTest {
    
    @Test
    public void testJarWithClassifier() {
        String path = "org/springframework/security/oauth"
                + "/"
                + "spring-security-oauth2"
                + "/"
                + "2.0.4.RELEASE"
                + "/"
                + "spring-security-oauth2-2.0.4.RELEASE"
                + "-"
                + "sources"
                + "."
                + "jar";
        MavenArtifactId aid = MavenPathMapper.parseResourcePath(path);
        assertEquals("org.springframework.security.oauth", aid.getGroupId());
        assertEquals("spring-security-oauth2", aid.getArtifactId());
        assertEquals("2.0.4.RELEASE", aid.getVersion());
        assertEquals("sources", aid.getClassifier());
        assertEquals("jar", aid.getType());
    }
    
    @Test
    public void testJarWithoutClassifier() {
        String path = "org/springframework/security/oauth"
                + "/"
                + "spring-security-oauth2"
                + "/"
                + "2.0.4.RELEASE"
                + "/"
                + "spring-security-oauth2-2.0.4.RELEASE"
                + "."
                + "jar";
        MavenArtifactId aid = MavenPathMapper.parseResourcePath(path);
        assertEquals("org.springframework.security.oauth", aid.getGroupId());
        assertEquals("spring-security-oauth2", aid.getArtifactId());
        assertEquals("2.0.4.RELEASE", aid.getVersion());
        assertNull(aid.getClassifier());
        assertEquals("jar", aid.getType());
    }
    
}
