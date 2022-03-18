package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.fileBundle.adapters.TextAdapter
import dreifa.app.registry.Registry
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPScanJarRequest
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.adapters.MulitPartRequestToFileBundleAdapter
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.types.StringList
import org.http4k.core.*

class DoRegisterProviderController(registry: Registry) : BaseController() {
    private val taskClient = registry.get(TaskClient::class.java)

    fun handle(request: Request): Response {
        val model = buildBaseModel(request)

        // build a FileBundle
        val requestAdapter = MulitPartRequestToFileBundleAdapter()
        val bundle = requestAdapter.toFileBundle(request)

        // store the FileBundle
        val bundleAdapter = TextAdapter()
        val ctx = SimpleClientContext()
        taskClient.execBlocking(
            ctx,
            "dreifa.app.tasks.inbuilt.fileBundle.FBStoreTaskImpl",
            bundleAdapter.fromBundle(bundle),
            Unit::class
        )

        // scan the Jar in the FileBundle
        val scanRequest = TPScanJarRequest(bundle.id)
        val registrations = taskClient.execBlocking(
            ctx,
            "dreifa.app.tasks.inbuilt.providers.TPScanJarTaskImpl",
            scanRequest,
            StringList::class
        )

        // build the view
        model["name"] = bundle.items[0].path
        model["registrations"] = registrations
        val html = TemplateProcessor().renderMustache("providers/registerResult.html", model)
        return Response(Status.OK).body(html)

    }

}