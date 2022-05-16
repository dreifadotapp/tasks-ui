package dreifa.app.tasks.ui.tasks

import dreifa.app.registry.Registry
import dreifa.app.tasks.BlockingTask
import dreifa.app.tasks.NotRemotableTask
import dreifa.app.tasks.client.TaskClient
import dreifa.app.tasks.executionContext.ExecutionContext
import dreifa.app.tasks.ui.InternalOnlyTaskClient
import dreifa.app.types.UniqueId

class UITaskClientTask(registry: Registry) : BlockingTask<UniqueId, ClassLoader>, NotRemotableTask {
    private val taskClient = registry.get(TaskClient::class.java)
    private val internalTaskClient = registry.get(InternalOnlyTaskClient::class.java).client

    override fun exec(ctx: ExecutionContext, input: UniqueId): ClassLoader {
        TODO("Not yet implemented")
    }
}