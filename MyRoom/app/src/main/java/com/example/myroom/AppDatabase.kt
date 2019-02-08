package com.example.myroom

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    companion object {

        private const val dbName = "userDatabase"
        private var instance: AppDatabase? = null

        @Synchronized
        fun shared(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.dbName)
                    .build()
            }
            return instance!!
        }
    }

    abstract fun getDao(): UserEntityDao
}