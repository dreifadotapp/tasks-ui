package dreifa.app.tasks.ui.controllers.tasks

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.*
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TemplateProcessor
import dreifa.app.tasks.ui.controllers.BaseController
import dreifa.app.tasks.ui.services.ClassLoaderService
import dreifa.app.tasks.ui.services.TaskClientService
import dreifa.app.tasks.ui.services.TaskFactoryService
import dreifa.app.types.UniqueId
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import java.lang.RuntimeException

class ViewTaskController(registry: Registry) : BaseController() {
    private val classLoaderService = ClassLoaderService(registry)
    private val serialiser = JsonSerialiser()
    private val taskFactoryService = TaskFactoryService(registry)
    private val taskClientService = TaskClientService(registry)

    override fun handle(request: Request): Response {
        val taskName = request.path("task")!!
        val providerId = request.path("providerId")!!

        val model = buildBaseModel(request)
        setMenuFlags(model, "tsk", "view_tsk")
        setActiveTask(model, providerId, taskName)

        val taskModel = HashMap<String, Any>()
        taskModel["name"] = taskName
        taskModel["providerId"] = providerId

        val ctx = SimpleClientContext()
        val loader = classLoaderService.exec(ctx,UniqueId.fromString(providerId))
        val taskFactory = taskFactoryService.exec(ctx, UniqueId.fromString(providerId))
        val taskClient = taskClientService.exec(ctx, UniqueId.fromString(providerId))

        val task = taskFactory.createInstance(taskName)
        when (task) {
            is BlockingTask<*, *> -> {
                taskModel["type"] = "Blocking"
            }
            is AsyncTask<*, *> -> {
                taskModel["type"] = "Async"
            }
        }

        checkForTaskDocs(taskClient, taskName, taskModel)

        val reflections = TaskReflections(task::class)
        taskModel["inputClazz"] = reflections.paramClass().qualifiedName!!
        taskModel["outputClazz"] = reflections.resultClass().qualifiedName!!
        model["task"] = taskModel

        val html = TemplateProcessor().renderMustache("tasks/view.html", model)
        return Response(Status.OK).body(html)
    }

    private fun checkForTaskDocs(taskClient: TaskClient, task: String, model: MutableMap<String, Any>) {
        try {
            val docs = taskClient.taskDocs<Any, Any>(
                SimpleClientContext(),
                task
            )
            model["hasTaskDoc"] = true
            model["description"] = docs.description()
            model["examples"] = docs.examples().mapIndexed { index, example -> examplesPresenter(index, example) }

        } catch (ignoreMe: RuntimeException) {
        }
    }

    private fun examplesPresenter(index: Int, example: TaskExample<Any, Any>): Map<String, Any> {
        val model = HashMap<String, Any>()
        if (example.input() != null) {
            model["hasInput"] = true
            model["input"] = example.input()!!
            model["inputAsJson"] = serialiser.toPacketPayload(example.input()!!.example) ?: ""
        } else {
            model["hasInput"] = false
        }
        if (example.output() != null) {
            model["hasOutput"] = true
            model["output"] = example.output()!!
            model["outputAsJson"] = serialiser.toPacketPayload(example.output()!!) ?: ""
        } else {
            model["hasOutput"] = false
        }
        model["exampleNumber"] = index + 1
        model["description"] = example.description()
        return model
    }
}