// "Surround with null check" "false"
// ACTION: Convert to expression body
// ACTION: Wrap with '?.let { ... }' call
// ERROR: Reference has a nullable type '(() -> Unit)?', use explicit '?.invoke()' to make a function-like call instead

fun foo(exec: (() -> Unit)?) {
    // Normally should work but for some reason cannot determine KotlinType for 'arg'
    <caret>exec()
}