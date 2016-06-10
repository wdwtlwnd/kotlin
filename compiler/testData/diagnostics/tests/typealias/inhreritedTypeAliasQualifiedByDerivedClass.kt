open class Base {
    typealias Nested = String
}

class Derived : Base()

fun test(x: Derived.<!UNRESOLVED_REFERENCE!>Nested<!>) = <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>x<!>
