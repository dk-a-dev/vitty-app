package com.dscvit.vitty.util

import java.text.SimpleDateFormat
import java.util.*

object SemesterUtils {
    
    fun determineSemester(date: Date = Date()): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val month = calendar.get(Calendar.MONTH) + 1
        
        return when (month) {
            12, 1, 2 -> "Winter ${academicYear(date)}"
            in 3..6 -> "Summer ${academicYear(date)}"
            in 7..11 -> "Fall ${academicYear(date)}"
            else -> "Unknown"
        }
    }
    
    private fun academicYear(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        
        return if (month < 3) {
            "${year - 1}-${String.format("%02d", year % 100)}"
        } else {
            "$year-${String.format("%02d", (year + 1) % 100)}"
        }
    }
}
