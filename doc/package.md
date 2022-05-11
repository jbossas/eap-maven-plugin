# Package your application

The `package` goal offers the following capabilities:

* Provision an EAP server using Galleon.
* Execute JBoss CLI commands and scripts to fine tune the provisioned server configuration.
* Copy some extra content (e.g.: keystore, properties files) to the provisioned server installation.
* Deploy your application in the provisioned server.

By default the server is provisioned in the ``<project directory>/target/server`` directory.

## Package a JAXRS application

The example below shows how to produce the latest released EAP 8 server trimmed using 'jaxrs-server' Galleon layer. 

```xml
<project>
    ...
    <build>
        ...
        <plugins>
            ...
            <plugin>
                <groupId>${project.groupId}</groupId>
                <artifactId>${project.artifactId}</artifactId>
                <version>${project.version}</version>
                <configuration>
                    <feature-packs>
                        <feature-pack>
                            <location>org.jboss.eap:wildfly-ee-galleon-pack</location>
                        </feature-pack>
                        <feature-pack>
                            <location>org.jboss.eap.cloud:eap-cloud-galleon-pack</location>
                        </feature-pack>
                    </feature-packs>
                    <layers>
                        <!-- Galleon layer allows to trim the installed server to your needs. The 'jaxrs-server' 
                        contains the server content required to execute JAXRS applications -->
                        <layer>jaxrs-server</layer>
                    </layers>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>package</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            ...
        </plugins>
        ...
    </build>
...
</project>
```

## Fine tune server configuration and package extra content

The example below shows how to configure the plugin goal to execute JBoss CLI commands and copy extra content inside the installed server: 

```xml
<plugin>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
    <configuration>
        <feature-packs>
            <feature-pack>
                <location>org.jboss.eap:wildfly-ee-galleon-pack</location>
            </feature-pack>
            <feature-pack>
                <location>org.jboss.eap.cloud:eap-cloud-galleon-pack</location>
            </feature-pack>
        </feature-packs>
        <layers>
            <layer>jaxrs-server</layer>
        </layers>
        <packaging-scripts>
            <!-- Commands and scripts are executed in the context of an embedded-server. 
            You don't need to start and stop the embedded-server. -->
            <execution>
                <!-- List of commands -->
                <commands>
                    <command>/system-property=org.jboss.example.runtime:write-attribute(name=dev)</command>
                    <command>/system-property=org.jboss.example.runtime2:write-attribute(name=dev2)</command>
                </commands>
                <!-- Properties files containing System Properties to resolve expressions -->
                <properties-files>
                    <file>cli.properties</file>
                </properties-files>
                <!-- Enable expression resolution prior to send the commands to the server, false by default -->
                <resolve-expressions>true</resolve-expressions>
                <!-- List of CLI script files -->
                <scripts>
                    <!-- CLI scripts are located in src/main/scripts directory -->
                    <script>${project.build.scriptSourceDirectory}/config.cli</script>
                    <script>${project.build.scriptSourceDirectory}/config2.cli</script>
                </scripts>
            </execution>
        </packaging-scripts>
        <!-- A list of directory that contains content to copy to the server. Each directory must contain a 
         directory structure that complies with the server directory structure. 
         For example extra-content/standalone/configuration/foo.properties to copy the foo.properties file to 
         target/server/standalone/configuration/ directory -->
        <extra-server-content-dirs>
            <extra-content>extra-content</extra-content>
        </extra-server-content-dirs>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>package</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Deploy your WAR application in the root context

By default the name of the WAR is used as the runtime-name for your deployment. 
You can specify a `runtime-name` parameter to tune the context in which your deployment will be registered. 
In this example we are using the special `ROOT.war` runtime name to reference the root context.

```xml
<plugin>
    <groupId>${project.groupId}</groupId>
    <artifactId>${project.artifactId}</artifactId>
    <version>${project.version}</version>
    <configuration>
        <feature-packs>
            <feature-pack>
                <location>org.jboss.eap:wildfly-ee-galleon-pack</location>
            </feature-pack>
            <feature-pack>
                <location>org.jboss.eap.cloud:eap-cloud-galleon-pack</location>
            </feature-pack>
        </feature-packs>
        <layers>
            <layer>jaxrs-server</layer>
        </layers>
        <runtime-name>ROOT.war</runtime-name>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>package</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```
