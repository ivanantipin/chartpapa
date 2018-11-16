package firelib.common

object Direction {
    val Up = new Direction("Up")
    val Down = new Direction("Down")
    val None = new Direction("None")

}

sealed case class Direction private (val Name: String) {
    override def toString: String = Name
}


