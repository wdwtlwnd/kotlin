// "Surround with null check" "false"
// ACTION: Add non-null asserted (!!) call
// ACTION: Introduce local variable
// ACTION: Wrap with '?.let { ... }' call
// ERROR: Only safe (?.) or non-null asserted (!!.) calls are allowed on a nullable receiver of type Int?

operator fun Int.invoke() = this

fun foo(arg: Int?) {
    // Normally should work but for some reason cannot determine KotlinType for 'arg'
    <caret>arg()
}