package firelib.core.domain

enum class OrderStatus {
    New, Accepted, PendingCancel, CancelFailed, Done, Rejected, Cancelled;

    fun isFinal(): Boolean = this == Rejected || this == Done || this == Cancelled
    fun isPending(): Boolean = this == New || this == PendingCancel
    fun isLiveAccepted(): Boolean = this == Accepted
}