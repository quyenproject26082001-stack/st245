package com.cat.catpiano.music.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cat.catpiano.music.data.local.dao.UserDao
import com.cat.catpiano.music.data.local.entity.User

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}