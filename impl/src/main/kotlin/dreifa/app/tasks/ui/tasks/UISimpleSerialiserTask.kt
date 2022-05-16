package dreifa.app.tasks.ui.tasks

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.BlockingTask
import dreifa.app.tasks.NotRemotableTask
import dreifa.app.tasks.client.ClientContext
import dreifa.app.tasks.client.SimpleClientContext
import dreifa.app.tasks.executionContext.ExecutionContext
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.tasks.ui.TaskNames
import dreifa.app.types.UniqueId

class UISimpleSerialiserTask(val registry: Registry) : BlockingTask<UniqueId, JsonSerialiser>, NotRemotableTask {
    private val internalTaskClient = registry.get(InternalOnlyTaskClient::class.java).client

    override fun exec(ctx: ExecutionContext, input: UniqueId): JsonSerialiser {
        val clientContext = SimpleClientContext(telemetryContext = ctx.telemetryContext().dto())
        return doExec(clientContext, input)
    }

    private fun doExec(ctx: ClientContext, providerId: UniqueId): JsonSerialiser {
        val loader = internalTaskClient.execBlocking(
            ctx,
            TaskNames.UIClassLoaderTask,
            providerId,
            ClassLoader::class
        )

        return JsonSerialiser(loader)
    }

}