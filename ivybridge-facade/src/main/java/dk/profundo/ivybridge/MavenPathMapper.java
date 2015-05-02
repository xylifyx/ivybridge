/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author emartino
 *
 */
public class MavenPathMapper {
    public static MavenArtifactId parseResourcePath(String resourcePath) {
        String regexp = "(.+)/([^/]+)/([^/]+)/([^/]+)\\.([^/-]+)";
        final Pattern pt = Pattern.compile(regexp);
        final Matcher m = pt.matcher(resourcePath);
        if (!m.matches()) {
            throw new IllegalArgumentException("resource path not understood");
        }
        MavenArtifactId a = new MavenArtifactId();
        a.groupId = m.group(1).replace("/", ".");
        a.artifactId = m.group(2);
        a.version = m.group(3);
        a.type = m.group(5);
        String fileBase = m.group(4);
        String noClassifierPrefix = a.artifactId + "-" + a.version;
        String classifierPrefix = noClassifierPrefix + "-";
        if (fileBase.startsWith(classifierPrefix)) {
            a.classifier = fileBase.substring(classifierPrefix.length());
        }
        else if (fileBase.equals(noClassifierPrefix)) {
            a.classifier = null;
        }
        else {
            throw new IllegalArgumentException("resource path not decodable");
        }
        return a;
    }
}
