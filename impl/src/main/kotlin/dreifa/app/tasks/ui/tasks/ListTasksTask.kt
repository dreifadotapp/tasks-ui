package dreifa.app.tasks.ui.tasks

import dreifa.app.registry.Registry
import dreifa.app.tasks.BlockingTask
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.executionContext.ExecutionContext
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.services.ListProvidersService
import dreifa.app.types.NotRequired
import dreifa.app.types.UniqueId

data class TaskInfo(
    val clazz: String,
    val providerId: UniqueId,
    val providerName: String,
    val inBuilt: Boolean
)

class TaskInfos(data: List<TaskInfo>) : ArrayList<TaskInfo>(data)

/**
 * A internal task to list all available tasks
 */
class ListTasksTask(val registry: Registry) : BlockingTask<NotRequired,TaskInfos>{
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val listProvidersService = ListProvidersService(registry)

    override fun exec(ctx: ExecutionContext, input: NotRequired): TaskInfos {
        val clientContext = SimpleClientContext(telemetryContext = ctx.telemetryContext().dto())
        return TaskInfos(doExec(clientContext))
    }

    private fun doExec(ctx: ClientContext): List<TaskInfo> {
        val results = ArrayList<TaskInfo>()

        listProvidersService.exec(ctx).forEach { provider ->
            if (provider.inbuilt) {
                results.addAll(taskFactory
                    .list()
                    .map {
                        TaskInfo(
                            clazz = it,
                            providerId = provider.providerId,
                            providerName = provider.name,
                            inBuilt = true
                        )
                    })
            } else {
                val providerTaskFactory = taskClient.execBlocking(
                    ctx,
                    TaskNames.TPLoadTaskFactoryTask,
                    provider.providerId,
                    TaskFactory::class
                )
                results.addAll(providerTaskFactory
                    .list()
                    .map {
                        TaskInfo(
                            clazz = it,
                            providerId = provider.providerId,
                            providerName = provider.name,
                            inBuilt = false
                        )
                    })
            }
        }

        return results.sortedBy { it.clazz }
    }

}