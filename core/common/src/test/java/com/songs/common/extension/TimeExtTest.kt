package com.songs.common.extension

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class TimeExtTest {

    @Test
    fun `zero milliseconds formats to 00 minutes 00 seconds`() {
        assertThat(0L.formatTime()).isEqualTo("00:00")
    }

    @Test
    fun `999 milliseconds rounds down to 00 seconds`() {
        assertThat(999L.formatTime()).isEqualTo("00:00")
    }

    @Test
    fun `1000 milliseconds formats to 00 minutes 01 second`() {
        assertThat(1_000L.formatTime()).isEqualTo("00:01")
    }

    @Test
    fun `59 seconds formats correctly`() {
        assertThat(59_000L.formatTime()).isEqualTo("00:59")
    }

    @Test
    fun `exactly 1 minute formats to 01 minutes 00 seconds`() {
        assertThat(60_000L.formatTime()).isEqualTo("01:00")
    }

    @Test
    fun `90 seconds formats to 01 minutes 30 seconds`() {
        assertThat(90_000L.formatTime()).isEqualTo("01:30")
    }

    @Test
    fun `typical song duration of 3 minutes 45 seconds`() {
        assertThat(225_000L.formatTime()).isEqualTo("03:45")
    }

    @Test
    fun `10 minutes formats with leading zero on seconds`() {
        assertThat(600_000L.formatTime()).isEqualTo("10:00")
    }

    @Test
    fun `durations over 60 minutes are not capped`() {
        // 90 minutes
        assertThat(5_400_000L.formatTime()).isEqualTo("90:00")
    }

    @Test
    fun `seconds portion always zero-pads to two digits`() {
        // 1 minute and 5 seconds
        assertThat(65_000L.formatTime()).isEqualTo("01:05")
    }
}
