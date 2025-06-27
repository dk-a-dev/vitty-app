package com.dscvit.vitty.ui.academics.models

data class Course(
    val title: String,
    val slot: String,
    val code: String,
    val semester: String,
    val isStarred: Boolean = false,
) {
    val details: String get() = "$slot | $semester"
}
