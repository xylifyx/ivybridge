/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import java.beans.IntrospectionException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author emartino
 *
 */
public class IvyBridgeOptions {

    private String ivysettings;
    private String artroot;
    private String artpattern;
    private String ivyroot;
    private String ivypattern;
    private String cacheBasedir;
    private Map<String, String> configurationScope;
    private String branch;
    private Map<String, String> classifierConfiguration;
    private Map<String, String> extConfiguration;
    private Map<String, String> rule;

    public String getArtroot() {
        return artroot;
    }

    public void setArtroot(String artroot) {
        this.artroot = artroot;
    }

    public String getArtpattern() {
        return artpattern;
    }

    public void setArtpattern(String artpattern) {
        this.artpattern = artpattern;
    }

    public String getIvyroot() {
        return ivyroot;
    }

    public void setIvyroot(String ivyroot) {
        this.ivyroot = ivyroot;
    }

    public String getIvypattern() {
        return ivypattern;
    }

    public void setIvypattern(String ivypattern) {
        this.ivypattern = ivypattern;
    }

    public String getCacheBasedir() {
        return cacheBasedir;
    }

    public void setCacheBasedir(String cacheBasedir) {
        this.cacheBasedir = cacheBasedir;
    }

    public Map<String, String> getConfscope() {
        if (configurationScope == null) {
            Map<String, String> m = configurationScope = new TreeMap<>();
            m.put("compile", "compile");
            m.put("runtime", "runtime");
            m.put("provided", "provided");
            m.put("test", "test");
            m.put("system", "system");
        }
        return configurationScope;
    }

    public Map<String, String> getClassifierConfiguration() {
        if (classifierConfiguration == null) {
            classifierConfiguration = new TreeMap<>();
        }
        return classifierConfiguration;
    }
    
    public Map<String, String> getRule() {
        if (rule == null) {
            rule = new TreeMap<>();
        }
        return rule;
    }

    public Map<String, String> getExtConfiguration() {
        if (extConfiguration == null) {
            extConfiguration = new TreeMap<>();
        }
        return extConfiguration;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getIvysettings() {
        return ivysettings;
    }

    public void setIvysettings(String ivysettings) {
        this.ivysettings = ivysettings;
    }

    public static IvyBridgeOptions newOptionsFromUri(String uriString) throws URISyntaxException, IntrospectionException, IllegalArgumentException {
        URI uri = new URI(uriString);
        String rawQuery = uri.getRawQuery();
        Map<String, String> parameters = BeanUriParameters.parseParameters(rawQuery);
        URI rootUri = new URI(uri.getScheme(),
            uri.getUserInfo(), uri.getHost(), uri.getPort(),
            uri.getPath(), null, null);
        IvyBridgeOptions opts = new IvyBridgeOptions();
        BeanUriParameters ps = new BeanUriParameters(opts);
        ps.fromParameters(parameters);
        String root = rootUri.toASCIIString();
        if (opts.getArtroot() == null) {
            opts.setArtroot(root);
        }
        if (opts.getIvyroot() == null) {
            opts.setIvyroot(root);
        }
        return opts;
    }
}
