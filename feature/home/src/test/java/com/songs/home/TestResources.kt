package com.songs.home

fun loadJson(fileName: String): String =
    object {}.javaClass.classLoader!!
        .getResourceAsStream(fileName)!!
        .bufferedReader()
        .readText()
