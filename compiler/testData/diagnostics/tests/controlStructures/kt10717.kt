fun <!IMPLICIT_NOTHING_RETURN_TYPE!>test1<!>() = run {
    return <!TYPE_MISMATCH(String; Nothing)!>"OK"<!>
}

fun <!IMPLICIT_NOTHING_RETURN_TYPE!>test2<!>() = run {
    fun local(): String {
        return ""
    }
    return <!TYPE_MISMATCH(String; Nothing)!>""<!>
}

inline fun <T, R> Iterable<T>.map(<!UNUSED_PARAMETER!>transform<!>: (T) -> R): List<R> = null!!
fun test3(a: List<String>) = a.map {
    if (it.length == 3) return <!TYPE_MISMATCH(Nothing?; List<Int>)!>null<!>
    if (it.length == 4) return <!TYPE_MISMATCH(String; List<Int>)!>""<!>
    if (it.length == 4) return <!TYPE_MISMATCH(Int; List<Int>)!>5<!>
    1
}