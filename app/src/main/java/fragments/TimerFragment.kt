package fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.alarmapp.R
import com.example.alarmapp.databinding.FragmentTimerBinding
import service.TimerService

class TimerFragment : Fragment() {
    private lateinit var binding: FragmentTimerBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTimerBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.timerToolbar.inflateMenu(R.menu.timer_menu)
        binding.timePicker.setIs24HourView(true)


        val sharedPreferences = this.activity?.getSharedPreferences("pref", MODE_PRIVATE)
        val timeLeft = sharedPreferences?.getLong("time left", 0)
        val lastActive = sharedPreferences?.getBoolean("active", false)
        var isActive = false

        if (timeLeft != null) {
            if (timeLeft > 1 && lastActive == true) {
                isActive = true
                with(sharedPreferences.edit()) {
                    this?.putBoolean("active", isActive)
                    this?.apply()
                }
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = true
                binding.btnStop.isEnabled = true
                binding.timePicker.visibility = View.GONE
                binding.timeLeftLayout.visibility = View.VISIBLE
                binding.progressBar.max = sharedPreferences.getInt("progress max", 0)
                val hour = timeLeft / 3600
                val minute = timeLeft / 60 % 60
                val second = timeLeft % 60
                binding.txtTime.text = (String.format("%02d", hour) + ":" + String.format(
                    "%02d",
                    minute
                ) + ":" + String.format("%02d", second))
                LocalBroadcastManager.getInstance(requireActivity())
                    .registerReceiver(broadcastReceiver, IntentFilter(TimerService.COUNTDOWN))
            }

            else if (timeLeft > 1 && lastActive == false) {
                binding.btnPause.text = "Devam Et"
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = true
                binding.btnStop.isEnabled = true
                isActive = false
                with(sharedPreferences.edit()) {
                    this?.putBoolean("active", isActive)
                    this?.apply()
                }
                binding.timePicker.visibility = View.GONE
                binding.timeLeftLayout.visibility = View.VISIBLE
                val hour = timeLeft / 3600
                val minute = timeLeft / 60 % 60
                val second = timeLeft % 60
                binding.txtTime.text = (String.format("%02d", hour) + ":" + String.format(
                    "%02d",
                    minute
                ) + ":" + String.format("%02d", second))
                binding.progressBar.max = sharedPreferences.getInt("progress max", 0)
                binding.progressBar.progress = binding.progressBar.max - timeLeft.toInt()
            }

            else if (timeLeft <= 1) {
                Log.e("Reset timer", "true")
                resetTimer()
            }
        }

        binding.btnStart.setOnClickListener {
            isActive = true
            binding.btnStart.isEnabled = false
            binding.btnPause.isEnabled = true
            binding.btnStop.isEnabled = true
            binding.timePicker.visibility = View.GONE
            binding.timeLeftLayout.visibility = View.VISIBLE
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute

            binding.progressBar.max = setNewTime(0, minute, hour).toInt()
            with(sharedPreferences?.edit()) {
                this?.putInt("progress max", binding.progressBar.max)
                this?.apply()
            }
            sharedPreferences?.edit()?.putBoolean("active", true)?.apply()
            val intent = Intent(context, TimerService::class.java)
            intent.putExtra("New time", setNewTime(0, minute, hour))
            context?.startService(intent)
        }
        binding.btnPause.setOnClickListener {
            if (isActive) {
                isActive = false
                val intent = Intent(context, TimerService::class.java)
                context?.stopService(intent)
                binding.btnPause.text = "Devam Et"
                binding.btnStart.isEnabled = false
                with(sharedPreferences?.edit()) {
                    this?.putBoolean("active", false)
                    this?.apply()
                }
            } else {
                val timeLefAfterPause = sharedPreferences?.getLong("time left", 0)
                binding.progressBar.max = sharedPreferences?.getInt("progress max", 0)!!
                Log.e("max progress", binding.progressBar.max.toString())
                var hour = 0
                var minute = 0
                var second = 0
                if (timeLefAfterPause != null) {
                    hour = (timeLefAfterPause / 3600).toInt()
                    minute = (timeLefAfterPause / 60 % 60).toInt()
                    second = (timeLefAfterPause % 60).toInt()
                }
                val intent = Intent(context, TimerService::class.java)
                intent.putExtra("New time", setNewTime(second, minute, hour))
                context?.startService(intent)
                binding.btnStart.isEnabled = false
                isActive = true
                binding.btnPause.text = "Durdur"
                with(sharedPreferences.edit()) {
                    this?.putBoolean("active", true)
                    this?.apply()
                }
            }
        }
        binding.btnStop.setOnClickListener {


            isActive = false
            val intent = Intent(context, TimerService::class.java)
            context?.stopService(intent)
            with(sharedPreferences?.edit()) {
                this?.putLong("time left", 0)
                this?.apply()
            }
            with(sharedPreferences?.edit()) {
                this?.putBoolean("active", false)
                this?.apply()
            }
            resetTimer()

        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(broadcastReceiver, IntentFilter(TimerService.COUNTDOWN))
        LocalBroadcastManager.getInstance(requireActivity())
            .registerReceiver(broadcastReceiver, IntentFilter(TimerService.TIME_OUT))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(broadcastReceiver)
    }

    override fun onStop() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(broadcastReceiver)
        super.onStop()
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(broadcastReceiver)
        super.onDestroy()

    }

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                if (TimerService.COUNTDOWN == intent.action) {
                    updateUI(intent)
                }
                if (TimerService.TIME_OUT == intent.action) {
                    resetTimer()
                }
            }
        }

    }

    private fun resetTimer() {
        binding.timePicker.hour = 0
        binding.timePicker.minute = 0
        binding.btnPause.isEnabled = false
        binding.btnStop.isEnabled = false
        binding.btnStart.isEnabled = true
        binding.btnPause.text = "Durdur"
        binding.timePicker.visibility = View.VISIBLE
        binding.timeLeftLayout.visibility = View.GONE
    }

    private fun setNewTime(sec: Int, min: Int, hour: Int): Long {
        return (hour * 3600 + min * 60 + sec).toLong()

    }

    private fun updateUI(intent: Intent) {
        val time = intent.getLongExtra("countdown", 0)
        val hour = time / 3600
        val minute = time / 60 % 60
        val second = time % 60
        binding.txtTime.text = (String.format("%02d", hour) + ":" + String.format(
            "%02d",
            minute
        ) + ":" + String.format("%02d", second))
        binding.progressBar.progress = binding.progressBar.max - time.toInt()
    }
}