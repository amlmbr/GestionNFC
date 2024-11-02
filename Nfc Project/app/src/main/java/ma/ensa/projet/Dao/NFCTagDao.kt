package ma.ensa.projet.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ma.ensa.projet.beans.NFCDataType
import ma.ensa.projet.beans.NFCTag

@Dao
interface NFCTagDao {
    @Insert
    suspend fun insert(tag: NFCTag)

    @Query("SELECT * FROM nfc_tags ORDER BY timestamp DESC")
    fun getAllTags(): Flow<List<NFCTag>>

    @Query("SELECT * FROM nfc_tags WHERE type = :type ORDER BY timestamp DESC")
    fun getTagsByType(type: NFCDataType): Flow<List<NFCTag>>
    @Delete
    suspend fun delete(tag: NFCTag)
}
