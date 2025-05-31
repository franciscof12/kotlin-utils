package com.ecra.kotlinutils.infrastructure

object EnvReader {
    fun getValue(key: String) =
        System.getenv(key) ?: throw IllegalArgumentException("Environment variable '$key' not found")
}