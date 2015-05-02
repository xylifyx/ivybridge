/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

/**
 * @author emartino
 *
 */
public class MavenRepositoryProxy {
    IvyBridgeOptions options;
    private IvyBridge ivyBridge;
    
    public MavenRepositoryProxy(IvyBridgeOptions options) {
        super();
        this.options = options;
    }
    
    public URI resolveArtifact(String repositoryPath) throws ParseException, IOException, URISyntaxException {
        return resolveArtifact(MavenPathMapper.parseResourcePath(repositoryPath));
    }
    
    protected URI resolveArtifact(MavenArtifactId art) throws ParseException, IOException, URISyntaxException {
        String name = art.getArtifactId();
        String organisation = art.getGroupId();
        String ext = art.getType();
        String depConf = configurationOfArtifact(art);
        String revision = art.getVersion();
        String branch = options.getBranch();
        
        if (ext.equals("pom")) {
            byte[] pom = getIvyBridge().getPomContent(organisation, name, revision, branch, depConf);
            return new URI(DataUri.toURI(pom));
        }
        else {
            return getIvyBridge().getArtifact(organisation, name, revision, branch, depConf, null, ext);
        }
    }
    
    /**
     * @param opts
     * @param art
     * @return
     */
    private String configurationOfArtifact(MavenArtifactId art) {
        if (art.getClassifier() != null && options.getClassifierConfiguration().containsKey(art.getClassifier())) {
            return options.getClassifierConfiguration().get(art.getClassifier());
        }
        else if (art.getType() != null && options.getExtConfiguration().containsKey(art.getType())) {
            return options.getExtConfiguration().get(art.getType());
        }
        else {
            return "compile";
        }
    }
    
    public IvyBridge getIvyBridge() {
        if (ivyBridge == null) {
            ivyBridge = new IvyBridgeImpl(options);
        }
        return ivyBridge;
    }
    
    public void setIvyBridge(IvyBridge ivyBridge) {
        this.ivyBridge = ivyBridge;
    }
}
