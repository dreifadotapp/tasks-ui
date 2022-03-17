package dreifa.app.tasks.ui.controllers.providers

import dreifa.app.fileBundle.BinaryBundleItem
import dreifa.app.fileBundle.adapters.TextAdapter
import dreifa.app.fileBundle.builders.FileBundleBuilder
import dreifa.app.registry.Registry
import dreifa.app.ses.EventStore
import dreifa.app.sis.JsonSerialiser
import dreifa.app.sks.SKS
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPScanJarRequest
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.types.StringList
import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfoList
import org.http4k.core.*
import java.io.File
import java.net.URL
import java.net.URLClassLoader


class DoRegisterProviderController(registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val serialiser = JsonSerialiser()
    private val sks = registry.get(SKS::class.java)
    private val ses = registry.get(EventStore::class.java)

    fun handle(request: Request): Response {
        try {
            val model = HashMap<String, Any>()

            val receivedForm = MultipartFormBody.from(request)
            val multipart = receivedForm.files("file1")[0]
            val f = File(multipart.filename)
            f.writeBytes(multipart.content.readAllBytes())

            val bundle = FileBundleBuilder()
                .withName("${multipart.filename} Bundle")
                .addItem(BinaryBundleItem.fromFile(f, multipart.filename))
                .build()

            val bundleAdapter = TextAdapter()

            val ctx = SimpleClientContext()
            taskClient.execBlocking(
                ctx,
                "dreifa.app.tasks.inbuilt.fileBundle.FBStoreTaskImpl",
                bundleAdapter.fromBundle(bundle),
                Unit::class
            )


            val scanRequest = TPScanJarRequest(bundle.id)

            val registrations = taskClient.execBlocking(
                ctx,
                "dreifa.app.tasks.inbuilt.providers.TPScanJarTaskImpl",
                scanRequest,
                StringList::class
            )


            val url = URL("file:${f.absolutePath}")

            val bytes = url.openStream().readAllBytes().size
            model["name"] = multipart.filename
            model["size"] = bytes
            model["registrations"] = registrations

            val html = TemplateProcessor().renderMustache("providers/registerResult.html", model)
            return Response(Status.OK).body(html)
        } catch (ex: Exception) {
            return Response(Status.INTERNAL_SERVER_ERROR).body(ex.message!!)
        }
    }

}