package com.better.medicalAlarm.configuration

import com.better.medicalAlarm.model.AlarmValue
import com.better.medicalAlarm.presenter.RowHolder
import com.better.medicalAlarm.util.Optional

/** Created by Yuriy on 09.08.2017. */
data class EditedAlarm(
    val isNew: Boolean = false,
    val id: Int = -1,
    val value: Optional<AlarmValue> = Optional.absent(),
    val holder: Optional<RowHolder> = Optional.absent()
) {
  fun id() = id
  val isEdited: Boolean = value.isPresent()
}
