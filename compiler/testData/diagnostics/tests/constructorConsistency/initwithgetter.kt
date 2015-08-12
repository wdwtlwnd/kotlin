class My {
    val x: Int
        get() = field + z

    val y: Int
        get() = field - z

    val w: Int

    init {
        // Safe, val never has a setter
        x = 0
        this.y = 0
        // Unsafe
        w = <!DEBUG_INFO_LEAKING_THIS!>this<!>.x + <!DEBUG_INFO_LEAKING_THIS!>y<!>
    }

    val z = 1
}
