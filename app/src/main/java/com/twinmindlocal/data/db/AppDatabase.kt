package com.twinmindlocal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.twinmindlocal.data.model.Session
import com.twinmindlocal.data.model.Transcript

@Database(entities = [Session::class, Transcript::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "twinmind.db")
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
