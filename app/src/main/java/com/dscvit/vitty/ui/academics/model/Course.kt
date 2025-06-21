package com.dscvit.vitty.ui.academics.model

data class Course(
    val title: String,
    val details: String,
    val semester: String,
    val isStarred: Boolean = false,
)

val sampleCourses =
    listOf(
        Course("Software Engineering - ETH", "C2 + TC2", "Winter 2023-24", true),
        Course("Software Engineering - ETH", "C2 + TC2", "Winter 2023-24"),
        Course("Software Engineering - ETH", "C2 + TC2", "Winter 2023-24"),
        Course("Software Engineering - ETH", "C2 + TC2", "Winter 2023-24"),
    )
