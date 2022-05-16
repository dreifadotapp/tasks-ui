package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId

class SimpleSerialiserService(val registry: Registry) {
    private val internalTaskClient = registry.get(InternalOnlyTaskClient::class.java).client

    fun exec(ctx: ClientContext, providerId: UniqueId): JsonSerialiser {
        val loader = internalTaskClient.execBlocking(ctx,
            TaskNames.UIClassLoaderTask,
            providerId,
            ClassLoader::class)

        //val loader = classLoaderService.exec(ctx, providerId)
        return JsonSerialiser(loader)
    }

    fun exec(ctx: ClientContext, providerId: String) = exec(ctx, UniqueId.fromString(providerId))
}