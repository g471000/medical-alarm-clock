package com.better.medicalAlarm.persistance

interface DatastoreMigration {
  fun drop()
  fun insertDefaultAlarms()
  fun migrateDatabase()
}
