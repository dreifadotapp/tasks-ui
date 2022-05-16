package dreifa.app.tasks.ui.tasks

import dreifa.app.registry.Registry
import dreifa.app.tasks.BlockingTask
import dreifa.app.tasks.NotRemotableTask
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.executionContext.ExecutionContext
import dreifa.app.tasks.inbuilt.providers.TPQueryParams
import dreifa.app.tasks.inbuilt.providers.TPQueryResult
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.NotRequired
import dreifa.app.types.UniqueId

data class ProviderInfo(
    val providerId: UniqueId,
    val name: String,
    val clazz: String,
    val inbuilt: Boolean
)

class ProviderInfos(data: List<ProviderInfo>) : ArrayList<ProviderInfo>(data)

class UIListProvidersTask(registry: Registry) : BlockingTask<NotRequired, ProviderInfos>, NotRemotableTask {
    private val taskClient = registry.get(TaskClient::class.java)

    override fun exec(ctx: ExecutionContext, input: NotRequired): ProviderInfos {
        val clientContext = SimpleClientContext(telemetryContext = ctx.telemetryContext().dto())
        return ProviderInfos(doExec(clientContext))
    }

    private fun doExec(ctx: ClientContext): List<ProviderInfo> {
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