package com.better.medicalAlarm.wakelock

interface Wakelocks {
  fun acquireServiceLock()

  fun releaseServiceLock()
}
