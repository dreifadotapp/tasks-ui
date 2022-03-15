package dreifa.app.tasks.ui

import dreifa.app.registry.Registry
import dreifa.app.ses.InMemoryEventStore
import dreifa.app.sks.SimpleKVStore
import dreifa.app.tasks.DefaultAsyncResultChannelSinkFactory
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.client.SimpleTaskClient
import dreifa.app.tasks.demo.DemoTasks
import dreifa.app.tasks.demo.echo.EchoTasks
import dreifa.app.tasks.logging.CapturedOutputStream
import dreifa.app.tasks.logging.DefaultLoggingChannelFactory
import dreifa.app.tasks.logging.InMemoryLogging
import dreifa.app.tasks.ui.controllers.RoutingController

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

    // base services
    val registry = Registry()
    val es = InMemoryEventStore()
    val sks = SimpleKVStore()
    val logConsumerContext = InMemoryLogging()
    val captured = CapturedOutputStream(logConsumerContext)
    registry.store(es).store(sks).store(logConsumerContext).store(captured)

    // wirein logging channel
    val logChannelFactory = DefaultLoggingChannelFactory(registry)
    registry.store(logChannelFactory)

    registry.store(DefaultAsyncResultChannelSinkFactory())

    // wire in TaskFactory
    val taskFactory = TaskFactory(registry)
    taskFactory.register(DemoTasks())
    taskFactory.register(EchoTasks())
    registry.store(taskFactory)

    // wire in TaskClient
    registry.store(SimpleTaskClient(registry))

    val app = RoutingController(registry, vhost)
    val server = app.asServer(SunHttp(port.toInt()))
    println("Starting server on port $port")
    server.start()
}


