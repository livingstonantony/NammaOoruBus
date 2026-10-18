package com.ell.nammaooru.di

import com.ell.nammaooru.data.repository.AuthRepository
import com.ell.nammaooru.data.repository.BusRepositoryImpl
import com.ell.nammaooru.feature.auth.AuthViewModel
import com.ell.nammaooru.domain.repository.BusRepository
import com.ell.nammaooru.feature.bus.BusViewModel
import com.ell.nammaooru.network.createHttpClient
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val sharedModule: Module = module {
    single { createHttpClient() }
    single { AuthRepository(get()) }
    single<BusRepository> { BusRepositoryImpl(get(), get()) }
    factory { BusViewModel(get()) }
    factory { AuthViewModel(get()) }
}

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    try {
        startKoin {
            appDeclaration()
            modules(sharedModule)
        }
    } catch (_: Exception) {
        // Ignored if Koin is already started
    }
}

