package dreifa.app.tasks.ui

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.readValue

import org.http4k.core.*
import org.http4k.routing.*
import org.http4k.routing.ResourceLoader.Companion.Classpath
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.io.File

fun main(args: Array<String>) {
    val port = System.getenv("TASKUI_PORT") ?: "8080"
    val vhost = System.getenv("TASKUI_VHOST") ?: "http://localhost:$port"
    val expectedConfig = File("config.yaml") // under docker we simply expect this to mapped to the working dir
    val testConfig = File("src/test/resources/config.yaml")

//    val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
//    val configYaml = if (expectedConfig.exists()) expectedConfig else testConfig
//
//    val config: DashboardConfig = mapper.readValue(configYaml)


    val app = RoutingController(vhost)
    val server =
        app.asServer(SunHttp(port.toInt()))
    println("Starting server on port $port")
    server.start()
}


class RoutingController(vHost: String) : HttpHandler {
    override fun invoke(p: Request) = routes(p)

    private val routes: RoutingHttpHandler = routes(
        "/" bind Method.GET to {
            Response(Status.TEMPORARY_REDIRECT).header("Location", "$vHost/home")
        },
        "/static" bind static(Classpath("www")),


        "/home" bind Method.GET to {
            val html = TemplateProcessor().renderMustache("home.html", mapOf("message" to "foobar"))
            Response(Status.OK).body(html)
        }
    )


}
