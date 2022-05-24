package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.tasks.ui.tasks.TaskInfos
import dreifa.app.types.NotRequired
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ListTasksController(registry: Registry) : BaseController(registry) {
    private val internalTasks = registry.get(InternalOnlyTaskClient::class.java)
    override fun handle(req: Request): Response {
        val trc = TelemetryRequestContext(req, "/tasks")
        return runWithTelemetry(trc) { span ->
            val clientContext = clientContextWithTelemetry(span)
            val model = buildBaseModel(req)
            setMenuFlags(model, "tsk", "list_tsk")

            val tasks = internalTasks.client.execBlocking(
                clientContext,
                TaskNames.UIListTasksTask,
                NotRequired.instance(),
                TaskInfos::class
            )
            model["tasks"] = tasks

            val html = templateEngine().renderMustache("tasks/list.html", model)
            Response(Status.OK).body(html)
        }
    }
}