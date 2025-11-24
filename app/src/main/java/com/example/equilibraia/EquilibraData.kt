package com.example.equilibraia

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow


@Entity(tableName = "tarefas")
data class Tarefa(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descricao: String,
    val dataPrazo: Long,
    val concluida: Boolean = false,
    val nivelEnergia: Int,
    val categoriaBemEstar: String,
    val duracaoMinutos: Int //
)

// 2. O DAO
@Dao
interface TarefaDao {
    @Query("SELECT * FROM tarefas ORDER BY concluida ASC, nivelEnergia DESC")
    fun getTarefasOrdenadasPorCarga(): Flow<List<Tarefa>>

    @Insert
    suspend fun inserirTarefa(tarefa: Tarefa)

    @Update
    suspend fun atualizarTarefa(tarefa: Tarefa)

    @Delete
    suspend fun deletarTarefa(tarefa: Tarefa)
}

// 3. O BANCO
@Database(entities = [Tarefa::class], version = 2, exportSchema = false) //
abstract class AppDatabase : RoomDatabase() {
    abstract fun tarefaDao(): TarefaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "equilibra_database"
                )
                    .fallbackToDestructiveMigration() //
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}