package dreifa.app.tasks.ui

object TaskNames {
    // TaskProvider (TP)
    val TPScanJarTask = "dreifa.app.tasks.inbuilt.providers.TPScanJarTask"
    val TPRegisterProviderTask = "dreifa.app.tasks.inbuilt.providers.TPRegisterProviderTask"
    val TPQueryTask = "dreifa.app.tasks.inbuilt.providers.TPQueryTask"
    val TPLoadTaskFactoryTask= "dreifa.app.tasks.inbuilt.providers.TPLoadTaskFactoryTask"
    val TPInfoTask = "dreifa.app.tasks.inbuilt.providers.TPInfoTask"

    // FileBundle (FB) Tasks
    val FBStoreTask = "dreifa.app.tasks.inbuilt.fileBundle.FBStoreTask"

    // ClassLoader
    val CLLoadJarTask = "dreifa.app.tasks.inbuilt.classloader.CLLoadJarTask"

    // Internal Task - not exposed to clients
    val ListProvidersTask = "dreifa.app.tasks.ui.tasks.ListProvidersTask"
}