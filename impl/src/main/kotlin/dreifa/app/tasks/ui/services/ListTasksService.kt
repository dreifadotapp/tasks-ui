package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId

data class TaskInfo(
    val clazz: String,
    val providerId: UniqueId,
    val providerName: String,
    val inBuilt: Boolean
)

/**
 * A UI service to return all tasks
 */
class ListTasksService(val registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val listProvidersService = ListProvidersService(registry)

    fun exec(ctx: ClientContext): List<TaskInfo> {
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