// !CHECK_TYPE
// !DIAGNOSTICS: -UNUSED_PARAMETER

operator fun Int.plus(s: String) = s

fun test(plus: Int.(String) -> Unit) {
    val a = 1 + ""
    a checkType { _<String>() }
}

class Foo
operator fun Foo.set(i: Int, j: Int) = ""

fun test2(foo: Foo, set: Foo.(Int, Int) -> Int) {
    foo[1] = 2
}

infix fun Int.inf(i: Int) = i

fun test3(inf: Int.(Int) -> Unit) {
    val b = 1 inf 2
    b checkType { _<Int>() }
}