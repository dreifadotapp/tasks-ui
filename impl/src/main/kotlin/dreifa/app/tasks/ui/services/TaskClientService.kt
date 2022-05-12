package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.SimpleTaskClient
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId

class TaskClientService(private val registry: Registry) {
    private val taskClient = registry.get(TaskClient::class.java)
    private val listProvidersService = ListProvidersService(registry)
    fun exec(ctx: ClientContext, providerId: UniqueId): TaskClient {

        val provider = listProvidersService
            .exec(ctx)
            .single { it.providerId == providerId }
        return if (provider.inbuilt) {
            taskClient
        } else {
            // A TaskFactory with the right classloader
            val providerTaskFactory = taskClient.execBlocking(
                ctx,
                TaskNames.TPLoadTaskFactoryTask,
                provider.providerId,
                TaskFactory::class
            )

            // A TaskClient with the right classloader
            val clazzLoader = ClassLoaderService(registry).exec(ctx, provider.providerId)
            val localRegistry = registry.clone()
            localRegistry.store(providerTaskFactory)
            SimpleTaskClient(localRegistry, clazzLoader)
        }
    }

    fun exec(ctx: ClientContext, providerId: String) = exec(ctx, UniqueId.fromString(providerId))
}