package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.tasks.ui.services.TaskClientService
import dreifa.app.types.UniqueId
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.body.form
import org.http4k.routing.path
import kotlin.reflect.KClass

class DoExecuteTaskController(registry: Registry) : BaseController() {
    private val serialiser = JsonSerialiser()
    private val taskClientService = TaskClientService(registry)
    override fun handle(request: Request): Response {
        val taskName = request.path("task")!!
        val providerId = request.path("providerId")!!

        val inputClazz = request.form("inputClazz")!!
        val outputClazz = request.form("outputClazz")!!
        val inputJson = request.form("inputJson")!!
        val kClass = clazzFromName(outputClazz)

        val input = serialiser.fromPacketPayload(inputJson, inputClazz)
        val ctx = SimpleClientContext()

        val taskClient = taskClientService.exec(ctx, UniqueId.fromString(providerId))
        val result = taskClient.execBlocking(ctx, taskName, input, kClass)

        val json = serialiser.toPacketPayload(result)
        return json(json ?: "null")
    }

    private fun clazzFromName(clazzName: String): KClass<out Any> {
        // annoyingly, Class.forName doesn't understand kotlin types
        val remappedClazzName = when (clazzName) {
            "kotlin.Int" -> "java.lang.Integer"
            "kotlin.Long" -> "java.lang.Long"
            "kotlin.Float" -> "java.lang.Float"
            "kotlin.Double" -> "java.lang.Double"
            "kotlin.Boolean" -> "java.lang.Boolean"
            "kotlin.String" -> "java.lang.String"
            else -> clazzName
        }
        return Class.forName(remappedClazzName).kotlin
    }
}