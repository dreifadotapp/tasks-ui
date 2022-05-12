package dreifa.app.tasks.ui.controllers

import dreifa.app.opentelemetry.OpenTelemetryProvider
import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.controllers.providers.*
import dreifa.app.tasks.ui.controllers.ses.AllEventsController
import dreifa.app.tasks.ui.controllers.tasks.*
import org.http4k.core.*
import org.http4k.routing.*

class RoutingController(registry: Registry, vHost: String) : HttpHandler {

    private val regWithTelemetry: Registry

    init {
        // setup OT if there is a provider
        regWithTelemetry = registry.clone()
        val provider = regWithTelemetry.getOrNull(OpenTelemetryProvider::class.java)
        if (provider != null) {
            val tracer = provider.sdk().getTracer("routing")
            regWithTelemetry.store(tracer)
        }
    }


    override fun invoke(p: Request) = routes(p)

    private val routes: RoutingHttpHandler = routes(
        "/" bind Method.GET to {
            Response(Status.TEMPORARY_REDIRECT).header("Location", "$vHost/home")
        },
        "/static" bind static(ResourceLoader.Classpath("www")),

        "/events/all" bind Method.GET to {
            AllEventsController(regWithTelemetry).handle(it)
        },

        "/home" bind Method.GET to {
            val html = TemplateProcessor().renderMustache(
                "home.html",
                mapOf("message" to "foobar"),
                "home.html"
            )
            Response(Status.OK).body(html)
        },

        "/providers" bind Method.GET to {
            ListProvidersController(regWithTelemetry).handle(it)
        },

        "/providers/startRegistration" bind Method.GET to {
            RegisterProviderController(regWithTelemetry).handle(it)
        },

        "/providers/doScan" bind Method.POST to {
            DoScanJarController(regWithTelemetry).handle(it)
        },

        "/providers/{bundleId}/register/{providerClass}" bind Method.GET to {
            ConfirmRegisterProviderController(regWithTelemetry).handle(it)
        },

        "/providers/{bundleId}/doRegister/{providerClass}" bind Method.POST to {
            DoRegisterProviderController(regWithTelemetry).handle(it)
        },

        "/providers/{providerId}" bind Method.GET to {
            ViewProviderController(regWithTelemetry).handle(it)
        },

        "/tasks" bind Method.GET to {
            ListTasksController(regWithTelemetry).handle(it)
        },

        "/tasks/{providerId}/{task}/execute" bind Method.GET to {
            ExecuteTaskController(regWithTelemetry).handle(it)
        },

        "/tasks/{providerId}/{task}/doExecute" bind Method.POST to {
            DoExecuteTaskController(regWithTelemetry).handle(it)
        },

        "/tasks/view" bind Method.GET to {
            // the case when there is no active task
            Response(Status.TEMPORARY_REDIRECT).header("Location", "$vHost/tasks")
        },

        "/tasks/{providerId}/{task}/view" bind Method.GET to {
            ViewTaskController(regWithTelemetry).handle(it)
        }
    )
}
