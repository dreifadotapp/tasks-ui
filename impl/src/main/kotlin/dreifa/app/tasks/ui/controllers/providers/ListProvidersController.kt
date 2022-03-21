package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPQueryParams
import dreifa.app.tasks.inbuilt.providers.TPQueryResult
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.controllers.BaseController
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status

class ListProvidersController(val registry: Registry) : BaseController() {
    private val taskClient = registry.get(TaskClient::class.java)

    override fun handle(req: Request): Response {
        val model = buildBaseModel(req)
        setMenuFlags(model, "prv","lst_prv")

        val ctx = SimpleClientContext()
        model["providers"] = taskClient.execBlocking(
            ctx,
            TaskNames.TPQueryTask,
            TPQueryParams(),
            TPQueryResult::class
        )

        val html = templateEngine().renderMustache("providers/list.html", model)
        return Response(Status.OK).body(html)
    }
}