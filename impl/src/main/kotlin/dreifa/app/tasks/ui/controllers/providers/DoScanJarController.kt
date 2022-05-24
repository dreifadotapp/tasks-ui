package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.fileBundle.adapters.TextAdapter
import dreifa.app.registry.Registry
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPScanJarRequest
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.adapters.MultiPartRequestToFileBundleAdapter
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.StringList
import org.http4k.core.*

class DoScanJarController(registry: Registry) : BaseController(registry) {
    private val taskClient = registry.get(TaskClient::class.java)

    override fun handle(req: Request): Response {
        val trc = TelemetryRequestContext(req, "/providers/doScan")
        return runWithTelemetry(trc) { span ->
            val clientContext = clientContextWithTelemetry(span)
            val model = buildBaseModel(req)
            setMenuFlags(model, "prv", "reg_prv")

            // build a FileBundle
            val requestAdapter = MultiPartRequestToFileBundleAdapter()
            val bundle = requestAdapter.toFileBundle(req)

            // store the FileBundle
            val bundleAdapter = TextAdapter()
            taskClient.execBlocking(
                clientContext,
                TaskNames.FBStoreTask,
                bundleAdapter.fromBundle(bundle),
                Unit::class
            )

            // scan the Jar in the FileBundle
            val scanRequest = TPScanJarRequest(bundle.id)
            val registrations = taskClient.execBlocking(
                clientContext,
                TaskNames.TPScanJarTask,
                scanRequest,
                StringList::class
            )

            // build the view
            model["name"] = bundle.items[0].path
            model["bundleId"] = bundle.id.toString()
            model["registrations"] = registrations
            val content = templateEngine().renderMustache("providers/scanJarResult.html", model)
            html(content)
        }
    }

}