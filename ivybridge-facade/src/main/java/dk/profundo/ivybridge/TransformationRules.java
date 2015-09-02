/*
 * Copyright (C) 2015 emartino.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package dk.profundo.ivybridge;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.plugins.namespace.MRIDRule;
import org.apache.ivy.plugins.namespace.MRIDTransformationRule;
import org.apache.ivy.plugins.namespace.Namespace;
import org.apache.ivy.plugins.namespace.NamespaceRule;
import org.apache.ivy.plugins.namespace.NamespaceTransformer;

/**
 *
 * @author emartino
 */
public final class TransformationRules {
    
    IvyBridgeOptions settings;
    Namespace namespace = new Namespace();
    
    public Namespace getNamespace() {
        return namespace;
    }
    
    public TransformationRules(IvyBridgeOptions settings) {
        this.settings = settings;
        init();
    }
    
    Pattern rulePattern = Pattern.compile("(org|mod|rev):(.*)=(.*)");
    
    protected void init() {
        
        for (Map.Entry<String, String> e : settings.getFromivy().entrySet()) {
            NamespaceRule nsrule = new NamespaceRule();
            final String toSystem = e.getValue();
            MRIDTransformationRule toRule = parseRule(toSystem);
            final String fromSystem = settings.getToivy().get(e.getKey());
            if (fromSystem == null) {
                throw new IllegalArgumentException("mismatching rules: " + e.getKey());
            }
            MRIDTransformationRule fromRule = parseRule(fromSystem);
            
            nsrule.addFromsystem(fromRule);
            nsrule.addTosystem(toRule);
            
            namespace.addRule(nsrule);
        }
        
        Set<String> mismatchingRuleIds = new HashSet<>(settings.getFromivy().keySet());
        mismatchingRuleIds.removeAll(settings.getToivy().keySet());
        
        if (mismatchingRuleIds.isEmpty() == false) {
            throw new IllegalArgumentException("mismatching rules: " + mismatchingRuleIds);
        }
        
        namespace.setName("ivybridge");
    }
    
    private MRIDTransformationRule parseRule(String value) throws IllegalArgumentException {
        String[] cs = value.split(",");
        
        MRIDRule src = new MRIDRule();
        MRIDRule dest = new MRIDRule();
        for (String c : cs) {
            final Matcher matcher = rulePattern.matcher(c);
            if (matcher.matches()) {
                final String component = matcher.group(1);
                final String from = matcher.group(2);
                final String to = matcher.group(3);
                switch (component) {
                case "org":
                    src.setOrg(from);
                    dest.setOrg(to);
                    break;
                case "mod":
                    src.setModule(from);
                    dest.setModule(to);
                    break;
                case "rev":
                    src.setRev(from);
                    dest.setRev(to);
                    break;
                default:
                    throw new IllegalArgumentException();
                }
            }
            else {
                throw new IllegalArgumentException("rule format: org:from=to,mod:from=to,rev:from=to");
            }
        }
        MRIDTransformationRule nstrule = new MRIDTransformationRule();
        nstrule.addSrc(src);
        nstrule.addDest(dest);
        return nstrule;
    }
    
    public static ModuleDescriptor reverseTransformInstance(
            ModuleDescriptor md, final Namespace ns) {
        return DefaultModuleDescriptor.transformInstance(md, new Namespace() {
            
            @Override
            public NamespaceTransformer getToSystemTransformer() {
                return ns.getToSystemTransformer();
            }
            
        });
    }
}
