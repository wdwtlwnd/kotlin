interface CollectionWithRemove<out E> : Collection<E> {
    fun remove(element: @UnsafeVariance E): CollectionWithRemove<E>
}

class CollWithRemoveImpl<E> : CollectionWithRemove<E> {
    override val size: Int
        get() = throw UnsupportedOperationException()

    override fun contains(element: E): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsAll(elements: Collection<E>): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isEmpty(): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun iterator(): Iterator<E> {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun remove(element: E): CollectionWithRemove<E> = this
}

fun box(): String {
    val c = CollWithRemoveImpl<String>()
    if (c.remove("") !== c) return "fail"
    return "OK"
}
