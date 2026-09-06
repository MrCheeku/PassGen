package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.security.VaultSecurityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("PassGen", appName)
  }

  @Test
  fun `vault security manager setup and verify pin`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val sec = VaultSecurityManager(context)
    val setupSuccess = sec.setupMasterLock("1234")
    assertTrue(setupSuccess)
    assertTrue(sec.isMasterLockEnabled())
    assertTrue(sec.verifyMasterLock("1234"))
  }
}

