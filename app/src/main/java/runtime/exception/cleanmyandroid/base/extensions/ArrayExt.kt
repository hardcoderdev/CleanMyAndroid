package runtime.exception.cleanmyandroid.base.extensions

fun <T> Array<T>.toPair(): Pair<T, T> {
    val firstElement = get(0)
    val secondElement = get(1)
    return Pair(firstElement, secondElement)
}