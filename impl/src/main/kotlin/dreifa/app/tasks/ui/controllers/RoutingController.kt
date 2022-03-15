package dreifa.app.tasks.ui.controllers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.controllers.providers.DoRegisterProviderController
import dreifa.app.tasks.ui.controllers.providers.RegisterProviderController
import dreifa.app.tasks.ui.controllers.tasks.DoExecuteTaskController
import dreifa.app.tasks.ui.controllers.tasks.ExecuteTaskController
import dreifa.app.tasks.ui.controllers.tasks.ListTasksController
import dreifa.app.tasks.ui.controllers.tasks.ViewTaskController
import org.http4k.core.*
import org.http4k.routing.*

class RoutingController(registry: Registry, vHost: String) : HttpHandler {
    override fun invoke(p: Request) = routes(p)

    private val routes: RoutingHttpHandler = routes(
        "/" bind Method.GET to {
            Response(Status.TEMPORARY_REDIRECT).header("Location", "$vHost/home")
        },
        "/static" bind static(ResourceLoader.Classpath("www")),

        "/home" bind Method.GET to {
            val html = TemplateProcessor().renderMustache("home.html", mapOf("message" to "foobar"))
            Response(Status.OK).body(html)
        },

        "/providers/register" bind Method.GET to {
            RegisterProviderController(registry).handle(it)
        },

        "/providers/doRegister" bind Method.POST to {
            DoRegisterProviderController(registry).handle(it)
        },

        "/tasks" bind Method.GET to {
            ListTasksController(registry).handle(it)
        },

        "/tasks/{task}/execute" bind Method.GET to {
            ExecuteTaskController(registry).handle(it)
        },

        "/tasks/{task}/doExecute" bind Method.POST to {
            DoExecuteTaskController(registry).handle(it)
        },

        "/tasks/{task}/view" bind Method.GET to {
            ViewTaskController(registry).handle(it)
        }
    )


}
