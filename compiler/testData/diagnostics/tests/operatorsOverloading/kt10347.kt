fun test(plus: Int.(String) -> Unit) {
    1 <!COMPLEX_CALL_TO_VARIABLE_AS_FUNCTION!>+<!> ""
}

class Foo
fun test2(foo: Foo, <!UNUSED_PARAMETER!>set<!>: Foo.(Int, Int) -> Int) {
    <!COMPLEX_CALL_TO_VARIABLE_AS_FUNCTION!>foo[1]<!> = 2
}

class Y {
    operator fun invoke() : Int = TODO()
}
class X  {
    val component1 : Y = TODO()
}
fun getX() : X = TODO()
fun test3() {
    val (<!UNUSED_VARIABLE!>a<!>:Int) = <!COMPONENT_FUNCTION_MISSING!>getX()<!>
}

fun test4(inf: Int.(Int) -> Unit) {
    1 <!COMPLEX_CALL_TO_VARIABLE_AS_FUNCTION, INFIX_MODIFIER_REQUIRED!>inf<!> 2
}