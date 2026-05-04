plugins {
    alias(libs.plugins.songs.android.library)
}

dependencies {
    implementation(projects.feature.home.domain)
    implementation(projects.feature.player.domain)
}
