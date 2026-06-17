fun main() {
    val all = listOf(1, 2, 3)
    val filtered = false
    val tab = "STORE"
    
    val result = run {
        var list = all
        if (filtered) list = list.filter { it == 1 }
        
        if (tab == "STORE") {
            list.filter { it > 1 }
        } else {
            list.filter { it < 2 }
        }
    }
    println("Result: $result")
}
