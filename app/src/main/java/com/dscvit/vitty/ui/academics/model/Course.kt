package com.dscvit.vitty.ui.academics.model

data class Course(
    val title: String,
    val slot: String,
    val code: String,
    val semester: String,
    val isStarred: Boolean = false,
) {
    val details: String get() = "$slot | $semester"
}

val sampleCourses =
    listOf(
        Course("Software Engineering", "C1 + TC1", "CSE3011", "Winter 2023-24", true),
        Course("Database Management Systems", "A1 + TA1 + L39", "CSE3002", "Winter 2023-24"),
        Course("Computer Networks", "B2 + TB2 + L40", "CSE3003", "Winter 2023-24"),
        Course("Operating Systems", "D1 + TD1", "CSE3004", "Winter 2023-24"),
    )
