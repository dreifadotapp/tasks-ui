package dreifa.app.tasks.ui.controllers

import dreifa.app.registry.Registry
import dreifa.app.tasks.ui.TemplateProcessor
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

        "/tasks" bind Method.GET to {
            ListTasksController(registry).handle(it)
        },

        "/tasks/{task}/view" bind Method.GET to {
            ViewTaskController(registry).handle(it)
        }
    )


}
