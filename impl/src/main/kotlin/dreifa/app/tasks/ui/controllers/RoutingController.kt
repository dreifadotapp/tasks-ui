package dreifa.app.tasks.ui.controllers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.controllers.providers.*
import dreifa.app.tasks.ui.controllers.ses.AllEventsController
import dreifa.app.tasks.ui.controllers.tasks.*
import org.http4k.core.*
import org.http4k.routing.*

class RoutingController(registry: Registry, vHost: String) : HttpHandler {
    override fun invoke(p: Request) = routes(p)

    private val routes: RoutingHttpHandler = routes(
        "/" bind Method.GET to {
            Response(Status.TEMPORARY_REDIRECT).header("Location", "$vHost/home")
        },
        "/static" bind static(ResourceLoader.Classpath("www")),

        "/events/all" bind Method.GET to {
            AllEventsController(registry).handle(it)
        },

        "/home" bind Method.GET to {
            val html = TemplateProcessor().renderMustache("home.html",
                mapOf("message" to "foobar"),
            "home.html")
            Response(Status.OK).body(html)
        },

        "/providers" bind Method.GET to {
            ListProvidersController(registry).handle(it)
        },

        "/providers/startRegistration" bind Method.GET to {
            RegisterProviderController().handle(it)
        },

        "/providers/doScan" bind Method.POST to {
            DoScanJarController(registry).handle(it)
        },

        "/providers/{bundleId}/register/{providerClass}" bind Method.GET to {
            ConfirmRegisterProviderController().handle(it)
        },

        "/providers/{bundleId}/doRegister/{providerClass}" bind Method.POST to {
            DoRegisterProviderController(registry).handle(it)
        },

        "/providers/{providerId}" bind Method.GET to {
            ViewProviderController(registry).handle(it)
        },

        "/tasks" bind Method.GET to {
            ListTasksController(registry).handle(it)
        },

        "/tasks/{providerId}/{task}/execute" bind Method.GET to {
            ExecuteTaskController(registry).handle(it)
        },

        "/tasks/{providerId}/{task}/doExecute" bind Method.POST to {
            DoExecuteTaskController(registry).handle(it)
        },

        "/tasks/view" bind Method.GET to {
            // the case when there is no active task
            Response(Status.TEMPORARY_REDIRECT).header("Location", "$vHost/tasks")
        },

        "/tasks/{providerId}/{task}/view" bind Method.GET to {
            ViewTaskController(registry).handle(it)
        }
    )
}
