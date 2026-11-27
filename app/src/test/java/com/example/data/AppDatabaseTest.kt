package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseTest {

    @Test
    fun `getInstance returns the same application database instance`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val first = AppDatabase.getInstance(context)
        val second = AppDatabase.getInstance(context)

        assertNotNull(first)
        assertSame(first, second)
        assertNotNull(first.vaultDao())
    }
}
