package com.better.medicalAlarm

import com.better.medicalAlarm.configuration.Prefs
import com.better.medicalAlarm.configuration.Store
import com.better.medicalAlarm.logger.Logger
import com.better.medicalAlarm.model.AlarmCore
import com.better.medicalAlarm.model.AlarmSetter
import com.better.medicalAlarm.model.AlarmValue
import com.better.medicalAlarm.model.AlarmsScheduler
import com.better.medicalAlarm.model.Alarmtone
import com.better.medicalAlarm.model.CalendarType
import com.better.medicalAlarm.model.Calendars
import com.better.medicalAlarm.model.DaysOfWeek
import com.better.medicalAlarm.stores.InMemoryRxDataStoreFactory
import com.better.medicalAlarm.util.Optional
import io.mockk.mockk
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.ArrayList
import java.util.Calendar
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class AlarmSchedulerTest {
  private lateinit var stateNotifierMock: AlarmCore.IStateNotifier
  private lateinit var alarmSetterMock: SetterMock
  private lateinit var testScheduler: TestScheduler
  private lateinit var store: Store
  private lateinit var prefs: Prefs
  private lateinit var logger: Logger
  private lateinit var alarmsScheduler: AlarmsScheduler
  private val calendars = Calendars { Calendar.getInstance() }

  @Before
  fun setUp() {
    testScheduler = TestScheduler()
    logger = Logger.create()

    prefs = Prefs.create(Single.just(true), InMemoryRxDataStoreFactory.create())

    store =
        Store(
            alarmsSubject = BehaviorSubject.createDefault(ArrayList()),
            next = BehaviorSubject.createDefault(Optional.absent()),
            sets = PublishSubject.create(),
            events = PublishSubject.create())

    stateNotifierMock = mockk()
    alarmSetterMock = SetterMock()
    alarmsScheduler = AlarmsScheduler(alarmSetterMock, logger, store, prefs, calendars)
  }

  class SetterMock : AlarmSetter {
    var id: Int? = null
    var typeName: String? = null
    var calendar: Calendar? = null
    val inexactAlarms = mutableMapOf<Int, Calendar>()

    override fun setUpRTCAlarm(id: Int, typeName: String, calendar: Calendar) {
      this.id = id
      this.typeName = typeName
      this.calendar = calendar
    }

    override fun removeRTCAlarm() {
      id = null
      typeName = null
      calendar = null
    }

    override fun fireNow(id: Int, typeName: String) {}

    override fun removeInexactAlarm(id: Int) {
      inexactAlarms.remove(id)
    }

    override fun setInexactAlarm(id: Int, calendar: Calendar) {
      inexactAlarms[id] = calendar
    }
  }

  @Test
  fun `Only closest alarm is set by the scheduler`() {
    alarmsScheduler.start()
    alarmsScheduler.setAlarm(
        1,
        CalendarType.NORMAL,
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) },
        createTestAlarmValue(1))

    assertThat(alarmSetterMock.id).isEqualTo(1)
  }

  @Test
  fun `Only closest alarm is set by the scheduler if more alarms are present`() {
    alarmsScheduler.start()
    alarmsScheduler.setAlarm(
        1,
        CalendarType.NORMAL,
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) },
        createTestAlarmValue(1))

    alarmsScheduler.setAlarm(
        2,
        CalendarType.NORMAL,
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 2) },
        createTestAlarmValue(2))

    assertThat(alarmSetterMock.id).isEqualTo(1)
  }

  @Test
  fun `Only closest alarm is set by the scheduler if more alarms are present scheduled before current`() {
    alarmsScheduler.start()
    alarmsScheduler.setAlarm(
        2,
        CalendarType.NORMAL,
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 2) },
        createTestAlarmValue(2))

    alarmsScheduler.setAlarm(
        1,
        CalendarType.NORMAL,
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) },
        createTestAlarmValue(1))

    assertThat(alarmSetterMock.id).isEqualTo(1)
  }

  @Test
  fun `Scheduler must wait until it has been started to set alarms`() {
    alarmsScheduler.setAlarm(
        2,
        CalendarType.NORMAL,
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 2) },
        createTestAlarmValue(2))

    alarmsScheduler.setAlarm(
        1,
        CalendarType.NORMAL,
        Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 1) },
        createTestAlarmValue(1))

    assertThat(alarmSetterMock.id).isNull()
    alarmsScheduler.start()
    assertThat(alarmSetterMock.id).isEqualTo(1)
  }

  private fun createTestAlarmValue(id: Int, label: String = id.toString()) =
      AlarmValue(
          id = id,
          alarmtone = Alarmtone.Default,
          daysOfWeek = DaysOfWeek(0),
          hour = 12,
          isEnabled = true,
          isPrealarm = false,
          isVibrate = false,
          label = label,
          minutes = 1,
          nextTime = calendars.now(),
          state = "")
}
