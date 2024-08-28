/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.config.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.onap.config.api.Configuration;
import org.onap.config.api.ConfigurationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sheetalm on 10/13/2016.
 */
public class TestUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtil.class);

    public static final String jsonSchemaLoc = System.getProperty("user.home") + "/TestResources/";

    public static void cleanUp() throws Exception {
        writeFile("{name:\"SCM\"}");
    }

    private static void writeFile(final String data) {
        final File dir = new File(jsonSchemaLoc);
        dir.mkdirs();
        final File file = new File(jsonSchemaLoc + "/GeneratorsList.json");
        String canonicalPath = "";
        try (final FileWriter fileWriter = new FileWriter(file)) {
            canonicalPath = file.getCanonicalPath();
            fileWriter.write(data);
        } catch (final IOException e) {
            LOGGER.warn("Failed to write file {}", canonicalPath, e);
        }
    }

    public static void validateConfiguration(String nameSpace) {
        Configuration config = ConfigurationManager.lookup();

        Assert.assertEquals("14", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_NAME_MAXLENGTH));

        // First value from list is picked from Merge properties
        Assert.assertEquals("1048576", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_MAXSIZE));

        List<String> expectedExtList = new ArrayList<>();
        expectedExtList.add("pdf");
        expectedExtList.add("zip");
        expectedExtList.add("xml");
        expectedExtList.add("pdf");
        expectedExtList.add("tgz");
        expectedExtList.add("xls");
        List<String> extList = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_EXT);
        Assert.assertEquals(expectedExtList, extList);

        List<String> expectedEncList = new ArrayList<>();
        expectedEncList.add("Base64");
        expectedEncList.add("MD5");
        List<String> encList = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_ENC);
        Assert.assertEquals(expectedEncList, encList);

        Assert.assertEquals("a-zA-Z_0-9", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_NAME_UPPER));
        Assert.assertEquals("a-zA-Z", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_NAME_LOWER));
        Assert.assertEquals("deleted", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_STATUS));

        List<String> expectedLocList = new ArrayList<>();
        expectedLocList.add("/opt/spool");
        expectedLocList.add(System.getProperty("user.home") + "/asdc");
        List<String> locList = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_LOC);
        Assert.assertEquals(expectedLocList, locList);

        Assert.assertEquals("@GeneratorList.json",
            config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_JSON_SCHEMA));

        Assert.assertEquals("@" + getenv(ConfigTestConstant.PATH) + "/myschema.json",
            config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_XML_SCHEMA));

        List<String> artifactConsumer = config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_CONSUMER);
        Assert.assertEquals(config.getAsStringValues(nameSpace, ConfigTestConstant.ARTIFACT_CONSUMER_APPC),
            artifactConsumer);

        Assert.assertEquals("6", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_NAME_MINLENGTH));
        Assert.assertEquals("true", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_MANDATORY_NAME));
        Assert.assertEquals("true", config.getAsString(nameSpace, ConfigTestConstant.ARTIFACT_ENCODED));
    }

    /**
     * This to make the behavior of tests consistent with "env:X" in configuration files
     * when environment variable X is not defined.
     */
    public static String getenv(String name) {
        String value = System.getenv(name);
        return value == null ? "" : value;
    }

    /**
     * Creates temporary directories structure with files inside every directory
     *
     * @param tmpDirPrefix
     * @return
     * @throws IOException
     */
    public static Path createTestDirsStructure(String tmpDirPrefix) throws IOException {
        Path tmpPath = Files.createTempDirectory(tmpDirPrefix);
        Path dir0 = Paths.get(tmpPath.toString(), "dir0", "dir1", "dir2");
        Files.createDirectories(dir0);
        Path[] files = {
            Paths.get(tmpPath.toString(), "file001"),
            Paths.get(tmpPath.toString(), "dir0", "file002"),
            Paths.get(tmpPath.toString(), "dir0", "dir1", "file003"),
            Paths.get(tmpPath.toString(), "dir0", "dir1", "dir2", "file004"),
        };
        for (Path file : files) {
            Files.createFile(file);
        }
        return tmpPath;
    }

    public static Path createEmptyTmpDir(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }

    /**
     * Delete all tmp directories and files created for testing
     *
     * @param rootPath
     */
    public static void deleteTestDirsStrucuture(Path rootPath) {
        try {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
