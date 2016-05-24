class First {
    val x: Int

    init {
        x = foo()
    }

    fun foo() = x
}

abstract class Second {
    val x: Int?

    init {
        x = foo()
    }

    abstract fun foo(): Int?
}

class SecondDerived : Second() {
    val y = 42

    override fun foo() = y
}

open class Third {
    open var x: Int

    constructor() {
        x = 42
    }
}

class ThirdDerived : Third() {
    override var x: Int
        set(arg) { field = arg + y }

    val y = 1
}
