/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.profundo.ivybridge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.DefaultResolutionCacheManager;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.DownloadStatus;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.IvyNode;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.resolve.ResolvedModuleRevision;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.namespace.Namespace;
import org.apache.ivy.plugins.parser.m2.PomModuleDescriptorWriter;
import org.apache.ivy.plugins.parser.m2.PomWriterOptions;
import org.apache.ivy.plugins.parser.m2.PomWriterOptions.ConfigurationScopeMapping;
import org.apache.ivy.plugins.resolver.IvyRepResolver;

/**
 *
 * @author emartino
 */
public class IvyBridgeImpl implements IvyBridge {
    
    private IvyBridgeOptions options;
    private Ivy _ivy;
    
    public IvyBridgeImpl() {
    
    }
    
    public IvyBridgeImpl(IvyBridgeOptions options) {
        super();
        this.options = options;
    }
    
    @Override
    public Ivy getIvy() {
        if (_ivy == null) {
            _ivy = getIvy(getOptions());
        }
        return _ivy;
    }
    
    private static Ivy getIvy(IvyBridgeOptions o) {
        return getIvy(o.getArtroot(), o.getArtpattern(), o.getIvyroot(),
                o.getIvypattern(), o.getCacheBasedir(), o.getIvysettings());
    }
    
    private static Ivy getIvy(String artroot, String artpattern,
            String ivyroot, String ivypattern, String cacheBasedir, String ivysettings) {
        final Ivy ivy;
        
        if (ivysettings == null) {
            ivy = Ivy.newInstance(null);
            final IvyRepResolver r = new IvyRepResolver();
            r.setName("ivy-resolver");
            if (artroot != null) {
                r.setArtroot(artroot);
            }
            if (artpattern != null) {
                r.setArtpattern(artpattern);
            }
            if (ivyroot != null) {
                r.setIvyroot(ivyroot);
            }
            if (ivypattern != null) {
                r.setIvypattern(ivypattern);
            }
            ivy.getSettings().addResolver(r);
            ivy.getSettings().setDefaultResolver("ivy-resolver");
        }
        else {
            IvySettings settings = new IvySettings();
            try {
                settings.load(new URL(ivysettings));
            }
            catch (MalformedURLException ex) {
                throw new IllegalArgumentException("ivysettings must be an url", ex);
            }
            catch (ParseException ex) {
                throw new IllegalArgumentException("cannot parse ivysettings", ex);
            }
            catch (IOException ex) {
                throw new IllegalArgumentException("cannot load ivysettings", ex);
            }
            ivy = Ivy.newInstance(settings);
        }
        
        if (cacheBasedir != null) {
            ResolutionCacheManager resolutionCacheManager = ivy
                    .getResolutionCacheManager();
            if (resolutionCacheManager instanceof DefaultResolutionCacheManager) {
                DefaultResolutionCacheManager cm = (DefaultResolutionCacheManager) resolutionCacheManager;
                cm.setBasedir(new File(cacheBasedir));
            }
        }
        return ivy;
    }
    
    @Override
    public URI makePom(URI ivyFile) throws ParseException, IOException {
        URL ivyXml = ivyFile.toURL();
        
        ResolveReport resolve = getIvy().resolve(ivyXml);
        final ModuleDescriptor md = resolve.getModuleDescriptor();
        URI pomURI = makePom(md);
        return pomURI;
    }
    
    public URI makePom(ModuleDescriptor md) throws IOException {
        TransformationRules tr = new TransformationRules(this.getOptions());
        final Namespace namespace = tr.getNamespace();
        
        final ModuleDescriptor tmd = DefaultModuleDescriptor.transformInstance(md, namespace);
        
        File tmpFile = File.createTempFile("pom", ".xml");
        
        String artifactPackaging = getPackaging(tmd);
        
        PomWriterOptions pomWriterOptions = pomWriterOptions();
        pomWriterOptions.setArtifactPackaging(artifactPackaging);
        PomModuleDescriptorWriter.write(tmd, tmpFile, pomWriterOptions);
        return tmpFile.toURI();
    }

    /**
     * @param tmd
     * @return
     */
    private String getPackaging(final ModuleDescriptor tmd) {
        String artifactPackaging="jar";
        Artifact[] allArtifacts = tmd.getAllArtifacts();
        for(Artifact a : allArtifacts) {
            if (a.getType().equals("jar") && a.getExt().equals("jar")) {
                artifactPackaging = "jar";
                break;
            } else if (a.getType().equals("war") && a.getExt().equals("war")) {
                artifactPackaging = "war";
                break;
            }
        }
        return artifactPackaging;
    }
    
    @Override
    public byte[] getPomContent(final String organisation, final String name,
            final String revision, final String branch, final String depConf)
                    throws ParseException, IOException {
        Ivy ivy = getIvy();
        ivy.pushContext();
        try {
            System.out.println("org: " + organisation + " name: " + name + " rev: " + revision);
            
            ResolveReport resolveReport = getResolveReport(organisation, name,
                    revision, branch, depConf);
                    
            @SuppressWarnings("unchecked")
            final List<IvyNode> dependencies = resolveReport.getDependencies();
            for (IvyNode n : dependencies) {
                final ResolvedModuleRevision moduleRevision = n
                        .getModuleRevision();
                final ModuleDescriptor md = moduleRevision.getDescriptor();
                
                URI tmpFile = makePom(md);
                byte[] pomFileContent = Files.readAllBytes(Paths.get(tmpFile));
                Paths.get(tmpFile).toFile().delete();
                return pomFileContent;
            }
            
            return null;
        }
        finally {
            ivy.popContext();
        }
    }
    
