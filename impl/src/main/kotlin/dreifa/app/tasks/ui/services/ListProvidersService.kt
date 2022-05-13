package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.inbuilt.providers.TPQueryParams
import dreifa.app.tasks.inbuilt.providers.TPQueryResult
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId

data class ProviderInfo(
    val providerId: UniqueId,
    val name: String,
    val clazz: String,
    val inbuilt: Boolean
)

@Deprecated(message = "use ListProvidersTask")
class ListProvidersService(val registry: Registry) {
    private val taskClient = registry.get(TaskClient::class.java)

    fun exec(ctx: ClientContext): List<ProviderInfo> {
        val providers = taskClient.execBlocking(
            ctx,
            TaskNames.TPQueryTask,
            TPQueryParams(),
            TPQueryResult::class
        ).map { ProviderInfo(it.providerId, it.providerName, it.providerClazz, false) }.toMutableList()
        providers.add(ProviderInfo(InBuiltProviderId, "Built In", "", true))
        return providers
    }

    companion object {
        val InBuiltProviderId = UniqueId.fromString("inbuilt")
    }
}