package com.github.xylifyx.ivy;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.repository.Repository;

import dk.profundo.ivybridge.IvyBridgeOptions;
import dk.profundo.ivybridge.MavenRepositoryProxy;

/**
 * Wagon Provider for Ivy repositories
 *
 * @author <a href="erik.martino@gmail.com">Erik Martino</a>
 *
 * @plexus.component role="org.apache.maven.wagon.Wagon" role-hint="ivybridge"
 * instantiation-strategy="per-lookup"
 */
public class IvyWagon extends StreamWagon {

    protected String getResolverUrl() {
        final Repository rep = getRepository();
        if (rep == null) {
            return null;
        }
        final String url = rep.getUrl();
        if (url == null) {
            return null;
        }
        if (!url.startsWith(IVYBRIDGE_PREFIX)) {
            return null;
        }
        return url.substring(IVYBRIDGE_PREFIX.length());
    }

    private static final String IVYBRIDGE_PREFIX = "ivybridge:";

    @Override
    public void fillInputData(InputData inputData)
        throws TransferFailedException, ResourceDoesNotExistException {
        String repoUrl = getResolverUrl();
        String name = inputData.getResource().getName();
        InputStream inputStream = streamResult(name, repoUrl);
        inputData.setInputStream(inputStream);
    }

    public static InputStream streamResult(String name, String repoUrl) throws ResourceDoesNotExistException, TransferFailedException {
        try {
            final IvyBridgeOptions options = IvyBridgeOptions.newOptionsFromUri(repoUrl);
            MavenRepositoryProxy proxy = new MavenRepositoryProxy(options);
            final URI artifactUri = proxy.resolveArtifact(name);

            if (artifactUri == null) {
                throw new ResourceDoesNotExistException("resource not found: " + name);
            }

            InputStream inputStream = proxy.toInputStream(artifactUri);

            return inputStream;
        } catch (FileNotFoundException ex) {
            throw new ResourceDoesNotExistException("resource not found: " + name, ex);
        } catch (URISyntaxException | IntrospectionException | IllegalArgumentException | ParseException | IOException ex) {
            throw new TransferFailedException("Exception", ex);
        }
    }


    @Override
    public void fillOutputData(OutputData outputData)
        throws TransferFailedException {
        throw new TransferFailedException("Writing unsupported: " + outputData.getResource().getName());
    }

    @Override
    protected void openConnectionInternal() throws ConnectionException {

    }

    @Override
    public void closeConnection() {

    }

    @Override
    public boolean supportsDirectoryCopy() {
        System.err.println("supportsDirectoryCopy");
        return false;
    }

    @Override
    public void putDirectory(File sourceDirectory, String destinationDirectory)
        throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        throw new UnsupportedOperationException("putDirectory");
    }

    @Override
    public List<String> getFileList(String destinationDirectory)
        throws TransferFailedException, ResourceDoesNotExistException,
        AuthorizationException {
        System.err.println("getFileList: " + destinationDirectory);
        return Collections.emptyList();
    }

    @Override
    public boolean resourceExists(String resourceName)
        throws TransferFailedException, AuthorizationException {
        System.err.println("resourceExists: " + resourceName);
        return false;
    }

}
