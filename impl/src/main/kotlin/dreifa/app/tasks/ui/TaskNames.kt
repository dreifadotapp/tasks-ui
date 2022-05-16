package dreifa.app.tasks.ui

object TaskNames {
    // TaskProvider (TP)
    const val TPScanJarTask = "dreifa.app.tasks.inbuilt.providers.TPScanJarTask"
    const val TPRegisterProviderTask = "dreifa.app.tasks.inbuilt.providers.TPRegisterProviderTask"
    const val TPQueryTask = "dreifa.app.tasks.inbuilt.providers.TPQueryTask"
    const val TPLoadTaskFactoryTask = "dreifa.app.tasks.inbuilt.providers.TPLoadTaskFactoryTask"
    const val TPInfoTask = "dreifa.app.tasks.inbuilt.providers.TPInfoTask"

    // FileBundle (FB) Tasks
    const val FBStoreTask = "dreifa.app.tasks.inbuilt.fileBundle.FBStoreTask"

    // ClassLoader
    const val CLLoadJarTask = "dreifa.app.tasks.inbuilt.classloader.CLLoadJarTask"

    // Internal Tasks - not exposed to clients
    const val UIListTasksTask = "dreifa.app.tasks.ui.tasks.UIListTasksTask"
    const val UIListProvidersTask = "dreifa.app.tasks.ui.tasks.UIListProvidersTask"
    const val UITaskClientTask = "dreifa.app.tasks.ui.tasks.UITaskClientTask"
    const val UIClassLoaderTask = "dreifa.app.tasks.ui.tasks.UIClassLoaderTask"
    const val UISimpleSerialiserTask = "dreifa.app.tasks.ui.tasks.UISimpleSerialiserTask"
    const val UITaskFactoryTask = "dreifa.app.tasks.ui.tasks.UITaskFactoryTask"

}