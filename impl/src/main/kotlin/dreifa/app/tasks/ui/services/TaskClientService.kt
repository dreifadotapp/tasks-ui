package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.SimpleTaskClient
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId

class TaskClientService(val registry: Registry) {
    private val taskClient = registry.get(TaskClient::class.java)
    private val listProvidersService = ListProvidersService(registry)

    fun exec(ctx: ClientContext, providerId: UniqueId): TaskClient {

        val provider = listProvidersService
            .exec(ctx)
            .single { it.providerId == providerId }
        return if (provider.inbuilt) {
            taskClient
        } else {
            val providerTaskFactory = taskClient.execBlocking(
                ctx,
                TaskNames.TPLoadTaskFactoryTask,
                provider.providerId,
                TaskFactory::class
            )

            val localRegistry = registry.clone()
            localRegistry.store(providerTaskFactory)
            SimpleTaskClient(localRegistry)
        }
    }

}