package firelib.common

enum class OrderStatus {
    New, Accepted, PendingCancel, CancelFailed, Done, Rejected, Cancelled;

    fun isFinal(): Boolean = this == OrderStatus.Rejected || this == OrderStatus.Done || this == OrderStatus.Cancelled
    fun isPending(): Boolean = this == OrderStatus.New || this == OrderStatus.PendingCancel
    fun isLiveAccepted(): Boolean = this == OrderStatus.Accepted
}