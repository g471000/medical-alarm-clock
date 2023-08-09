package com.better.medicalAlarm

import com.better.medicalAlarm.model.AlarmStore
import com.better.medicalAlarm.model.AlarmValue
import com.better.medicalAlarm.model.AlarmsRepository
import com.better.medicalAlarm.stores.InMemoryRxDataStoreFactory.Companion.inMemoryRxDataStore

/** Created by Yuriy on 25.06.2017. */
class TestAlarmsRepository : AlarmsRepository {
  private var idCounter: Int = 0
  val createdRecords = mutableListOf<AlarmStore>()

  override fun create(): AlarmStore {
    val storeId = idCounter++
    val inMemory = inMemoryRxDataStore(AlarmValue(id = storeId))
    return object : AlarmStore {
          override var value = inMemory.value
          override val id: Int = storeId

          override fun delete() {
            createdRecords.removeIf { it.value.id == value.id }
          }

          override suspend fun awaitStored() {}
        }
        .also { createdRecords.add(it) }
  }

  override fun query(): List<AlarmStore> {
    return createdRecords
  }

  override var initialized: Boolean = true
  override suspend fun awaitStored() {}
}
