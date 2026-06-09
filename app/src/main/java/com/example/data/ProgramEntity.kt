package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "programs")
data class ProgramEntity(
    @PrimaryKey val id: String,
    val title: String,
    val degree: String,
    val department: String,
    val description: String,
    val duration: String,
    val language: String,
    val objectivesJson: String, // JSON Array
    val plosJson: String,       // JSON Array
    val curriculumJson: String, // JSON Array
    val faqJson: String,        // JSON Array
    val admissionRequirements: String,
    val graduationRequirements: String
)
