/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import java.io.File;
import java.net.URI;

public class IvyTestRepo {
    public static IvyBridgeOptions opts() {
        String root = repositoryRoot();
        IvyBridgeOptions opts = new IvyBridgeOptions();
        opts.setArtpattern("([branch]/)[organisation]/[module]/[revision]/[type]/[artifact]-[revision].[ext]");
        opts.setArtroot(root);
        opts.setIvypattern("([branch]/)[organisation]/[module]/[revision]/[type]/ivy-[revision].xml");
        opts.setIvyroot(root);
        opts.getConfscope().put("test.unit", "test");
        opts.setBranch("trunk");
        
        return opts;
    }
    
    public static String repositoryRoot() {
        URI uri = new File("target/test-classes/ivyrepo").toURI();
        String root = uri.toASCIIString();
        return root;
    }
}