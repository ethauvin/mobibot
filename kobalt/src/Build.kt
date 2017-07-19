import com.beust.kobalt.*
import com.beust.kobalt.api.Project
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.misc.KobaltLogger
import com.beust.kobalt.misc.log
import com.beust.kobalt.plugin.application.application
import com.beust.kobalt.plugin.apt.apt
import com.beust.kobalt.plugin.java.javadoc
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.packaging.install
import com.beust.kobalt.plugin.publish.autoGitTag
import net.thauvin.erik.kobalt.plugin.versioneye.versionEye
import java.io.File
import java.io.FileInputStream
import java.util.*

val bs = buildScript {
    repos(localMaven())
    plugins("net.thauvin.erik:kobalt-versioneye:")
}

val mainClassName = "net.thauvin.erik.mobibot.Mobibot"
val deploy = "deploy"

fun StringBuilder.prepend(s: String): StringBuilder {
    if (this.isNotEmpty()) {
        this.insert(0, s)
    }
    return this
}

val p = project {

    name = "mobibot"

    fun versionFor(): String {
        val propsFile = "version.properties"
        val majorKey = "version.major"
        val minorKey = "version.minor"
        val patchKey = "version.patch"
        val metaKey = "version.buildmeta"
        val preKey = "version.prerelease"

        val p = Properties().apply { FileInputStream(propsFile).use { fis -> load(fis) } }

        return (p.getProperty(majorKey, "1") + "." + p.getProperty(minorKey, "0") + "." + p.getProperty(patchKey, "0")
                + StringBuilder(p.getProperty(preKey, "")).prepend("-")
                + StringBuilder(p.getProperty(metaKey, "")).prepend("+"))
    }

    version = versionFor()

    val processorJar = "net.thauvin.erik:semver:1.0.1"
    val lib = "lib"

    dependencies {
        compile("pircbot:pircbot:1.5.0")
        //compileOnly("pircbot:pircbot::sources:1.5.0")

        compile("org.apache.logging.log4j:log4j-api:2.8.2",
            "org.apache.logging.log4j:log4j-core:2.8.2",
            "org.apache.logging.log4j:log4j-slf4j-impl:jar:2.8.2")

        compile("commons-cli:commons-cli:1.4", "commons-net:commons-net:3.6")
        compile("com.squareup.okhttp3:okhttp:3.8.1")

        compile("com.rometools:rome:1.7.4")

        compile("org.json:json:20170516")
        compile("org.ostermiller:utils:1.07.00")
        compile("org.jsoup:jsoup:1.10.3")
        compile("net.objecthunter:exp4j:0.4.8")

        compile("org.twitter4j:twitter4j-core:4.0.6")
        compile("net.thauvin.erik:pinboard-poster:0.9.1")

        // https://bitbucket.org/akapribot/owm-japis/
        compile(file("lib/owm-japis-2.5.0.5.jar"))

        apt(processorJar)
        compileOnly(processorJar)
    }

    dependenciesTest {
        compile("org.testng:testng:6.11")
        compile("org.assertj:assertj-core:3.8.0")
    }

    apt {
        outputDir = "../src/generated/java/"
    }

    autoGitTag {
        enabled = true
        message = "Version $version"
        annotated = true
    }

    assemble {
        jar {
            name = "${project.name}.jar"
            manifest {
                attributes("Main-Class", mainClassName)
                attributes("Class-Path",
                        collect(compileDependencies)
                                .map { it.file.name }
                                .joinToString(" ./$lib/", prefix = ". ./$lib/"))
            }
        }
    }

    application {
        mainClass = mainClassName
        args("-v")
    }

    install {
        target = deploy
        include(from("kobaltBuild/libs"), to(target), glob("**/*"))
        include(from("properties"), to(target), glob("**/*"))
        collect(compileDependencies)
                .forEach {
                    copy(from(it.file.absolutePath), to("$target/$lib"))
                }
    }

    javadoc {
        title = project.name + ' ' + project.version
        tags("created")
        author = true
        links("http://www.jibble.org/javadocs/pircbot/", "http://docs.oracle.com/javase/8/docs/api/")
    }

    versionEye {
        org = "thauvin"
        team = "Owners"
    }
}

@Task(name = "deploy", dependsOn = arrayOf("assemble", "install"), description = "Deploy application")
fun deploy(project: Project): TaskResult {
    File("$deploy/logs").mkdir()
    KobaltLogger.log(1, "  Deployed to " + File(deploy).absolutePath)
    return TaskResult()
}
