import com.beust.kobalt.*
import com.beust.kobalt.plugin.apt.*
import com.beust.kobalt.plugin.packaging.*
import com.beust.kobalt.plugin.application.*
import com.beust.kobalt.plugin.java.*
import java.io.File
import java.io.FileInputStream
import java.util.*

val bs = buildScript {
    repos()
}

val mainClassName = "net.thauvin.erik.mobibot.Mobibot"

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

    val processorJar = "net.thauvin.erik:semver:"

    dependencies {
            compile("log4j:log4j:jar:1.2.17")

            compile("pircbot:pircbot:1.5.0")

            compile("commons-codec:commons-codec:1.10")
            compile("commons-logging:commons-logging:1.2")
            compile("commons-net:commons-net:3.5")
            compile("commons-cli:commons-cli:1.3.1")
            compile("commons-httpclient:commons-httpclient:3.1")

            compile("oro:oro:2.0.8")

            compile("org.jsoup:jsoup:1.9.2")
            compile("com.rometools:rome:1.6.1")
            compile("org.slf4j:slf4j-log4j12:1.7.21")
            compile("org.json:json:20160212")
            compile("org.ostermiller:utils:1.07.00")

            compile("net.sourceforge.jweather:jweather:jar:0.3.0")
            compile("net.objecthunter:exp4j:0.4.7")

            compile("org.twitter4j:twitter4j-core:4.0.4")
            compile("net.sf.delicious-java:delicious:1.14")

            apt(processorJar)
            compile(processorJar)
    }

    apt {
        outputDir = "/src/generated/java/"
    }

    assemble {
        jar {
            name = "${project.name}.jar"
            fatJar = true
            manifest {
                attributes("Main-Class", mainClassName)
            }
        }
    }

    application {
        mainClass = mainClassName
        args("-v")
    }

    install {
        libDir = "deploy"
    }
}
