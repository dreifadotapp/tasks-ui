package dreifa.app.tasks.ui

import dreifa.app.registry.Registry
import dreifa.app.ses.InMemoryEventStore
import dreifa.app.sks.SimpleKVStore
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.demo.DemoTasks
import dreifa.app.tasks.logging.CapturedOutputStream
import dreifa.app.tasks.logging.InMemoryLogging
import dreifa.app.tasks.ui.controllers.RoutingController

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

    val registry = Registry()
    val es = InMemoryEventStore()
    val sks = SimpleKVStore()
    val logConsumerContext = InMemoryLogging()
    val captured = CapturedOutputStream(logConsumerContext)

    registry.store(es).store(sks).store(logConsumerContext).store(captured)

    val taskFactory = TaskFactory(registry)
    taskFactory.register(DemoTasks())
    registry.store(taskFactory)

    val app = RoutingController(registry, vhost)
    val server = app.asServer(SunHttp(port.toInt()))
    println("Starting server on port $port")
    server.start()
}


