/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.plugin.provision;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.jboss.eap.glow.metadata.MetadataProvider;
import org.wildfly.channel.ChannelManifest;
import org.wildfly.channel.ChannelSession;
import org.wildfly.channel.MavenArtifact;
import org.wildfly.channel.Stream;
import org.wildfly.glow.Arguments;
import org.wildfly.glow.LayerConfigurationProvider;
import org.wildfly.glow.OutputFormat;
import org.wildfly.glow.ScanArguments.Builder;

/**
 *
 * @author jdenise
 */
@SuppressWarnings("unused")
public class GlowConfig {

    private String context = "bare-metal";
    private String profile;
    private Set<String> addOns = Set.of();
    private boolean suggest;
    private Set<String> layersForJndi = Set.of();
    private Set<String> excludedArchives = Set.of();
    private boolean failsOnError = true;
    private boolean verbose;
    private boolean ignoreDeployment;

    public GlowConfig() {
    }

    public Arguments toArguments(Log log, List<Path> lst, Path inProvisioning, String layersConfigurationFileName,
            ChannelSession session) throws MojoExecutionException, IOException {
        final Set<String> profiles = profile != null ? Set.of(profile) : Set.of();
        List<Path> deployments = ignoreDeployment ? Collections.emptyList() : lst;
        String version = null;
        LayerConfigurationProvider layerConfigurationProvider = null;
        if (inProvisioning == null) {
            if (session == null) {
                throw new MojoExecutionException(
                        "JBoss EAP channel(s) must be configured to discover provisioning information");
            }
            // Retrieve the list of known metadata
            Map<String, MetadataProvider> providers = new HashMap<>();
            List<URL> artifacts = new ArrayList<>();
            for (ChannelManifest manifest : session.getManifests()) {
                for (Stream stream : manifest.getStreams()) {
                    if (stream.getGroupId().equals("org.jboss.eap.glow.metadata")) {
                        MavenArtifact ma = session.resolveMavenArtifact(stream.getGroupId(),
                                stream.getArtifactId(), "jar", null, null);
                        artifacts.add(ma.getFile().toURI().toURL());
                    }
                }
            }
            if (artifacts.isEmpty()) {
                throw new MojoExecutionException(
                        "The configured channels don't support provisioning discovery, missing org.jboss.eap.glow.metadata:* artifacts");
            }
            URL[] urls = new URL[artifacts.size()];
            artifacts.toArray(urls);
            try (URLClassLoader loader = new URLClassLoader(urls, MetadataProvider.class.getClassLoader())) {
                for (MetadataProvider provider : ServiceLoader.load(MetadataProvider.class, loader)) {
                    providers.put(provider.getVersion(), provider);
                    if (version == null) {
                        version = provider.getVersion();
                    } else {
                        if (version.compareTo(provider.getVersion()) < 0) {
                            version = provider.getVersion();
                        }
                    }
                }
                MetadataProvider provider = providers.get(version);
                Path provisioningXML = Files.createTempFile("eap-maven-plugin-glow", "-provisioning.xml");
                provisioningXML.toFile().deleteOnExit();
                try (InputStream stream = provider.getProvisioningFile(null, context, loader)) {
                    if (stream == null) {
                        throw new MojoExecutionException("Didn't find provisioning discovery metadata in " + artifacts);
                    }
                    Files.write(provisioningXML, stream.readAllBytes());
                }
                inProvisioning = provisioningXML;
                layerConfigurationProvider = new LayerConfigurationProvider() {
                    @Override
                    public URI getConfigurationURI(String layerName, String version, Set<String> spaces, String context,
                            String variant, URI uri) {
                        for (String space : spaces) {
                            URI ret = provider.getLayerConfiguration(uri, layerName, space, context, loader);
                            if (ret != null) {
                                return ret;
                            }
                        }
                        return uri;
                    }
                };
            }
        }

        Builder builder = Arguments.scanBuilder().setExecutionContext(context).setExecutionProfiles(profiles)
                .setUserEnabledAddOns(addOns).setBinaries(deployments).setSuggest(suggest).setJndiLayers(getLayersForJndi())
                .setExcludeArchivesFromScan(excludedArchives)
                .setVerbose(verbose)
                // In maven the way to configure build time is to set system properties
                .setPreferSystemProperties(true)
                .setOutput(OutputFormat.PROVISIONING_XML);
        builder.setProvisoningXML(inProvisioning);
        if (layersConfigurationFileName != null) {
            builder.setConfigName(layersConfigurationFileName);
        }
        if (layerConfigurationProvider != null) {
            builder.setLayerConfigurationProider(layerConfigurationProvider);
        }
        return builder.build();
    }

    /**
     * @return the execution context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the execution context to set
     */
    public void setContext(String context) {
        this.context = context;
    }

    /**
     * @return the profile
     */
    public String getProfile() {
        return profile;
    }

    /**
     * @param profile the profile to set
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * @return the userEnabledAddOns
     */
    public Set<String> getAddOns() {
        return addOns;
    }

    /**
     * @param addOns the userEnabledAddOns to set
     */
    public void setAddOns(Set<String> addOns) {
        this.addOns = Set.copyOf(addOns);
    }

    /**
     * @return the suggest
     */
    public boolean isSuggest() {
        return suggest;
    }

    /**
     * @param suggest the suggest to set
     */
    public void setSuggest(boolean suggest) {
        this.suggest = suggest;
    }

    /**
     * @return the layersForJndi
     */
    public Set<String> getLayersForJndi() {
        return layersForJndi;
    }

    /**
     * @param layersForJndi the layersForJndi to set
     */
    public void setLayersForJndi(Set<String> layersForJndi) {
        this.layersForJndi = Set.copyOf(layersForJndi);
    }

    /**
     * @return the failsOnError
     */
    public boolean isFailsOnError() {
        return failsOnError;
    }

    /**
     * @param failsOnError the failsOnError to set
     */
    public void setFailsOnError(boolean failsOnError) {
        this.failsOnError = failsOnError;
    }

    /**
     * @return the excludedArchives
     */
    public Set<String> getExcludedArchives() {
        return excludedArchives;
    }

    /**
     * @param excludedArchives the excludedArchives to set
     */
    public void setExcludedArchives(Set<String> excludedArchives) {
        this.excludedArchives = Set.copyOf(excludedArchives);
    }

    /**
     * @param verbose the verbose to set
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * @return the verbose
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * @param ignoreDeployment the ignoreDeployment to set
     */
    public void setIgnoreDeployment(boolean ignoreDeployment) {
        this.ignoreDeployment = ignoreDeployment;
    }

    /**
     * @return the ignoreDeployment
     */
    public boolean isIgnoreDeployment() {
        return ignoreDeployment;
    }
}
