package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.tasks.ui.tasks.ProviderInfos
import dreifa.app.types.NotRequired
import org.http4k.core.Request
import org.http4k.core.Response

class ListProvidersController(registry: Registry) : BaseController(registry) {
    private val internalTasks = registry.get(InternalOnlyTaskClient::class.java)
    override fun handle(req: Request): Response {
        val trc = TelemetryRequestContext(req, "/providers")
        return runWithTelemetry(trc) { tec ->
            val clientContext = SimpleClientContext(telemetryContext = tec.otc.dto())

            val model = buildBaseModel(req)
            setMenuFlags(model, "prv", "list_prv")

            val providers = internalTasks.client.execBlocking(
                clientContext,
                TaskNames.UIListProvidersTask,
                NotRequired.instance(),
                ProviderInfos::class
            )

            model["providers"] = providers

            val content = templateEngine().renderMustache("providers/list.html", model)
            html(content)
        }
    }
}