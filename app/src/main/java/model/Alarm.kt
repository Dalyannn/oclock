package model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.room.Entity
import androidx.room.PrimaryKey
import broadcastReceiver.AlarmReceiver
import java.util.*

@Entity(tableName = "alarm_table")
data class Alarm(
    var hour: Int,
    var minute: Int,
    var recurring: Boolean,
    var monday: Boolean,
    var tuesday: Boolean,
    var wednesday: Boolean,
    var thursday: Boolean,
    var friday: Boolean,
    var saturday: Boolean,
    var sunday: Boolean,
    var label: String,
    var isOn: Boolean,
    var timeCreated: Long,
    @PrimaryKey var id: Long
) {

    fun schedule(context: Context) {
        Log.e("ALarm", "schedule")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra("HOUR", hour)
        intent.putExtra("MINUTE", minute)
        intent.putExtra("RECURRING", recurring)
        intent.putExtra("MONDAY", monday)
        intent.putExtra("TUESDAY", tuesday)
        intent.putExtra("WEDNESDAY", wednesday)
        intent.putExtra("THURSDAY", thursday)
        intent.putExtra("FRIDAY", friday)
        intent.putExtra("SATURDAY", saturday)
        intent.putExtra("SUNDAY", sunday)
        intent.putExtra("LABEL", label)
        intent.putExtra("ID", id)
        val pendingIntent = id.let { PendingIntent.getBroadcast(context, it.toInt(), intent, 0) };


        val calendar = Calendar.getInstance()
        calendar.timeInMillis = System.currentTimeMillis();
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)


        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 1)
        }


        if (!recurring) {
            Log.e("Alarm", "!recurring")
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            Log.e("Alarm", "repeat")
            val daily: Long = 24 * 60 * 60 * 1000
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                daily,
                pendingIntent
            )
        }
        Log.e("id schedule", id.toString())
        Log.e("schedule pending intent", pendingIntent.toString())
        Log.e("Schedule Alarm context", context.toString())
        this.isOn = true
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = id.let { PendingIntent.getBroadcast(context, it.toInt(), intent, 0) }
        Log.e("cancel pending intent", pendingIntent.toString())
        Log.e("id cancel", id.toString())
        Log.e("Cancel Alarm context", context.toString())
        alarmManager.cancel(pendingIntent)
        this.isOn = false
    }

    fun getRecurringDays(): String {
        if (!recurring) {
            return ""
        }
        var recurDays = ""
        if (monday) recurDays += "Pzt"
        if (tuesday) recurDays += "Sal "
        if (wednesday) recurDays += "Çrş "
        if (thursday) recurDays += "Prş "
        if (friday) recurDays += "Cum "
        if (saturday) recurDays += "Cmt "
        if (sunday) recurDays += "Pzr "
        return recurDays
    }
}
