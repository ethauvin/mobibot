/*
 * MobibotBuild.java
 *
 * Copyright 2004-2024 Erik C. Thauvin (erik@thauvin.net)
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
import rife.bld.extension.CompileKotlinOperation;
import rife.bld.extension.DetektOperation;
import rife.bld.extension.GeneratedVersionOperation;
import rife.bld.extension.JacocoReportOperation;
import rife.bld.operations.exceptions.ExitStatusException;
import rife.bld.publish.PomBuilder;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

import static rife.bld.dependencies.Repository.*;
import static rife.bld.dependencies.Scope.compile;
import static rife.bld.dependencies.Scope.test;

public class MobibotBuild extends Project {
    public MobibotBuild() {
        pkg = "net.thauvin.erik.mobibot";
        name = "mobibot";
        version = version(0, 8, 0, "rc+" +
                DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now()));

        mainClass = pkg + ".Mobibot";

        javaRelease = 17;
        downloadSources = true;
        autoDownloadPurge = true;
        repositories = List.of(
                MAVEN_LOCAL,
                MAVEN_CENTRAL,
                new Repository("https://jitpack.io"),
                SONATYPE_SNAPSHOTS_LEGACY);

        var log4j = version(2, 23, 0);
        var kotlin = version(1, 9, 22);
        scope(compile)
                // PircBotX
                .include(dependency("com.github.pircbotx", "pircbotx", "2.3.1"))
                // Commons (mostly for PircBotX)
                .include(dependency("org.apache.commons", "commons-lang3", "3.14.0"))
                .include(dependency("org.apache.commons", "commons-text", "1.11.0"))
                .include(dependency("commons-codec", "commons-codec", "1.16.1"))
                .include(dependency("commons-net", "commons-net", "3.10.0"))
                // Google
                .include(dependency("com.google.code.gson", "gson", "2.10.1"))
                .include(dependency("com.google.guava", "guava", "33.0.0-jre"))
                .include(dependency("com.google.cloud", "google-cloud-vertexai", version(0, 5, 0)))
                // Kotlin
                .include(dependency("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.8.0"))
                .include(dependency("org.jetbrains.kotlinx", "kotlinx-cli-jvm", "0.3.6"))
                // Logging
                .include(dependency("org.slf4j", "slf4j-api", "2.0.12"))
                .include(dependency("org.apache.logging.log4j", "log4j-api", log4j))
                .include(dependency("org.apache.logging.log4j", "log4j-core", log4j))
                .include(dependency("org.apache.logging.log4j", "log4j-slf4j2-impl", log4j))
                // Misc.
                .include(dependency("com.rometools", "rome", "2.1.0"))
                .include(dependency("com.squareup.okhttp3", "okhttp", "4.12.0"))
                .include(dependency("net.aksingh", "owm-japis", "2.5.3.0"))
                .include(dependency("net.objecthunter", "exp4j", "0.4.8"))
                .include(dependency("org.json", "json", "20240205"))
                .include(dependency("org.jsoup", "jsoup", "1.17.2"))
                // Thauvin
                .include(dependency("net.thauvin.erik", "cryptoprice", "1.0.3-SNAPSHOT"))
                .include(dependency("net.thauvin.erik", "jokeapi", "0.9.1"))
                .include(dependency("net.thauvin.erik", "pinboard-poster", "1.1.2-SNAPSHOT"))
                .include(dependency("net.thauvin.erik.urlencoder", "urlencoder-lib-jvm", "1.4.0"));
        scope(test)
                .include(dependency("com.willowtreeapps.assertk", "assertk-jvm", version(0, 28, 0)))
                .include(dependency("org.jetbrains.kotlin", "kotlin-test-junit5", kotlin))
                .include(dependency("org.junit.jupiter", "junit-jupiter", version(5, 10, 2)))
                .include(dependency("org.junit.platform", "junit-platform-console-standalone", version(1, 10, 2)));

        List<String> jars = new ArrayList<>();
        runtimeClasspathJars().forEach(f -> jars.add("./lib/" + f.getName()));
        compileClasspathJars().forEach(f -> jars.add("./lib/" + f.getName()));
        jarOperation()
                .manifestAttribute(Attributes.Name.MAIN_CLASS, mainClass())
                .manifestAttribute(Attributes.Name.CLASS_PATH, ". " + String.join(" ", jars));

        jarSourcesOperation().sourceDirectories(new File(srcMainDirectory(), "kotlin"));
    }

    public static void main(String[] args) {
        new MobibotBuild().start(args);
    }

    @Override
    public void clean() throws Exception {
        FileUtils.deleteDirectory(new File("deploy"));
        super.clean();
    }

    @BuildCommand(summary = "Compiles the Kotlin project")
    @Override
    public void compile() throws Exception {
        releaseInfo();
        new CompileKotlinOperation()
                .fromProject(this)
                .execute();
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
                .baseline("config/detekt/baseline.xml")
                .execute();
    }

    @BuildCommand(value = "detekt-baseline", summary = "Creates the Detekt baseline")
    public void detektBaseline() throws ExitStatusException, IOException, InterruptedException {
        new DetektOperation()
                .fromProject(this)
                .baseline("config/detekt/baseline.xml")
                .createBaseline(true)
                .execute();
    }

    @BuildCommand(summary = "Generates JaCoCo Reports")
    public void jacoco() throws IOException {
        new JacocoReportOperation()
                .fromProject(this)
                .execute();
    }

    @BuildCommand(value = "release-info", summary = "Generates the ReleaseInfo class")
    public void releaseInfo() {
        new GeneratedVersionOperation()
                .fromProject(this)
                .classTemplate(new File(workDirectory(), "release-info.txt"))
                .className("ReleaseInfo")
                .packageName(pkg)
                .directory(new File(srcMainDirectory(), "kotlin"))
                .extension(".kt")
                .execute();
    }

    @BuildCommand(value = "root-pom", summary = "Generates the POM file in the root directory")
    public void rootPom() throws FileUtilsErrorException {
        PomBuilder.generateInto(publishOperation().info(), publishOperation().dependencies(),
                Path.of(workDirectory.getPath(), "pom.xml").toFile());
    }
}
