package firelib.common

object OrderStatus {
    val New = new OrderStatus("New")
    val Accepted = new OrderStatus("Accepted")
    val PendingCancel = new OrderStatus("PendingCancel")
    val CancelFailed = new OrderStatus("CancelFailed")
    val Done = new OrderStatus("Done")
    val Rejected = new OrderStatus("Rejected")
    val Cancelled = new OrderStatus("Cancelled")

}

sealed case class OrderStatus private (val Name: String) {

    def isFinal: Boolean = this == OrderStatus.Rejected || this == OrderStatus.Done || this == OrderStatus.Cancelled

    def isPending: Boolean = this == OrderStatus.New || this == OrderStatus.PendingCancel

    def isLiveAccepted: Boolean = this == OrderStatus.Accepted

    override def toString: String = Name

}
