package com.example.model

data class ProgramCourse(
    val code: String,
    val name: String,
    val hours: Int
)

data class ProgramTerm(
    val termName: String,
    val courses: List<ProgramCourse>
)

data class Program(
    val id: String,
    val title: String,
    val degree: String,
    val department: String,
    val description: String,
    val duration: String,
    val language: String,
    val objectives: List<String>,
    val plos: List<String>,
    val curriculum: List<ProgramTerm>,
    val faq: List<FAQItem>,
    val admissionRequirements: String,
    val graduationRequirements: String
)

data class FAQItem(
    val q: String,
    val a: String
)
