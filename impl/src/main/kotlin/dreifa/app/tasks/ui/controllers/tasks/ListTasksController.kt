package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ListTasksController(registry: Registry) : BaseController() {
    private val taskFactory = registry.get(TaskFactory::class.java)
    override fun handle(req: Request): Response {
        val model = buildBaseModel(req)
        model["tasks"] = taskFactory.list().sortedBy { it }
        val html = templateEngine().renderMustache("tasks/list.html", model)
        return Response(Status.OK).body(html)
    }
}