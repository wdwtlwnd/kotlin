// "Surround with null check" "false"
// ACTION: Convert to expression body
// ERROR: Reference has a nullable type '(() -> Unit)?', use explicit '?.invoke()' to make a function-like call instead

fun foo(exec: (() -> Unit)?) {
    // Normally should work but for some reason cannot determine KotlinType for 'arg'
    <caret>exec()
}