package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.tasks.ui.services.ListTasksService
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ListTasksController(registry: Registry) : BaseController() {
    private val service = ListTasksService(registry)
    override fun handle(req: Request): Response {
        val model = buildBaseModel(req)
        setMenuFlags(model, "tsk", "list_tsk")

        model["tasks"] = service.exec(SimpleClientContext())
        val html = templateEngine().renderMustache("tasks/list.html", model)
        return Response(Status.OK).body(html)
    }
}