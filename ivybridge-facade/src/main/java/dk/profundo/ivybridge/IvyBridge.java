/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.profundo.ivybridge;

import java.io.IOException;
import java.net.URI;
import java.text.ParseException;

import org.apache.ivy.Ivy;

/**
 *
 * @author emartino
 */
public interface IvyBridge {
    public byte[] getPomContent(final String organisation, final String name,
            final String revision,
            final String branch, final String depConf) throws ParseException, IOException;
            
    public URI getArtifact(final String organisation, final String name, final String revision,
            final String branch,
            final String depConf, final String type, final String ext) throws ParseException, IOException;
            
    public IvyBridgeOptions getOptions();
    
    public void setOptions(IvyBridgeOptions options);
    
    public Ivy getIvy();
    
    public void setIvy(Ivy ivy);
    
    public URI makePom(URI ivyFile) throws ParseException, IOException;
}
