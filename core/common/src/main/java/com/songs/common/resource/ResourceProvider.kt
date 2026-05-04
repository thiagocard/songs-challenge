package com.songs.common.resource

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ResourceProvider {
    fun getString(@StringRes resId: Int): String

    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String
}

class ResourceProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ResourceProvider {
    override fun getString(resId: Int): String {
        return context.getString(resId)
    }

    override fun getString(resId: Int, vararg formatArgs: Any?): String {
        return context.getString(resId, *formatArgs)
    }
}
