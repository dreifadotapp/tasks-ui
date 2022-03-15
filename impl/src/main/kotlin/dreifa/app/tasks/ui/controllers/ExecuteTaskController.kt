package dreifa.app.tasks.ui.controllers

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TemplateProcessor
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import java.lang.RuntimeException

class ExecuteTaskController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val serialiser = JsonSerialiser()
    fun handle(request: Request): Response {
        try {
            val taskName = request.path("task")!!
            val exampleNumber = request.query("example")!!.toInt()

            val model = HashMap<String, Any>()
            model["name"] = taskName

            val task = taskFactory.createInstance(taskName)
            when (task) {
                is BlockingTask<*, *> -> {
                    model["type"] = "Blocking"
                }
                is AsyncTask<*, *> -> {
                    model["type"] = "Async"
                }
            }

            checkForTaskDocs(taskName, exampleNumber, model)

            val reflections = TaskReflections(task::class)
            model["inputClazz"] = reflections.paramClass().qualifiedName!!
            model["outputClazz"] = reflections.resultClass().qualifiedName!!

            val html = TemplateProcessor().renderMustache("tasks/execute.html", mapOf("task" to model))
            return Response(Status.OK).body(html)
        } catch (ex: Exception) {
            return Response(Status.INTERNAL_SERVER_ERROR).body(ex.message!!)
        }
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