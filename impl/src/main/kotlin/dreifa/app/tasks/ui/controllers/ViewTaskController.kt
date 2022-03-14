package dreifa.app.tasks.ui.controllers

import dreifa.app.registry.Registry
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.SimpleTaskClient
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TemplateProcessor
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import java.lang.RuntimeException

class ViewTaskController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    fun handle(request: Request): Response {
        try {
            val model = HashMap<String, Any>()
            val taskName = request.path("task")!!
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

            checkForTaskDocs(taskName, model)

            val reflections = TaskReflections(task::class)
            model["inputClazz"] = reflections.paramClass()
            model["outputClazz"] = reflections.resultClass()

            val html = TemplateProcessor().renderMustache("tasks/view.html", mapOf("task" to model))
            return Response(Status.OK).body(html)
        } catch (ex: Exception) {
            return Response(Status.INTERNAL_SERVER_ERROR).body(ex.message!!)
        }
    }

    private fun checkForTaskDocs(task: String, model: HashMap<String, Any>) {
        try {
            val docs = taskClient.taskDocs<Any, Any>(
                SimpleClientContext(),
                task
            )
            model["hasTaskDoc"] = true
            model["description"] = docs.description()
            model["examples"] = docs.examples()

        } catch (ignoreMe: RuntimeException) {
        }
    }
}