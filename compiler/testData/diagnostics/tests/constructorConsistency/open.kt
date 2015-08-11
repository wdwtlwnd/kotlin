open class Base {
    init {
        register(<!DEBUG_INFO_LEAKING_THIS!>this<!>)
        <!DEBUG_INFO_LEAKING_THIS!>foo()<!>
    }

    open fun foo() {}
}

fun register(arg: Base) {
    arg.foo()
}
