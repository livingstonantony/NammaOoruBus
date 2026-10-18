package com.ell.nammaooru

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ell.nammaooru.di.initKoin
import com.ell.nammaooru.domain.model.AppMode

fun main() {
    initKoin()
    val mode = if (System.getProperty("nammaooru.mode") == "admin") AppMode.Admin else AppMode.Guest
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = if (mode == AppMode.Admin) "NammaOoru Admin" else "NammaOoru",
        ) {
            App(mode)
        }
    }
}