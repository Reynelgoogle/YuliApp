package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "animals")
data class AnimalEntity(
    @PrimaryKey val id: String,
    val name: String,
    val emoji: String,
    val colorHex: String,
    val synthType: String,
    val customSoundPath: String? = null,
    val isCustomSoundEnabled: Boolean = false
)

@Entity(tableName = "family")
data class FamilyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val relation: String,
    val emoji: String,
    val colorHex: String,
    val customPhotoPath: String? = null,
    val customSoundPath: String? = null,
    val isCustomSoundEnabled: Boolean = false
)

// --- DAO Interface ---

@Dao
interface YuliAppDao {
    @Query("SELECT * FROM animals")
    fun getAllAnimals(): Flow<List<AnimalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimals(animals: List<AnimalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimal(animal: AnimalEntity)

    @Update
    suspend fun updateAnimal(animal: AnimalEntity)

    @Query("DELETE FROM animals WHERE id = :id")
    suspend fun deleteAnimal(id: String)

    @Query("SELECT * FROM family ORDER BY id ASC")
    fun getAllFamily(): Flow<List<FamilyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMembers(members: List<FamilyEntity>)

    @Update
    suspend fun updateFamilyMember(member: FamilyEntity)

    @Query("DELETE FROM family WHERE id = :id")
    suspend fun deleteFamilyMember(id: Int)
}

// --- AppDatabase ---

@Database(entities = [AnimalEntity::class, FamilyEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun yuliAppDao(): YuliAppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "yuliapp_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- Repository (Abstracts Data Access) ---

class YuliAppRepository(private val dao: YuliAppDao) {
    val allAnimals: Flow<List<AnimalEntity>> = dao.getAllAnimals()
    val allFamily: Flow<List<FamilyEntity>> = dao.getAllFamily()

    suspend fun insertAnimal(animal: AnimalEntity) {
        dao.insertAnimal(animal)
    }

    suspend fun updateAnimal(animal: AnimalEntity) {
        dao.updateAnimal(animal)
    }

    suspend fun deleteAnimal(id: String) {
        dao.deleteAnimal(id)
    }

    suspend fun insertFamilyMember(member: FamilyEntity) {
        dao.insertFamilyMember(member)
    }

    suspend fun updateFamilyMember(member: FamilyEntity) {
        dao.updateFamilyMember(member)
    }

    suspend fun deleteFamilyMember(id: Int) {
        dao.deleteFamilyMember(id)
    }

    suspend fun prepopulateDatabaseIfEmpty() {
        // Will be called during app initialization
    }
}
