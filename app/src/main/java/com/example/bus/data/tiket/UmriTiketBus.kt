package com.example.bus.data.tiket

import androidx.room.*

@Dao
interface UmriTiketBus {
    @Query("SELECT * FROM tiket WHERE nama_penumpang LIKE :namaPenumpang")
    suspend fun searchTiket(namaPenumpang: String) : List<UmriTiket>

    @Insert
    suspend fun addTiket(umriTiket: UmriTiket)

    @Update
    suspend fun updateTiket(umriTiket: UmriTiket)

    @Delete
    suspend fun deleteTiket(umriTiket: UmriTiket)

    @Query("SELECT * FROM tiket")
    suspend fun getAllUmriTiket(): List<UmriTiket>
}