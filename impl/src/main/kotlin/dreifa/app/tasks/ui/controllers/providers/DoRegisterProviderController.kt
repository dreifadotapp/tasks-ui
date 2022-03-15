package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.*
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TemplateProcessor
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfoList
import org.http4k.core.*
import java.io.File
import java.net.URL
import java.net.URLClassLoader


class DoRegisterProviderController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val serialiser = JsonSerialiser()
    fun handle(request: Request): Response {
        try {
            val model = HashMap<String, Any>()


            val receivedForm = MultipartFormBody.from(request)
            val multipart = receivedForm.files("file1")[0]
            val f = File(multipart.filename)
            f.writeBytes(multipart.content.readAllBytes())

            val url = URL("file:${f.absolutePath}")

            val bytes = url.openStream().readAllBytes().size
            model["name"] = multipart.filename
            model["size"] = bytes

            val loader = URLClassLoader(listOf(url).toTypedArray())
            val graph = ClassGraph()
                .enableAllInfo()
                .enableRemoteJarScanning()
                .acceptPackages("dreifa.app")
                .addClassLoader(loader)
                .scan()

            val registrations: ClassInfoList = graph.getClassesImplementing("dreifa.app.tasks.TaskRegistrations")
            model["registration"] = registrations
                .filter { it.classpathElementURL == url }
                .single().name


            val html = TemplateProcessor().renderMustache("providers/registerResult.html", model)
            return Response(Status.OK).body(html)
        } catch (ex: Exception) {
            return Response(Status.INTERNAL_SERVER_ERROR).body(ex.message!!)
        }
    }

}