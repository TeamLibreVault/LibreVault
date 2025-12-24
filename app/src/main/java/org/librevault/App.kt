package org.librevault

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.librevault.common.vault_consts.VaultDirs
import org.librevault.di.appModule

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        VaultDirs.initVaultDirs()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }

}