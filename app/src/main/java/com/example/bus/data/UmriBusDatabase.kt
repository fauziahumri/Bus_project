package com.example.bus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.bus.data.tiket.UmriTiket
import com.example.bus.data.tiket.UmriTiketBus

@Database(entities = [UmriTiket::class], version = 1)
abstract class UmriBusDatabase : RoomDatabase() {

    abstract fun getUmriTiketBus(): UmriTiketBus

    companion object {
        @Volatile
        private var instance: UmriBusDatabase? = null
        private val Lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(Lock) {
            instance ?: buildDatabase(context).also{
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            UmriBusDatabase::class.java,
            "bus-db"
        ).build()
    }
}