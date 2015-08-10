// FILE: f.kt
class A() {
  fun foo() : Unit {
    <!UNUSED_EXPRESSION!>this@A<!>
    this<!UNRESOLVED_REFERENCE!>@a<!>
    <!UNUSED_EXPRESSION!>this<!>
  }

  val x = <!DEBUG_INFO_LEAKING_THIS!>this@A<!>.foo()
  val y = <!DEBUG_INFO_LEAKING_THIS!>this<!>.foo()
  val z = <!DEBUG_INFO_LEAKING_THIS!>foo()<!>
}