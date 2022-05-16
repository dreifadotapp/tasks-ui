package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPRegisterProviderRequest
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.UniqueId
import org.http4k.core.*
import org.http4k.core.body.form
import org.http4k.routing.path

class DoRegisterProviderController(registry: Registry) : BaseController(registry) {
    private val taskClient = registry.get(TaskClient::class.java)

    override fun handle(req: Request): Response {
        val trc = TelemetryRequestContext(req, "/providers/{bundleId}/doRegister/{providerClass}")
        return runWithTelemetry(trc) { tec ->
            val model = buildBaseModel(req)
            setMenuFlags(model, "prv", "reg_prv")

            val bundleId = req.path("bundleId")!!
            val providerClass = req.path("providerClass")!!
            val providerId = req.form("provider-id")!!
            val providerName = req.form("provider-name")!!

            val request = TPRegisterProviderRequest(
                jarBundleId = UniqueId.fromString(bundleId),
                providerId = UniqueId.fromString(providerId),
                providerClazz = providerClass,
                providerName = providerName
            )

            val clientContext = SimpleClientContext(telemetryContext = tec.otc.dto())
            taskClient.execBlocking(
                clientContext,
                TaskNames.TPRegisterProviderTask,
                request,
                Unit::class
            )

            // build the view
            model["providerName"] = providerClass
            model["providerId"] = providerId
            val html = templateEngine().renderMustache(
                "providers/registrationResult.html",
                model
            )
            Response(Status.OK).body(html)
        }
    }
}