package com.singularitycoder.rememberme

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {

    @Singleton
    @Provides
    fun injectContactRoomDatabase(@ApplicationContext context: Context): ContactDatabase {
        return Room.databaseBuilder(context, ContactDatabase::class.java, DB_CONTACT).build()
    }

    @Singleton
    @Provides
    fun injectContactDao(db: ContactDatabase): ContactDao = db.contactDao()
}
