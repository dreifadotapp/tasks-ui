package dreifa.app.tasks.ui.tasks

import dreifa.app.registry.Registry
import dreifa.app.tasks.BlockingTask
import dreifa.app.tasks.NotRemotableTask
import dreifa.app.tasks.TaskFactory
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.client.SimpleTaskClient
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.executionContext.ExecutionContext
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.NotRequired
import dreifa.app.types.UniqueId

class UITaskClientTask(private val registry: Registry) : BlockingTask<UniqueId, TaskClient>, NotRemotableTask {
    private val taskClient = registry.get(TaskClient::class.java)
    private val internalTaskClient = registry.get(InternalOnlyTaskClient::class.java).client

    override fun exec(ctx: ExecutionContext, input: UniqueId): TaskClient {
        val clientContext = SimpleClientContext(telemetryContext = ctx.telemetryContext().dto())
        return doExec(clientContext, input)
    }

    private fun doExec(ctx: ClientContext, providerId: UniqueId): TaskClient {
        val providers = internalTaskClient.execBlocking(
            ctx,
            TaskNames.UIListProvidersTask,
            NotRequired.instance(),
            ProviderInfos::class
        )

        val provider = providers
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

            val clazzLoader = internalTaskClient.execBlocking(
                ctx,
                TaskNames.UIClassLoaderTask,
                provider.providerId,
                ClassLoader::class
            )

            // A TaskClient with the right classloader
            val localRegistry = registry.clone()
            localRegistry.store(providerTaskFactory)
            SimpleTaskClient(localRegistry, clazzLoader)
        }
    }
}