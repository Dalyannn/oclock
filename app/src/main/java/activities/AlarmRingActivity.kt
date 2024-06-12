package activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alarmapp.databinding.ActivityAlarmRingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import model.Alarm
import service.AlarmService
import java.util.*

@AndroidEntryPoint
class AlarmRingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmRingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityAlarmRingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent(applicationContext, AlarmService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            initTimeAndLabel()
        }


        binding.btnStop.setOnClickListener {
            applicationContext.stopService(intent)
            finish()
        }
        binding.btnSnooze.setOnClickListener {

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MINUTE, 10)

            val newAlarm = Alarm(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                recurring = true,
                monday = false,
                tuesday = false,
                wednesday = false,
                thursday = false,
                friday = false,
                saturday = false,
                sunday = false,
                label = "Ertele",
                isOn = true,
                System.currentTimeMillis(),
                System.currentTimeMillis()
            )
            newAlarm.schedule(applicationContext)


            applicationContext.stopService(intent)
            finish()

        }

    }


    private fun initTimeAndLabel() {
        val alarmHour = intent.getIntExtra("HOUR", 0)
        val alarmMinute = intent.getIntExtra("MINUTE", 0)
        val alarmTime = String.format("%02d : %02d", alarmHour, alarmMinute)
        binding.txtALarmTime.text = alarmTime

        intent.getStringExtra("LABEL")?.let { Log.e("Label ring", it) }
        val alarmLabel = intent.getStringExtra("LABEL")

        binding.txtAlarmLabel.text = alarmLabel
    }
}