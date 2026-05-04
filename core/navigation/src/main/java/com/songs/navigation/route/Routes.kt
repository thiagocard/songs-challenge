package com.songs.navigation.route

import android.os.Parcel
import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SplashRoute : NavKey

@Serializable
data object HomeRoute : NavKey

@Serializable
data class AlbumRoute(val albumId: String) : NavKey {
    /**
     * Navigation token to identify the [AlbumRoute] in the back stack.
     */
    data object Key : Parcelable {
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) = Unit

        @JvmField
        val CREATOR: Parcelable.Creator<Key> =
            object : Parcelable.Creator<Key> {
                override fun createFromParcel(source: Parcel): Key = Key
                override fun newArray(size: Int): Array<Key?> = arrayOfNulls(size)
            }
    }
}

@Serializable
data class PlayerRoute(
    val trackIds: List<Long>,
    val currentTrackId: Long,
    val shouldPlay: Boolean = false,
) : NavKey {
    /**
     * Navigation token to identify the [PlayerRoute] in the back stack.
     */
    data object Key : Parcelable {
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) = Unit

        @JvmField
        val CREATOR: Parcelable.Creator<Key> =
            object : Parcelable.Creator<Key> {
                override fun createFromParcel(source: Parcel): Key = Key
                override fun newArray(size: Int): Array<Key?> = arrayOfNulls(size)
            }
    }
}
