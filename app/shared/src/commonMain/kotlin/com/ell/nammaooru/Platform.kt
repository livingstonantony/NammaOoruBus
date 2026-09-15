package com.ell.nammaooru

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform