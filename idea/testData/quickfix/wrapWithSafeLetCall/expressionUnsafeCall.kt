// "Wrap with '?.let { ... }' call" "true"
// WITH_RUNTIME

fun foo(arg: Int?) = arg<caret>.hashCode()