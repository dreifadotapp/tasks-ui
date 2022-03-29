package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.fileBundle.adapters.TextAdapter
import dreifa.app.registry.Registry
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPScanJarRequest
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.adapters.MultiPartRequestToFileBundleAdapter
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.StringList
import org.http4k.core.*

class DoScanJarController(registry: Registry) : BaseController() {
    private val taskClient = registry.get(TaskClient::class.java)

    override fun handle(request: Request): Response {
        val model = buildBaseModel(request)
        setMenuFlags(model, "prv","reg_prv")

        // build a FileBundle
        val requestAdapter = MultiPartRequestToFileBundleAdapter()
        val bundle = requestAdapter.toFileBundle(request)

        // store the FileBundle
        val bundleAdapter = TextAdapter()
        val ctx = SimpleClientContext()
        taskClient.execBlocking(
            ctx,
            TaskNames.FBStoreTask,
            bundleAdapter.fromBundle(bundle),
            Unit::class
        )

        // scan the Jar in the FileBundle
        val scanRequest = TPScanJarRequest(bundle.id)
        val registrations = taskClient.execBlocking(
            ctx,
            TaskNames.TPScanJarTask,
            scanRequest,
            StringList::class
        )

        // build the view
        model["name"] = bundle.items[0].path
        model["bundleId"] = bundle.id.toString()
        model["registrations"] = registrations
        val html = templateEngine().renderMustache("providers/scanJarResult.html", model)
        return Response(Status.OK).body(html)
    }

}