    private PomWriterOptions pomWriterOptions() {
        PomWriterOptions pop = new PomWriterOptions();
        if (options != null
                && options.getConfscope().isEmpty() == false) {
            pop.setMapping(new ConfigurationScopeMapping(options
                    .getConfscope()));
        }
        if (options.getPomtemplate() != null) {
            String template = options.getPomtemplate();
            final File file = new File(template);
            if (!file.isFile()) {
                throw new IllegalArgumentException(template);
            }
            pop.setTemplate(file);
        }
        return pop;
    }
    
    @Override
    public URI getArtifact(final String organisation, final String name,
            final String revision, final String branch, final String depConf,
            final String type, final String ext) throws ParseException,
                    IOException {
        Ivy ivy = getIvy();
        ivy.pushContext();
        try {
            ResolveOptions ro = new ResolveOptions();
            
            // nro.setConfs(new String[] { depConf });
            
            ResolvedModuleRevision mod = ivy.findModule(ModuleRevisionId.newInstance(
                    organisation, name, branch, revision));
            @SuppressWarnings("unchecked")
            Artifact[] artifacts = mod.getDescriptor().getAllArtifacts();
            
            List<String> orderedConfigurations = getConfigurationOrder(mod.getDescriptor(), depConf);
            
            Artifact artifact = null;
            int configurationIndex = Integer.MAX_VALUE;
            for (Artifact a : artifacts) {
                
                if (type != null && !a.getType().equals(type)) {
                    continue;
                }
                if (ext != null && !a.getExt().equals(ext)) {
                    continue;
                }
                
                String[] configurations = a.getConfigurations();
                int confIndex = Integer.MAX_VALUE;
                for (String c : configurations) {
                    int idx = orderedConfigurations.indexOf(c);
                    if (idx >= 0)
                        confIndex = Math.min(confIndex, idx);
                }
                
                if (confIndex < configurationIndex) {
                    artifact = a;
                    configurationIndex = confIndex;
                }
                
            }
            if (artifact == null)
                return null;
            ArtifactDownloadReport download = ivy.getResolveEngine()
                    .download(artifact, new DownloadOptions());
            if (download.getDownloadStatus() == DownloadStatus.FAILED) {
                throw new FileNotFoundException(artifact.toString() + ": "
                        + download.getDownloadDetails());
            }
            File localFile = download.getLocalFile();
            return localFile.toURI();
        }
        finally {
            ivy.popContext();
        }
    }
    
    /**
     * @param conforder
     * @param moduleDescriptor
     * @param configurationName
     * @return
     */
    private List<String> getConfigurationOrder(ModuleDescriptor moduleDescriptor, String configurationName) {
        int i = 0;
        ArrayList<String> orderedConfigurations = new ArrayList<>();
        orderedConfigurations.add(configurationName);
        while (i < orderedConfigurations.size()) {
            Configuration c = moduleDescriptor.getConfiguration(orderedConfigurations.get(i));
            if (c == null)
                continue;
            String[] ext = c.getExtends();
            if (ext == null)
                continue;
            orderedConfigurations.addAll(Arrays.asList(ext));
            i++;
        }
        return orderedConfigurations;
    }
    
    protected ResolveReport getResolveReport(final String organisation,
            final String name, final String revision, final String branch,
            final String depConf) throws ParseException, IOException {
        // 1st create an ivy module (this always(!) has a "default"
        // configuration already)
        TransformationRules tr = new TransformationRules(this.getOptions());
        
        DefaultModuleDescriptor envelope = DefaultModuleDescriptor
                .newDefaultInstance(
                        // give it some related name (so it can be cached)
                        ModuleRevisionId.newInstance(organisation, name + "-envelope",
                                revision));
                                
        final ModuleRevisionId moduleId = ModuleRevisionId.newInstance(
                organisation, name, branch, revision, null, true);
                
        DefaultDependencyDescriptor md = new DefaultDependencyDescriptor(
                envelope, moduleId, false, false, false);
                
        md.addDependencyConfiguration("default", depConf);
        envelope.addDependency(md);
        
        ResolveOptions resopts = new ResolveOptions();
        resopts.setTransitive(false);
        resopts.setDownload(false);
        
        final Namespace namespace = tr.getNamespace();
        
        final ModuleDescriptor transformedEnvelope = DefaultModuleDescriptor.transformInstance(envelope, namespace);
        
        ResolveReport resolveReport = getIvy().resolve(transformedEnvelope, resopts);
        
        if (resolveReport.hasError()) {
            throw new IllegalArgumentException(resolveReport
                    .getAllProblemMessages().toString());
        }
        return resolveReport;
    }
    
    @Override
    public IvyBridgeOptions getOptions() {
        return options;
    }
    
    @Override
    public void setOptions(IvyBridgeOptions options) {
        this.options = options;
    }
    
    @Override
    public void setIvy(Ivy ivy) {
        this._ivy = ivy;
    }
}
