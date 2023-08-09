package com.better.medicalAlarm.configuration

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.os.Vibrator
import android.telephony.TelephonyManager
import android.text.format.DateFormat
import com.better.medicalAlarm.alert.BackgroundNotifications
import com.better.medicalAlarm.background.AlertServicePusher
import com.better.medicalAlarm.background.KlaxonPlugin
import com.better.medicalAlarm.background.PlayerWrapper
import com.better.medicalAlarm.bugreports.BugReporter
import com.better.medicalAlarm.interfaces.IAlarmsManager
import com.better.medicalAlarm.logger.Logger
import com.better.medicalAlarm.logger.LoggerFactory
import com.better.medicalAlarm.logger.loggerModule
import com.better.medicalAlarm.model.AlarmCore
import com.better.medicalAlarm.model.AlarmSetter
import com.better.medicalAlarm.model.AlarmStateNotifier
import com.better.medicalAlarm.model.Alarms
import com.better.medicalAlarm.model.AlarmsRepository
import com.better.medicalAlarm.model.AlarmsScheduler
import com.better.medicalAlarm.model.Calendars
import com.better.medicalAlarm.model.IAlarmsScheduler
import com.better.medicalAlarm.persistance.DataStoreAlarmsRepository
import com.better.medicalAlarm.persistance.DatabaseQuery
import com.better.medicalAlarm.persistance.DatastoreMigration
import com.better.medicalAlarm.persistance.SQLiteDatabaseQuery
import com.better.medicalAlarm.presenter.AlarmsListActivity
import com.better.medicalAlarm.presenter.DynamicThemeHandler
import com.better.medicalAlarm.presenter.ScheduledReceiver
import com.better.medicalAlarm.presenter.ToastPresenter
import com.better.medicalAlarm.stores.SharedRxDataStoreFactory
import com.better.medicalAlarm.util.Optional
import com.better.medicalAlarm.wakelock.WakeLockManager
import com.better.medicalAlarm.wakelock.Wakelocks
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.io.File
import java.util.*
import kotlinx.coroutines.Dispatchers
import org.koin.core.Koin
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.binds
import org.koin.dsl.module

fun Scope.logger(tag: String): Logger {
  return get<LoggerFactory>().createLogger(tag)
}

fun Koin.logger(tag: String): Logger {
  return get<LoggerFactory>().createLogger(tag)
}

fun startKoin(context: Context): Koin {
  // The following line triggers the initialization of ACRA

  val module = module {
    single<DynamicThemeHandler> { DynamicThemeHandler(get()) }
    single<BugReporter> { BugReporter(logger("BugReporter"), context) }
    factory<Context> { context }
    factory(named("dateFormatOverride")) { "none" }
    factory<Single<Boolean>>(named("dateFormat")) {
      Single.fromCallable {
        get<String>(named("dateFormatOverride")).let { if (it == "none") null else it.toBoolean() }
            ?: DateFormat.is24HourFormat(context)
      }
    }

    single<Prefs> {
      val factory = SharedRxDataStoreFactory.create(get(), logger("preferences"))
      Prefs.create(get(named("dateFormat")), factory)
    }

    single<Store> {
      Store(
          alarmsSubject = BehaviorSubject.createDefault(ArrayList()),
          next = BehaviorSubject.createDefault<Optional<Store.Next>>(Optional.absent()),
          sets = PublishSubject.create(),
          events = PublishSubject.create())
    }

    factory { get<Context>().getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    single<AlarmSetter> { AlarmSetter.AlarmSetterImpl(logger("AlarmSetter"), get(), get()) }
    factory { Calendars { Calendar.getInstance() } }
    single<AlarmsScheduler> {
      AlarmsScheduler(get(), logger("AlarmsScheduler"), get(), get(), get())
    }
    factory<IAlarmsScheduler> { get<AlarmsScheduler>() }
    single<AlarmCore.IStateNotifier> { AlarmStateNotifier(get()) }
    single<AlarmsRepository> {
      DataStoreAlarmsRepository(
          datastoreDir = get(named("datastore")),
          logger = logger("DataStoreAlarmsRepository"),
          ioDispatcher = Dispatchers.IO,
      )
    }
    single(named("datastore")) { File(get<Context>().applicationContext.filesDir, "datastore") }
    factory { get<Context>().contentResolver }
    single<DatabaseQuery> { SQLiteDatabaseQuery(get()) }
    single { Alarms(get(), get(), get(), get(), get(), get(), logger("Alarms"), get()) } binds
        arrayOf(IAlarmsManager::class, DatastoreMigration::class)
    single { ScheduledReceiver(get(), get(), get(), get()) }
    single { ToastPresenter(get(), get()) }
    single { AlertServicePusher(get(), get(), get(), logger("AlertServicePusher")) }
    single { BackgroundNotifications(get(), get(), get(), get(), get()) }
    factory<Wakelocks> { get<WakeLockManager>() }
    factory<Scheduler> { AndroidSchedulers.mainThread() }
    single { WakeLockManager(logger("WakeLockManager"), get()) }
    factory { get<Context>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    factory { get<Context>().getSystemService(Context.POWER_SERVICE) as PowerManager }
    factory { get<Context>().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager }
    factory { get<Context>().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    factory { get<Context>().getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    factory { get<Context>().resources }

    factory(named("volumePreferenceDemo")) {
      KlaxonPlugin(
          log = logger("VolumePreference"),
          playerFactory = { PlayerWrapper(get(), get(), logger("VolumePreference")) },
          prealarmVolume = get<Prefs>().preAlarmVolume.observe(),
          fadeInTimeInMillis = Observable.just(100),
          inCall = Observable.just(false),
          scheduler = get())
    }
  }

  return startKoin {
        modules(module)
        modules(AlarmsListActivity.uiStoreModule)
        modules(loggerModule())
      }
      .koin
}

fun overrideIs24hoursFormatOverride(is24hours: Boolean) {
  loadKoinModules(
      module =
          module(override = true) { factory(named("dateFormatOverride")) { is24hours.toString() } })
}
