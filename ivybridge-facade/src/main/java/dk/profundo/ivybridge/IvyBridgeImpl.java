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
import java.util.List;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.cache.DefaultResolutionCacheManager;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
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
import org.apache.ivy.plugins.namespace.MRIDTransformationRule;
import org.apache.ivy.plugins.namespace.Namespace;
import org.apache.ivy.plugins.namespace.NamespaceRule;
import org.apache.ivy.plugins.namespace.NamespaceTransformer;
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
    private Ivy ivy;

    public IvyBridgeImpl() {

    }

    public IvyBridgeImpl(IvyBridgeOptions options) {
        super();
        this.options = options;
    }

    @Override
    public Ivy getIvy() {
        if (ivy == null) {
            ivy = getIvy(getOptions());
        }
        return ivy;
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
        } else {
            IvySettings settings = new IvySettings();
            try {
                settings.load(new URL(ivysettings));
            } catch (MalformedURLException ex) {
                throw new IllegalArgumentException("ivysettings must be an url", ex);
            } catch (ParseException ex) {
                throw new IllegalArgumentException("cannot parse ivysettings", ex);
            } catch (IOException ex) {
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

//        Namespace ns = new Namespace() {
//
//            @Override
//            public NamespaceTransformer getToSystemTransformer() {
//                return new NamespaceTransformer() {
//                    @Override
//                    public ModuleRevisionId transform(ModuleRevisionId mrid) {
//                        if (mrid == null) {
//                            return null;
//                        }
//                        if (mrid.getRevision().equals("latest.integration")) {
//                            return ModuleRevisionId.newInstance(mrid, "${project.version}");
//                        } else {
//                            return mrid;
//                        }
//                    }
//
//                    @Override
//                    public boolean isIdentity() {
//                        return false;
//                    }
//                };
//            }
//
//        };

        TransformationRules tr = new TransformationRules(this.getOptions());
        
        final ModuleDescriptor tmd = DefaultModuleDescriptor.transformInstance(md, tr.getNamespace());

        File tmpFile = File.createTempFile("pom", ".xml");
        PomModuleDescriptorWriter.write(tmd, tmpFile, pomWriterOptions());
        return tmpFile.toURI();
    }

    @Override
    public byte[] getPomContent(final String organisation, final String name,
        final String revision, final String branch, final String depConf)
        throws ParseException, IOException {
        Ivy ivy = getIvy();
        IvyBridgeOptions opts = this.getOptions();
        ivy.pushContext();
        try {
            ResolveReport resolveReport = getResolveReport(organisation, name,
                revision, branch, depConf);

            @SuppressWarnings("unchecked")
            final List<IvyNode> dependencies = resolveReport.getDependencies();
            for (IvyNode n : dependencies) {
                final ResolvedModuleRevision moduleRevision = n
                    .getModuleRevision();
                final ModuleDescriptor md = moduleRevision.getDescriptor();
                File tmpFile = File.createTempFile("pom", ".xml");
                final PomWriterOptions pomWriterOptions = pomWriterOptions();
                PomModuleDescriptorWriter.write(md, tmpFile, pomWriterOptions);
                byte[] pomFileContent = Files.readAllBytes(Paths.get(tmpFile
                    .toURI()));
                tmpFile.delete();
                return pomFileContent;
            }

            return null;
        } finally {
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
            ro.setConfs(new String[]{depConf});
            ResolveReport resolve = ivy.resolve(ModuleRevisionId.newInstance(
                organisation, name, branch, revision), ro, true);

            @SuppressWarnings("unchecked")
            List<Artifact> artifacts = resolve.getArtifacts();
            artifacts = new ArrayList<>(artifacts);

            for (Artifact a : artifacts) {

                if (type != null && !a.getType().equals(type)) {
                    continue;
                }
                if (ext != null && !a.getExt().equals(ext)) {
                    continue;
                }

                ArtifactDownloadReport download = ivy.getResolveEngine()
                    .download(a, new DownloadOptions());
                if (download.getDownloadStatus() == DownloadStatus.FAILED) {
                    throw new FileNotFoundException(a.toString() + ": "
                        + download.getDownloadDetails());
                }
                File localFile = download.getLocalFile();
                return localFile.toURI();
            }
            return null;
        } finally {
            ivy.popContext();
        }
    }

    protected ResolveReport getResolveReport(final String organisation,
        final String name, final String revision, final String branch,
        final String depConf) throws ParseException, IOException {
        // 1st create an ivy module (this always(!) has a "default"
        // configuration already)
        DefaultModuleDescriptor envelope = DefaultModuleDescriptor
            .newDefaultInstance(
                // give it some related name (so it can be cached)
                ModuleRevisionId.newInstance(organisation, name + "-envelope",
                    revision));

        final ModuleRevisionId moduleId = ModuleRevisionId.newInstance(
            organisation, name, branch, revision, null, true);

        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(
            envelope, moduleId, false, false, false);

        dd.addDependencyConfiguration("default", depConf);
        envelope.addDependency(dd);

        ResolveOptions resopts = new ResolveOptions();
        resopts.setTransitive(true);
        resopts.setDownload(false);

        ResolveReport resolveReport = getIvy().resolve(envelope, resopts);

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
        this.ivy = ivy;
    }
}
