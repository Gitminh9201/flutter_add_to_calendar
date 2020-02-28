package com.github.jonyas.add_to_calendar

import android.app.Activity
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar
import java.util.*

class AddToCalendarPlugin(private val activity: Activity) : MethodCallHandler {
  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "add_to_calendar")
      channel.setMethodCallHandler(AddToCalendarPlugin(registrar.activity()))
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "addToCalendar") {
      // compulsory params
      val title = call.argument<String>("title")!!
      val startTime = Date(call.argument<Long>("startTime")!!)

      // optional params
      val location = call.argument<String>("location")
      val description = call.argument<String>("description")
      val endTime = call.argument<Long>("endTime")?.let { Date(it) }
      val isAllDay = call.argument<Boolean>("isAllDay")

      val intent = Intent(Intent.ACTION_EDIT).apply {
        data = CalendarContract.Events.CONTENT_URI
        putExtra(CalendarContract.Events.TITLE, title)
        putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.time)
        if (location?.isNotEmpty() == true) putExtra(CalendarContract.Events.EVENT_LOCATION, location)
        if (description?.isNotEmpty() == true) putExtra(CalendarContract.Events.DESCRIPTION, description)
        if (endTime != null) putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.time)
        if (isAllDay != null) putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, isAllDay)
      }

      val calID: Long = 3
      val startMillis: Long = Calendar.getInstance().run {
        set(2020, 3, 1, 7, 30)
        timeInMillis
      }
      val endMillis: Long = Calendar.getInstance().run {
        set(2020, 3, 1, 8, 45)
        timeInMillis
      }

      val values = ContentValues().apply {
        put(CalendarContract.Events.DTSTART, startMillis)
        put(CalendarContract.Events.DTEND, endMillis)
        put(CalendarContract.Events.TITLE, "Jazzercise")
        put(CalendarContract.Events.DESCRIPTION, "Group workout")
        put(CalendarContract.Events.CALENDAR_ID, calID)
        put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles")
      }
      val uri: Uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)

// get the event ID that is the last element in the Uri
      val eventID: Long = uri.lastPathSegment.toLong()

      activity.startActivity(intent)
      result.success(true)

    } else {
      result.notImplemented()
    }
  }
}
