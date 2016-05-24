class First {
    val x: Int

    init {
        x = <!DEBUG_INFO_LEAKING_THIS!>foo()<!>
    }

    fun foo() = x
}

abstract class Second {
    val x: Int?

    init {
        x = <!DEBUG_INFO_LEAKING_THIS!>foo()<!>
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
        <!DEBUG_INFO_LEAKING_THIS!>x<!> = 42
    }
}

class ThirdDerived : Third() {
    <!MUST_BE_INITIALIZED!>override var x: Int<!>
        set(arg) { field = arg + y }

    val y = 1
}
