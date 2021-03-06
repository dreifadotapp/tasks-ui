package dreifa.app.tasks.ui

import dreifa.app.opentelemetry.JaegerOpenTelemetryProvider
import dreifa.app.registry.Registry
import dreifa.app.ses.InMemoryEventStore
import dreifa.app.sks.SimpleKVStore
import dreifa.app.tasks.DefaultAsyncResultChannelSinkFactory
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.TestLocations
import dreifa.app.tasks.client.SimpleTaskClient
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.demo.DemoTasks
import dreifa.app.tasks.demo.echo.EchoTasks
import dreifa.app.tasks.inbuilt.InBuiltTasks
import dreifa.app.tasks.inbuilt.providers.TPLoadTaskFactoryTask
import dreifa.app.tasks.inbuilt.providers.TPLoadTaskFactoryTaskImpl
import dreifa.app.tasks.logging.CapturedOutputStream
import dreifa.app.tasks.logging.DefaultLoggingChannelFactory
import dreifa.app.tasks.logging.InMemoryLogging
import dreifa.app.tasks.ui.controllers.RoutingController
import dreifa.app.tasks.ui.tasks.*
import org.http4k.core.then
import org.http4k.filter.ServerFilters

import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val config = Config()
    val port = "8080"
    val vhost = "http://localhost:$port"
    //val expectedConfig = File("config.yaml") // under docker we simply expect this to mapped to the working dir
    //val testConfig = File("src/test/resources/config.yaml")

    // base services
    val registry = Registry()
    val provider = JaegerOpenTelemetryProvider(false, "app.dreifa.tasks-ui", config.jaegerEndpoint())
    val tracer = provider.sdk().getTracer("local-tasks")
    registry.store(provider).store(tracer)

    val es = InMemoryEventStore(registry)
    val sks = SimpleKVStore()
    val logConsumerContext = InMemoryLogging()
    val captured = CapturedOutputStream(logConsumerContext)
    val locations = TestLocations()

    registry.store(es)
        .store(sks)
        .store(logConsumerContext)
        .store(captured)
        .store(locations)
    //.store(provider)
    //.store(tracer)

    // wirein logging channel
    val logChannelFactory = DefaultLoggingChannelFactory(registry)
    registry.store(logChannelFactory)

    registry.store(DefaultAsyncResultChannelSinkFactory())

    // wire in TaskFactory
    val taskFactory = TaskFactory(registry)
    taskFactory.register(DemoTasks())
    taskFactory.register(EchoTasks())
    taskFactory.register(InBuiltTasks())
    registry.store(taskFactory)

    // wire in TaskClient
    registry.store(SimpleTaskClient(registry))

    // wire in the internal tasks that support the UI layer (these cannot be run externally)
    val taskFactoryInternal = TaskFactory(registry)
    taskFactoryInternal.register(UIListTasksTask::class)
    taskFactoryInternal.register(UIListProvidersTask::class)
    taskFactoryInternal.register(UIClassLoaderTask::class)
    taskFactoryInternal.register(UITaskClientTask::class)
    taskFactoryInternal.register(UISimpleSerialiserTask::class)
    taskFactoryInternal.register(UITaskFactoryTask::class)

    taskFactoryInternal.register(TPLoadTaskFactoryTaskImpl::class, TPLoadTaskFactoryTask::class)
    val taskClientInternal = SimpleTaskClient(registry.clone().store(taskFactoryInternal))
    registry.store(InternalOnlyTaskClient(taskClientInternal))

    val app =
        ServerFilters.CatchAll().then(
            RoutingController(registry, vhost)
        )

    val server = app.asServer(SunHttp(port.toInt()))
    println("Starting server on port $port")
    server.start()
}

// Allows the app to hold 2 TaskClients in the single registry
data class InternalOnlyTaskClient(val client: TaskClient)

