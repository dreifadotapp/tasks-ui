package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.UniqueId
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import java.lang.RuntimeException

class ExecuteTaskController(registry: Registry) : BaseController(registry) {
    private val internalTasks = registry.get(InternalOnlyTaskClient::class.java)

    override fun handle(req: Request): Response {
        val trc = TelemetryRequestContext(req, "/tasks/{providerId}/{task}/execute")
        return runWithTelemetry(trc) { span ->
            val taskName = req.path("task")!!
            val providerId = req.path("providerId")!!
            val exampleNumber = req.query("example")!!.toInt()
            val clientContext = clientContextWithTelemetry(span)

            val model = HashMap<String, Any>()
            model["name"] = taskName

            val serialiser = internalTasks.client.execBlocking(
                clientContext,
                TaskNames.UISimpleSerialiserTask,
                UniqueId.fromString(providerId),
                JsonSerialiser::class
            )

            val taskFactory = internalTasks.client.execBlocking(
                clientContext,
                TaskNames.UITaskFactoryTask,
                UniqueId.fromString(providerId),
                TaskFactory::class
            )

            val taskClient = internalTasks.client.execBlocking(
                clientContext,
                TaskNames.UITaskClientTask,
                UniqueId.fromString(providerId),
                TaskClient::class
            )

            val task = taskFactory.createInstance(taskName)
            when (task) {
                is BlockingTask<*, *> -> {
                    model["type"] = "Blocking"
                }
                is AsyncTask<*, *> -> {
                    model["type"] = "Async"
                }
            }

            checkForTaskDocs(serialiser, taskClient, taskName, exampleNumber, model)

            val reflections = TaskReflections(task::class)
            model["inputClazz"] = reflections.paramClass().qualifiedName!!
            model["outputClazz"] = reflections.resultClass().qualifiedName!!

            val html = TemplateProcessor().renderMustache("tasks/execute.html", mapOf("task" to model))
            Response(Status.OK).body(html)
        }
    }

    private fun checkForTaskDocs(
        serialiser: JsonSerialiser,
        taskClient: TaskClient,
        task: String,
        example: Int,
        model: HashMap<String, Any>
    ) {
        try {
            val docs = taskClient.taskDocs<Any, Any>(
                SimpleClientContext(),
                task
            )
            model["hasTaskDoc"] = true
            model["description"] = docs.description()
            model["example"] = examplesPresenter(serialiser, docs.examples()[example - 1])
        } catch (ignoreMe: RuntimeException) {
        }
    }

    private fun examplesPresenter(serialiser: JsonSerialiser, example: TaskExample<Any, Any>): Map<String, Any> {
        val model = HashMap<String, Any>()
        if (example.input() != null) {
            model["hasInput"] = true
            model["input"] = example.input()!!
            model["inputAsJson"] = serialiser.toPacketPayload(example.input()!!.example) ?: ""
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