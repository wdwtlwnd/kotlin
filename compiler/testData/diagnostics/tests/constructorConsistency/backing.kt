class My {
    var x = 1
        set(value) {
            field = value
        }

    var y: Int = 1
        set(value) {
            field = value + w
        }

    var z: Int = 2
        set(value) {
            field = value + w
        }

    init {
        // Writing properties using setters is dangerous
        <!DEBUG_INFO_LEAKING_THIS!>y<!> = 4
        <!DEBUG_INFO_LEAKING_THIS!>this<!>.z = 5
    }

    val w = 6
}