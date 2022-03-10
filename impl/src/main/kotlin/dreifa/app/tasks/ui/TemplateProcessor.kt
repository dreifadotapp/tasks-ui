package dreifa.app.tasks.ui

import com.github.mustachejava.DefaultMustacheFactory
import java.io.*

class TemplateProcessor {
    fun process(templateFileName: String, model: Any, layoutName: String = "default.html"): String {
        try {
            println("Processing $templateFileName")

            val text = TemplateProcessor::class.java.getResource(templateFileName).readText()
            val mf = DefaultMustacheFactory()
            val mustache = mf.compile(StringReader(text), "template.mustache")

            val bos = ByteArrayOutputStream()
            val writer = OutputStreamWriter(bos)
            mustache.execute(writer, model).flush()
            return bos.toString()

        } catch (ex: Exception) {
            ex.printStackTrace()
            return "Problem with template: ${ex.message}"
        }
    }



    fun renderMustache(path: String, params: Map<String, Any?>, layoutName: String = "default.html"): String {
        return try {
            // mustache processing
            val layout = readFileAsText("templates/layout/$layoutName", params)

            val content = readFileAsText("templates/$path", params)
            layout.replace("<!--BODYTEXT-->", content)
            //   }
        } catch (ex: Exception) {
            "<pre>" + ex.message!! + "</pre>"
        }
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
            TemplateProcessor::class.java.getResource("/$path").readText()
        }
    }

}