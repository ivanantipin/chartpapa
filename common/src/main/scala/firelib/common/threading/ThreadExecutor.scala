package firelib.common.threading

/**

 */
trait ThreadExecutor {

    def execute(task: () => Unit)

    def start(): ThreadExecutor

    def shutdown()
}
