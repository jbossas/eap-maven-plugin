/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2022, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.eap.plugin.provision;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.maven.plugin.Mojo;
import org.junit.Test;
import org.junit.Assert;
import org.wildfly.plugin.tests.AbstractProvisionConfiguredMojoTestCase;
import org.wildfly.plugin.tests.AbstractWildFlyMojoTest;

public class PackageTest extends AbstractProvisionConfiguredMojoTestCase {

    private static final String JBOSS_HOME_PROPERTY = "jboss.home";

    public PackageTest() {
        super("eap-maven-plugin");
    }

    @Test
    public void testPackage() throws Exception {

        final Mojo packageMojo = lookupConfiguredMojo(AbstractWildFlyMojoTest.getPomFile("package-pom.xml").toFile(), "package");

        packageMojo.execute();
        Path jbossHome = AbstractWildFlyMojoTest.getBaseDir().resolve("target").resolve("packaged-server");
        Assert.assertTrue(Files.exists(jbossHome.resolve("standalone").
                resolve("configuration").resolve("foo.txt")));
        String[] layers = {"jaxrs-server"};
        String[] excluded = {"deployment-scanner"};
        String originalHome = System.getProperty(JBOSS_HOME_PROPERTY);
        System.setProperty(JBOSS_HOME_PROPERTY, jbossHome.toString());
        try {
            checkStandaloneWildFlyHome(jbossHome, 1, layers, excluded, true, "org.eap.maven.command-plugin-package-goal",
                    "org.eap.maven.plugin-package-goal-from-script");
        } finally {
            System.clearProperty(JBOSS_HOME_PROPERTY);
            if (originalHome != null) {
                System.setProperty(JBOSS_HOME_PROPERTY, originalHome.toString());
            }
        }
    }

}
