package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.Document
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents")
    fun getAllDocuments(): Flow<List<Document>>

    @Query("SELECT * FROM documents WHERE title LIKE :query OR content LIKE :query")
    suspend fun searchDocuments(query: String): List<Document>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documents: List<Document>)

    @Query("DELETE FROM documents")
    suspend fun clearAll()

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocById(id: Int)
}
