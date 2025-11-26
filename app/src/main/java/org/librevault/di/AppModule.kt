package org.librevault.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.librevault.data.repository.preferences.SecurityPreferences
import org.librevault.data.repository.vault.VaultRepositoryImpl
import org.librevault.data.repository.vault.utils.MediaThumbnailer
import org.librevault.domain.repository.vault.VaultRepository
import org.librevault.domain.use_case.preferences.security.GetAutoLockEnabled
import org.librevault.domain.use_case.preferences.security.GetAutoLockTimeout
import org.librevault.domain.use_case.vault.AddItems
import org.librevault.domain.use_case.vault.GetAllThumbnails
import org.librevault.domain.use_case.vault.GetInfoById
import org.librevault.domain.use_case_bundle.GalleryUseCases
import org.librevault.domain.use_case_bundle.MainUseCases
import org.librevault.presentation.viewmodels.GalleryViewModel
import org.librevault.presentation.viewmodels.MainViewModel

// Preferences
val securityModule = module {
    single { SecurityPreferences(get()) }
    single { GetAutoLockEnabled(get()) }
    single { GetAutoLockTimeout(get()) }
    single { MainUseCases(get(), get()) }
}

// Vault / Repository
val vaultModule = module {
    single { MediaThumbnailer() }
    single<VaultRepository> { VaultRepositoryImpl(get()) }

    single { AddItems(get()) }
    single { GetAllThumbnails(get()) }
    single { GetInfoById(get()) }

    single { GalleryUseCases(get(), get(), get()) }
}

// ViewModels
val viewModelModule = module {
    viewModel { MainViewModel(get()) }
    viewModel { GalleryViewModel(get()) }
}

// All together
val appModule = listOf(
    securityModule,
    vaultModule,
    viewModelModule
)