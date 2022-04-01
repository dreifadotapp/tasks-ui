package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId

class TaskFactoryService(val registry: Registry) {
    private val taskFactory = registry.get(TaskFactory::class.java)
    private val taskClient = registry.get(TaskClient::class.java)
    private val listProvidersService = ListProvidersService(registry)

    fun exec(ctx: ClientContext, providerId: UniqueId): TaskFactory {

        val provider = listProvidersService
            .exec(ctx)
            .single { it.providerId == providerId }
        return if (provider.inbuilt) {
            taskFactory
        } else {
            taskClient.execBlocking(
                ctx,
                TaskNames.TPLoadTaskFactoryTask,
                provider.providerId,
                TaskFactory::class
            )
        }
    }
}