package dreifa.app.tasks.ui.controllers

import dreifa.app.registry.Registry
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.ui.TemplateProcessor
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ListTasksController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    fun handle(request: Request): Response {
        val tasks = taskFactory.list()
        val html = TemplateProcessor().renderMustache("tasks/list.html", mapOf("tasks" to tasks))
        return Response(Status.OK).body(html)
    }
}