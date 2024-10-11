package cn.martinkay.checkin

fun formatTime(str: String): String {
    var startHour = str.split(":")[0].toInt()
    val startMinute = str.split(":")[1].toInt()
    val str = formatNum(startHour) + ":" + formatNum(startMinute)
    return str
}

fun formatNum(num: Int): String {
    if (num < 10) {
        return "0$num"
    }
    return num.toString()
}