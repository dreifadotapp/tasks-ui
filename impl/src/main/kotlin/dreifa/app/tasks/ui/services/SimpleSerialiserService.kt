package dreifa.app.tasks.ui.services

import dreifa.app.registry.Registry
import dreifa.app.sis.JsonSerialiser
import dreifa.app.tasks.client.ClientContext
import dreifa.app.types.UniqueId

class SimpleSerialiserService(val registry: Registry) {
    private val classLoaderService = ClassLoaderService(registry)

    fun exec(ctx: ClientContext, providerId: UniqueId): JsonSerialiser {
        val loader = classLoaderService.exec(ctx, providerId)
        return JsonSerialiser(loader)
    }

    fun exec(ctx: ClientContext, providerId: String) = exec(ctx, UniqueId.fromString(providerId))
}