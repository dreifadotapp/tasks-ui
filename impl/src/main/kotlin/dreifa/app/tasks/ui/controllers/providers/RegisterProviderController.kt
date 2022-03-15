package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TemplateProcessor
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.core.toParametersMap
import org.http4k.routing.path
import java.lang.RuntimeException

class RegisterProviderController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val serialiser = JsonSerialiser()
    fun handle(request: Request): Response {
        try {
            val model = HashMap<String, Any>()
            model["aa"] = "sss"


            val parameters: Map<String, List<String?>> = request.form().toParametersMap()
            //assertEquals("rita", parameters.getFirst("name"))
            //assertEquals(listOf("55"), parameters["age"])
            //assertNull(parameters["height"])
            val html = TemplateProcessor().renderMustache("providers/register.html", model)
            return Response(Status.OK).body(html)
        } catch (ex: Exception) {
            return Response(Status.INTERNAL_SERVER_ERROR).body(ex.message!!)
        }
    }

}