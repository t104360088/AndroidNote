package com.example.mycalendarview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        calendarPicker.showDateTitle(true)
        calendarPicker.setDisableWeek(4)
        calendarPicker.setShortWeekDays(true)
        calendarPicker.minDate = Date()
        calendarPicker.date = Date()

        btn_selected.setOnClickListener{ v -> Log.e("time", calendarPicker.selectedDate.toString()) }

        btn_mark.setOnClickListener { view ->
            val calendar = Calendar.getInstance()
            val random = Random(System.currentTimeMillis())
            val style = random.nextInt(2)
            val daySelected = random.nextInt(calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.DAY_OF_MONTH, daySelected)

            when (style) {
                0 -> calendarPicker.markCircleImage1(calendar.time)
                1 -> calendarPicker.markCircleImage2(calendar.time)
                else -> {
                }
            }
        }

        btn_clean.setOnClickListener { v -> calendarPicker.clearSelectedDay() }

        calendarPicker.setCustomCalendarListener(object : CustomCalendarView.CustomCalendarListener {
            override fun onDayClick(date: Date) {
                Toast.makeText(this@MainActivity, "onDayClick: $date", Toast.LENGTH_SHORT).show()
            }

            override fun onDayLongClick(date: Date) {
                Toast.makeText(this@MainActivity, "onDayLongClick: $date", Toast.LENGTH_SHORT).show()
            }

            override fun onLeftButtonClick() {
                Toast.makeText(this@MainActivity, "onRightButtonClick", Toast.LENGTH_SHORT).show()
            }

            override fun onRightButtonClick() {
                Toast.makeText(this@MainActivity, "onLeftButtonClick", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
