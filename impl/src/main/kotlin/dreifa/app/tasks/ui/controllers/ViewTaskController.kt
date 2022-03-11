package dreifa.app.tasks.ui.controllers

import dreifa.app.registry.Registry
import dreifa.app.tasks.AsyncTask
import dreifa.app.tasks.BlockingTask
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.TaskReflections
import dreifa.app.tasks.ui.TemplateProcessor
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path

class ViewTaskController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    fun handle(request: Request): Response {
        try {
            val model = HashMap<String, Any>()
            val taskName = request.path("task")!!
            model["name"] = taskName

            val task = taskFactory.createInstance(taskName)
            when (task) {
                is BlockingTask<*, *> -> {

                }
                is AsyncTask<*, *> -> {

                }
            }

            val reflections = TaskReflections(task::class)
            model["inputClazz"] = reflections.paramClass()
            model["outputClazz"] = reflections.resultClass()


            val html = TemplateProcessor().renderMustache("tasks/view.html", mapOf("task" to model))
            return Response(Status.OK).body(html)
        }
        catch (ex : Exception){
            return Response(Status.INTERNAL_SERVER_ERROR).body(ex.message!!)
        }
    }
}