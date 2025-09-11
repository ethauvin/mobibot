/*
 * MobibotBuild.java
 *
 * Copyright 2004-2025 Erik C. Thauvin (erik@thauvin.net)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *   Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *   Neither the name of this project nor the names of its contributors may be
 *   used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.thauvin.erik;

import rife.bld.BuildCommand;
import rife.bld.Project;
import rife.bld.dependencies.Repository;
import rife.bld.extension.*;
import rife.bld.operations.exceptions.ExitStatusException;
import rife.bld.publish.PomBuilder;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;

public class MobibotBuild extends Project {
    static final String TEST_RESULTS_DIR = "build/test-results/test/";
    private static final String DETEKT_BASELINE = "config/detekt/baseline.xml";
    final File srcMainKotlin = new File(srcMainDirectory(), "kotlin");

    public MobibotBuild() {
        pkg = "net.thauvin.erik.mobibot";
        name = "mobibot";
        version = version(0, 8, 0, "rc+" +
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));

        mainClass = pkg + ".Mobibot";

        javaRelease = 21;

        autoDownloadPurge = true;
        downloadSources = true;

        repositories = List.of(
                MAVEN_LOCAL,
                MAVEN_CENTRAL,
                new Repository("https://jitpack.io"),
                CENTRAL_SNAPSHOTS);

        var log4j = version(2, 25, 1);
        var kotlin = version(2, 2, 10);
        var langChain = version(1, 4, 0);
        scope(compile)
                // PircBotX
                .include(dependency("com.github.pircbotx", "pircbotx", "2.3.1"))
                // Commons (mostly for PircBotX)
                .include(dependency("org.apache.commons", "commons-lang3", "3.18.0"))
                .include(dependency("org.apache.commons", "commons-text", "1.14.0"))
                .include(dependency("commons-codec", "commons-codec", "1.19.0"))
                .include(dependency("commons-net", "commons-net", "3.12.0"))
                // Google
                .include(dependency("com.google.code.gson", "gson", "2.13.2"))
                .include(dependency("com.google.guava", "guava", "33.2.1-jre"))
                // Kotlin
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib", kotlin))
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib-common", kotlin))
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib-jdk7", kotlin))
                .include(dependency("org.jetbrains.kotlin", "kotlin-stdlib-jdk8", kotlin))
                .include(dependency("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.10.2"))
                .include(dependency("org.jetbrains.kotlinx", "kotlinx-cli-jvm", "0.3.6"))
                // Logging
                .include(dependency("org.slf4j", "slf4j-api", "2.0.17"))
                .include(dependency("org.apache.logging.log4j", "log4j-api", log4j))
                .include(dependency("org.apache.logging.log4j", "log4j-core", log4j))
                .include(dependency("org.apache.logging.log4j", "log4j-slf4j2-impl", log4j))
                // LangChain4J
                .include(dependency("dev.langchain4j", "langchain4j-open-ai", langChain))
                .include(dependency("dev.langchain4j", "langchain4j-google-ai-gemini", langChain))
                .include(dependency("dev.langchain4j", "langchain4j-core", langChain))
                .include(dependency("dev.langchain4j", "langchain4j", langChain))
                // Misc.
                .include(dependency("com.rometools", "rome", "2.1.0"))
                .include(dependency("com.squareup.okhttp3", "okhttp-jvm", "5.1.0"))
                .include(dependency("net.aksingh", "owm-japis", "2.5.3.0")
                        .exclude("com.squareup.okhttp3", "okhttp"))
                .include(dependency("net.objecthunter", "exp4j", "0.4.8"))
                .include(dependency("org.json", "json", "20250517"))
                .include(dependency("org.jsoup", "jsoup", "1.21.2"))
                // Thauvin
                .include(dependency("net.thauvin.erik", "cryptoprice", "1.0.3-SNAPSHOT"))
                .include(dependency("net.thauvin.erik", "frankfurter4j", "0.9.0-SNAPSHOT"))
                .include(dependency("net.thauvin.erik", "jokeapi", "1.0.1"))
                .include(dependency("net.thauvin.erik", "pinboard-poster", "1.2.1-SNAPSHOT"))
                .include(dependency("net.thauvin.erik.urlencoder", "urlencoder-lib-jvm", "1.6.0"));
        scope(test)
                // bld
                .include(dependency("com.uwyn.rife2", "bld-extensions-testing-helpers",
                        version(0, 9, 0, "SNAPSHOT")))
                // Mockito
                .include(dependency("net.bytebuddy", "byte-buddy",
                        version(1, 17, 7)))
                .include(dependency("org.mockito.kotlin", "mockito-kotlin",
                        version(6, 0, 0)))
                // AssertK
                .include(dependency("com.willowtreeapps.assertk", "assertk-jvm",
                        version(0, 28, 1)))
                // JUnit
                .include(dependency("org.jetbrains.kotlin", "kotlin-test-junit5", kotlin))
                .include(dependency("org.junit.jupiter", "junit-jupiter",
                        version(5, 13, 4)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone",
                        version(1, 13, 4)))
                .include(dependency("org.junit.platform", "junit-platform-launcher",
                        version(1, 13, 4)));

        List<String> jars = new ArrayList<>();
        runtimeClasspathJars().forEach(f -> jars.add("./lib/" + f.getName()));
        compileClasspathJars().forEach(f -> jars.add("./lib/" + f.getName()));
        jarOperation()
                .manifestAttribute(Attributes.Name.MAIN_CLASS, mainClass())
                .manifestAttribute(Attributes.Name.CLASS_PATH, ". " + String.join(" ", jars));

        jarSourcesOperation().sourceDirectories(srcMainKotlin);
    }

    public static void main(String[] args) {
        var level = Level.WARNING;
        var logger = Logger.getLogger("rife.bld.extension");
        var consoleHandler = new ConsoleHandler();

        consoleHandler.setLevel(level);
        logger.addHandler(consoleHandler);
        logger.setLevel(level);
        logger.setUseParentHandlers(false);

        new MobibotBuild().start(args);
    }

    @Override
    public void clean() throws Exception {
        var deploy = new File("deploy");
        if (deploy.exists()) {
            FileUtils.deleteDirectory(deploy);
        }
        super.clean();
    }

    @BuildCommand(summary = "Compiles the Kotlin project")
    @Override
    public void compile() throws Exception {
        releaseInfo();
        var op = new CompileKotlinOperation().fromProject(this);
        op.compileOptions().languageVersion("2.2").progressive(true).verbose(false);
        op.execute();
    }

    @Override
    public void updates() throws Exception {
        super.updates();
        pomRoot();
    }

    @BuildCommand(summary = "Copies all needed files to the deploy directory")
    public void deploy() throws FileUtilsErrorException {
        var deploy = new File("deploy");
        var lib = new File(deploy, "lib");
        var ignore = lib.mkdirs();
        FileUtils.copyDirectory(new File("properties"), deploy);
        for (var jar : compileClasspathJars()) {
            FileUtils.copy(jar, new File(lib, jar.getName()));
        }
        for (var jar : runtimeClasspathJars()) {
            FileUtils.copy(jar, new File(lib, jar.getName()));
        }
        FileUtils.copy(new File(buildDistDirectory(), jarFileName()), new File(deploy, "mobibot.jar"));
    }

    @BuildCommand(summary = "Checks source with Detekt")
    public void detekt() throws ExitStatusException, IOException, InterruptedException {
        new DetektOperation()
                .fromProject(this)
                .baseline(DETEKT_BASELINE)
                .execute();
    }

    @BuildCommand(value = "detekt-baseline", summary = "Creates the Detekt baseline")
    public void detektBaseline() throws ExitStatusException, IOException, InterruptedException {
        new DetektOperation()
                .fromProject(this)
                .baseline(DETEKT_BASELINE)
                .createBaseline(true)
                .execute();
    }

    @BuildCommand(summary = "Generates JaCoCo Reports")
    public void jacoco() throws Exception {
        var op = new JacocoReportOperation().fromProject(this);
        op.testToolOptions("--reports-dir=" + TEST_RESULTS_DIR);

        Exception ex = null;
        try {
            op.execute();
        } catch (Exception e) {
            ex = e;
        }

        renderWithXunitViewer();

        if (ex != null) {
            throw ex;
        }
    }

    @BuildCommand(value = "pom-root", summary = "Generates the POM file in the root directory")
    public void pomRoot() throws FileUtilsErrorException {
        PomBuilder.generateInto(publishOperation().fromProject(this).info(), dependencies(),
                new File(workDirectory, "pom.xml"));
    }

    @BuildCommand(value = "release-info", summary = "Generates the ReleaseInfo class")
    public void releaseInfo() throws Exception {
        new GeneratedVersionOperation()
                .fromProject(this)
                .classTemplate(new File(workDirectory(), "release-info.txt"))
                .className("ReleaseInfo")
                .packageName(pkg)
                .directory(srcMainKotlin)
                .extension(".kt")
                .execute();
    }

    private void renderWithXunitViewer() throws Exception {
        var npmPackagesEnv = System.getenv("NPM_PACKAGES");
        if (npmPackagesEnv != null && !npmPackagesEnv.isEmpty()) {
            var xunitViewer = Path.of(npmPackagesEnv, "bin", "xunit-viewer").toFile();
            if (xunitViewer.exists() && xunitViewer.canExecute()) {
                var reportsDir = "build/reports/tests/test/";

                Files.createDirectories(Path.of(reportsDir));

                new ExecOperation()
                        .fromProject(this)
                        .command(xunitViewer.getPath(), "-r", TEST_RESULTS_DIR, "-o", reportsDir + "index.html")
                        .execute();
            }
        }
    }

    @BuildCommand(summary = "Runs the JUnit reporter")
    public void reporter() throws Exception {
        new JUnitReporterOperation()
                .fromProject(this)
                .failOnSummary(true)
                .execute();
    }

    @Override
    public void test() throws Exception {
        var op = testOperation().fromProject(this);
        op.testToolOptions().reportsDir(new File(TEST_RESULTS_DIR));

        Exception ex = null;
        try {
            op.execute();
        } catch (Exception e) {
            ex = e;
        }

        renderWithXunitViewer();

        if (ex != null) {
            throw ex;
        }
    }
}
