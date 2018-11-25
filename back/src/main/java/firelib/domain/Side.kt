package firelib.common

enum class Side(val sign : Int){
    Sell(-1),Buy(1),None(0);

    fun sideForAmt(amt: Int): Side {
        assert(amt != 0, {"no side for zero amount !!"})
        return if (amt > 0) Buy else Sell
    }

    fun opposite(): Side {
        return when(this) {
            Side.None -> Side.None
            Side.Buy -> Side.Sell
            Side.Sell -> Side.Buy
        }
    }
}

