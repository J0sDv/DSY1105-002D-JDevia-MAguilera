package com.example.modoguardian.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class GuardianDao {

    @Query("SELECT * FROM camaras ORDER BY nombre")
    abstract fun camaras(): Flow<List<CamaraEntity>>

    @Query("SELECT * FROM eventos ORDER BY fechaMillis DESC")
    abstract fun eventos(): Flow<List<EventoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarCamaras(lista: List<CamaraEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertarEventos(lista: List<EventoEntity>)

    @Query("DELETE FROM camaras")
    abstract suspend fun borrarCamaras()

    @Query("DELETE FROM eventos")
    abstract suspend fun borrarEventos()

    @Transaction
    open suspend fun reemplazarCamaras(lista: List<CamaraEntity>) {
        borrarCamaras()
        insertarCamaras(lista)
    }

    @Transaction
    open suspend fun reemplazarEventos(lista: List<EventoEntity>) {
        borrarEventos()
        insertarEventos(lista)
    }
}