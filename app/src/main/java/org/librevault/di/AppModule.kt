package org.librevault.di

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.librevault.data.repository.VaultRepositoryImpl
import org.librevault.data.use_case.AddItems
import org.librevault.data.use_case.GetAllThumbnails
import org.librevault.data.use_case.GetInfoById
import org.librevault.data.use_case.gallery.GalleryUseCases
import org.librevault.domain.repository.VaultRepository
import org.librevault.presentation.viewmodels.gallery.GalleryViewModel
import org.librevault.data.repository.MediaThumbnailer

val appModule = module {
    single<MediaThumbnailer> { MediaThumbnailer() }

    single<VaultRepository> { VaultRepositoryImpl(get()) }

    single<AddItems> { AddItems(get()) }
    single<GetAllThumbnails> { GetAllThumbnails(get()) }
    single<GetInfoById> { GetInfoById(get()) }
    single<GalleryUseCases> { GalleryUseCases(get(), get(), get()) }

    viewModel { GalleryViewModel(get()) }
}