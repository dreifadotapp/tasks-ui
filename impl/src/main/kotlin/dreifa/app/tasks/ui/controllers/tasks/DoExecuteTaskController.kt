package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.body.form
import org.http4k.routing.path

class DoExecuteTaskController(registry: Registry) : BaseController(){
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val serialiser = JsonSerialiser()
    override fun handle(request: Request): Response {
            val taskName = request.path("task")!!

            val inputClazz = request.form("inputClazz")!!
            val outputClazz = request.form("outputClazz")!!
            val inputJson = request.form("inputJson")!!
            val kClass = Class.forName(outputClazz).kotlin

            val input = serialiser.fromPacketPayload(inputJson, inputClazz)

            val ctx = SimpleClientContext()
            val result = taskClient.execBlocking(ctx, taskName, input, kClass)

            val json = serialiser.toPacketPayload(result)
            return json(json)

    }



}