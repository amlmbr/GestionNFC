package ma.ensa.projet.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ma.ensa.projet.Dao.NFCTagDao
import ma.ensa.projet.beans.NFCTag

@Database(entities = [NFCTag::class], version = 1, exportSchema = false)
abstract class NFCTagDatabase : RoomDatabase() {
    abstract fun nfcTagDao(): NFCTagDao

    companion object {
        @Volatile
        private var INSTANCE: NFCTagDatabase? = null

        fun getDatabase(context: Context): NFCTagDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NFCTagDatabase::class.java,
                    "nfc_tag_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}