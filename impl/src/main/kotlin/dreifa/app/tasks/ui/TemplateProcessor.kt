package dreifa.app.tasks.ui

import com.github.mustachejava.DefaultMustacheFactory
import java.io.*
import java.lang.RuntimeException

class TemplateProcessor {

    fun renderMustache(path: String, params: Map<String, Any?>, layoutName: String = "default.html"): String {
        // mustache processing
        val layout = readFileAsText("templates/layout/$layoutName", params)

        val content = readFileAsText("templates/$path", params)
        return layout.replace("<!--BODYTEXT-->", content)
    }

    private fun readFileAsText(path: String, substitutions: Map<String, Any?> = emptyMap()): String {
        val raw = loadResourceAsText(path)

        return if (substitutions.isNotEmpty()) {
            val mf = DefaultMustacheFactory()
            val mustache = mf.compile(StringReader(raw), "template.mustache")

            val bos = ByteArrayOutputStream()
            val writer = OutputStreamWriter(bos)
            mustache.execute(writer, substitutions).flush()
            bos.toString()
        } else {
            raw;
        }
    }

    private fun loadResourceAsText(path: String): String {
        return try {
            // running through IDE
            FileInputStream("impl/src/main/resources/$path").bufferedReader().use { it.readText() }
        } catch (fne: FileNotFoundException) {
            // running as packaged JAR
            try {
                TemplateProcessor::class.java.getResource("/$path").readText()
            } catch (re: RuntimeException) {
                throw RuntimeException("Couldn't load the template `$path`. Tried both the local file system and the classpath")
            }
        }
    }
}