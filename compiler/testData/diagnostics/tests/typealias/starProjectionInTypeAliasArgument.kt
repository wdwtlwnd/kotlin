typealias L<T> = List<T>
typealias M<K, V> = Map<K, V>
typealias A<T> = Array<T>
typealias AA<T> = A<A<T>>

class NumNum<N : Number, M : Number>
class NumAny<N : Number, M : Any>

typealias NN<X, Y> = NumNum<X, Y>
typealias NA<X, Y> = NumAny<X, Y>

fun testL(x: L<*>) = x

fun testM1(x: M<*, Int>) = x
fun testM2(x: M<Int, *>) = x
fun testM3(x: M<*, *>) = x

fun testA(x: A<*>) = x

fun testAA(x: AA<*>) = x

fun testNN1(x: NN<*, Int>) = x
fun testNN2(x: NN<Int, *>) = x
fun testNN3(x: NN<*, *>) = x

fun testNA1(x: NN<*, Int>) = x
fun testNA2(x: NA<Int, *>) = x
fun testNA3(x: NA<*, *>) = x
