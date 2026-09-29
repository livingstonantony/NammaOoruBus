package com.ell.nammaooru

import androidx.compose.ui.window.ComposeUIViewController
import com.ell.nammaooru.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}