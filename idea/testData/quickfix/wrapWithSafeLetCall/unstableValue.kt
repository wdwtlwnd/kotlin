// "Wrap with '?.let { ... }' call" "true"
// WITH_RUNTIME

class My(var x: Int?) {

    fun foo() {
        x<caret>.hashCode()
    }
}