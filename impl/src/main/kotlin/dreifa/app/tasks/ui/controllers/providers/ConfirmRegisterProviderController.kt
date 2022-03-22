package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.registry.Registry
import dreifa.app.ses.EventStore
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPRegisterProviderRequest
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.UniqueId
import org.http4k.core.*
import org.http4k.routing.path

class ConfirmRegisterProviderController(registry: Registry) : BaseController() {
    private val taskClient = registry.get(TaskClient::class.java)
    private val ses = registry.get(EventStore::class.java)

    override fun handle(request: Request): Response {
        val model = buildBaseModel(request)

        setMenuFlags(model, "prv", "reg_prv")

        val bundleId = request.path("bundleId")!!
        val providerClass = request.path("providerClass")!!
        val providerId = UniqueId.alphanumeric()


        // build the view
        model["bundleId"] = bundleId
        model["providerClass"] = providerClass
        model["providerId"] = providerId
        val html = templateEngine().renderMustache(
            "providers/confirmRegistration.html",
            model
        )
        return Response(Status.OK).body(html)
    }
}