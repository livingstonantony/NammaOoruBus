package com.ell.nammaooru

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.ell.nammaooru.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "NammaOoru",
        ) {
            App()
        }
    }
}