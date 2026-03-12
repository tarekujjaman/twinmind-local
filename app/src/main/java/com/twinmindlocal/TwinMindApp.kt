package com.twinmindlocal

import android.app.Application
import com.twinmindlocal.data.db.AppDatabase
import com.twinmindlocal.data.repository.SessionRepository

class TwinMindApp : Application() {
    val repository by lazy {
        SessionRepository(AppDatabase.getInstance(this).sessionDao())
    }
}
