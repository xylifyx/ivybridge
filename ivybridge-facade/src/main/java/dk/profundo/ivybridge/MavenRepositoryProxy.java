/**
 * Copyright © 2015, QIAGEN Aarhus A/S. All rights reserved.
 */
package dk.profundo.ivybridge;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    
    public InputStream streamResult(String name, String repoUrl) throws IOException {
        URI artifactUri = null;
        try {
            MavenRepositoryProxy proxy = new MavenRepositoryProxy(options);
            artifactUri = proxy.resolveArtifact(name);
            
            if (artifactUri == null) {
                throw new FileNotFoundException(name);
            }
            
            InputStream inputStream = toInputStream(artifactUri);
            
            return inputStream;
        }
        catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Ivy repo info not ok", ex);
        }
        catch (IOException | RuntimeException | ParseException ex) {
            throw new IOException("Problem resolving resource: " + artifactUri, ex);
        }
    }
    
    public long fileLength(final URI artifactUri) {
        switch (artifactUri.getScheme()) {
        case "data":
            byte[] bytes = DataUri.fromURI(artifactUri.toASCIIString());
            return bytes.length;
        case "file":
            final Path get = Paths.get(artifactUri);
            return get.toFile().length();
        default:
            return -1;
        }
    }
    
    public InputStream toInputStream(final URI artifactUri) throws IOException {
        InputStream inputStream;
        if (artifactUri.getScheme().equals("data")) {
            byte[] bytes = DataUri.fromURI(artifactUri.toASCIIString());
            inputStream = new ByteArrayInputStream(bytes);
        }
        else {
            inputStream = artifactUri.toURL().openStream();
        }
        return inputStream;
    }
    
}
