package firelib.common.threading

/**

 */
interface ThreadExecutor {

    fun execute(task: () -> Unit)

    fun start(): ThreadExecutor

    fun shutdown()
}