package blitz

fun <T, O: Any> caching(tiedGet: Provider<T>, calc: (T) -> O) = object : Lazy<O> {
    private var lastTiedV = tiedGet()
    private var lastV: O? = null

    override val value: O get() {
        val nTied = tiedGet()
        if (lastTiedV != nTied) {
            lastTiedV = nTied
            lastV = calc(nTied)
            return lastV!!
        }
        if (lastV == null)
            lastV = calc(nTied)
        return lastV!!
    }

    override fun isInitialized(): Boolean =
        lastTiedV == tiedGet() && lastV != null
}