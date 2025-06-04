package com.taloscore.guessapp.di

import android.content.Context
import com.taloscore.guessapp.data.repository.AuthRepository
import com.taloscore.guessapp.data.repository.CategoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideAuthRepository(@ApplicationContext context: Context): AuthRepository {
        return AuthRepository(context)
    }

    @Provides
    fun provideCategoryRepository(@ApplicationContext context: Context): CategoryRepository{
        return CategoryRepository(context)
    }
}