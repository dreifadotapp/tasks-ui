package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPInfoResult
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.tasks.ui.tasks.ProviderInfos
import dreifa.app.types.NotRequired
import dreifa.app.types.UniqueId
import java.net.URLClassLoader

class ClassLoaderService(val registry: Registry) {
    private val taskClient = registry.get(TaskClient::class.java)
    private val internalTaskClient = registry.get(InternalOnlyTaskClient::class.java).client

    fun exec(ctx: ClientContext, providerId: UniqueId): ClassLoader {

        val providers = internalTaskClient.execBlocking(
            ctx,
            TaskNames.UIListProvidersTask,
            NotRequired.instance(),
            ProviderInfos::class
        )
        val provider = providers.single { it.providerId == providerId }

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