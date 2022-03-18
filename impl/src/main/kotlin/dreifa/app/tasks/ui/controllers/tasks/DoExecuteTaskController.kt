package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.body.form
import org.http4k.routing.path
import java.lang.RuntimeException

class DoExecuteTaskController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val serialiser = JsonSerialiser()
    fun handle(request: Request): Response {
            val taskName = request.path("task")!!

            val inputClazz = request.form("inputClazz")!!
            val outputClazz = request.form("outputClazz")!!
            val inputJson = request.form("inputJson")!!
            val kClass = Class.forName(outputClazz).kotlin

            val input = serialiser.fromPacketPayload(inputJson, inputClazz)

            val ctx = SimpleClientContext()
            val result = taskClient.execBlocking(ctx, taskName, input, kClass)

//            val mapper: ObjectMapper = ObjectMapper()
//            val module = KotlinModule()
//            //module.addSerializer(SerialisationPacketWireFormat::class.java, XX())
//            mapper.registerModule(module)

            return Response(Status.OK).body(result!!.toString())

    }

    private fun checkForTaskDocs(task: String, example: Int, model: HashMap<String, Any>) {
        try {
            val docs = taskClient.taskDocs<Any, Any>(
                SimpleClientContext(),
                task
            )
            model["hasTaskDoc"] = true
            model["description"] = docs.description()
            model["example"] = examplesPresenter(docs.examples()[example - 1])
        } catch (ignoreMe: RuntimeException) {
        }
    }

    private fun examplesPresenter(example: TaskExample<Any, Any>): Map<String, Any> {
        val model = HashMap<String, Any>()
        if (example.input() != null) {
            model["hasInput"] = true
            model["input"] = example.input()!!
            model["inputAsJson"] = serialiser.toPacketData(example.input()!!.example)
        } else {
            model["hasInput"] = false
        }
//        if (example.output() != null) {
//            model["hasOutput"] = true
//            model["output"] = example.output()!!
//            model["outputAsJson"] = serialiser.toPacketData(example.output()!!)
//        } else {
//            model["hasOutput"] = false
//        }
//        model["exampleNumber"] = index + 1
        model["description"] = example.description()
        return model
    }
}