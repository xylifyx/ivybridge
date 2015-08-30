/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.profundo.ivybridge;

/**
 *
 * @author martino
 */
public class MavenArtifactId {
    
    String groupId;
    String artifactId;
    String classifier;
    String type;
    String version;
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getArtifactId() {
        return artifactId;
    }
    
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
    
    public String getClassifier() {
        return classifier;
    }
    
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + "groupId=" + groupId + ", artifactId=" + artifactId + ", classifier=" + classifier
                + ", type="
                + type
                + ", version=" + version + '}';
    }
    
}
