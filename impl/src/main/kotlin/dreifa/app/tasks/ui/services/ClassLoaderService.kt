package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPInfoResult
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId
import java.net.URLClassLoader

class ClassLoaderService(val registry: Registry) {
    private val taskClient = registry.get(TaskClient::class.java)
    private val listProvidersService = ListProvidersService(registry)

    fun exec(ctx: ClientContext, providerId: UniqueId): ClassLoader {

        val provider = listProvidersService
            .exec(ctx)
            .single { it.providerId == providerId }
        return if (provider.inbuilt) {
            this::class.java.classLoader
        } else {
            val info = taskClient.execBlocking(
                ctx,
                TaskNames.TPInfoTask,
                provider.providerId,
                TPInfoResult::class
            )
            taskClient.execBlocking(
                ctx,
                TaskNames.CLLoadJarTask,
                info.jarBundleId,
                URLClassLoader::class
            )
        }
    }

    fun exec(ctx: ClientContext, providerId: String) = exec(ctx, UniqueId.fromString(providerId))
}