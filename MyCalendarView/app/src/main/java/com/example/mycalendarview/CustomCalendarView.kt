package com.example.mycalendarview

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * source: https://github.com/marcohc/roboto-calendar-view
 * revise: 2019/3/23
 */
class CustomCalendarView : LinearLayout {

    // View
    private var root: View? = null
    private var dateTitle: TextView? = null
    private var leftButton: ImageView? = null
    private var rightButton: ImageView? = null
    private var calendarMonthLayout: ViewGroup? = null

    // Calendar
    private var minCalendar = Calendar.getInstance()
    private var currentCalendar = Calendar.getInstance()
    private var lastSelectedDayCalendar: Calendar? = null

    // Event
    private var listener: CustomCalendarListener? = null
    private val onClickListener = OnClickListener { view ->
        // Extract day selected
        val dayOfTheMonthContainer = view as ViewGroup
        var tagId = dayOfTheMonthContainer.tag as String
        tagId = tagId.substring(DAY_OF_THE_MONTH_LAYOUT.length, tagId.length)
        val dayOfTheMonthText = view.findViewWithTag<TextView>(DAY_OF_THE_MONTH_TEXT + tagId)


        // Extract the day from the text
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayOfTheMonthText.text.toString()))

        // Mark day
        val isGreaterThanMinDate = calendar.compareTo(minCalendar) >= 1
        if (disableWeek != calendar.get(Calendar.DAY_OF_WEEK) && isGreaterThanMinDate)
            markDayAsSelectedDay(calendar.time)

        // Fire event
        if (listener == null)
            throw IllegalStateException("You must assign a valid CustomCalendarListener first!")
        else {
            if (disableWeek != calendar.get(Calendar.DAY_OF_WEEK) && isGreaterThanMinDate)
                listener?.onDayClick(calendar.time)
        }
    }
    private val onLongClickListener = OnLongClickListener { view ->
        // Extract day selected
        val dayOfTheMonthContainer = view as ViewGroup
        var tagId = dayOfTheMonthContainer.tag as String
        tagId = tagId.substring(DAY_OF_THE_MONTH_LAYOUT.length, tagId.length)
        val dayOfTheMonthText = view.findViewWithTag<TextView>(DAY_OF_THE_MONTH_TEXT + tagId)

        // Extract the day from the text
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(dayOfTheMonthText.text.toString()))

        // Mark day
        val isGreaterThanMinDate = calendar.compareTo(minCalendar) >= 1
        if (disableWeek != calendar.get(Calendar.DAY_OF_WEEK) && isGreaterThanMinDate)
            markDayAsSelectedDay(calendar.time)

        // Fire event
        if (listener == null)
            throw IllegalStateException("You must assign a valid RobotoCalendarListener first!")
        else {
            if (disableWeek != calendar.get(Calendar.DAY_OF_WEEK) && isGreaterThanMinDate)
                listener?.onDayClick(calendar.time)
        }
        true
    }

    // Other
    private var shortWeekDays = false
    private var disableWeek: Int = 0

    // The date of minimum
    var minDate: Date?
        get() = minCalendar.time
        set(date) {
            minCalendar.time = date
            updateView()
        }

    // The date of now
    var date: Date
        get() = currentCalendar.time
        set(date) {
            currentCalendar.time = date
            updateView()
        }

    // Get selected date
    val selectedDate: Date?
        get() = lastSelectedDayCalendar?.time


    /**
     * Initial
     */
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        if (!isInEditMode) {
            val inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            root = inflate.inflate(R.layout.calendar_view, this, true)
            root?.let { findViewsById(it) }
            setUpEventListeners()

            minCalendar.set(1970, 0, 1, 0, 0, 0)
            updateView()
        }
    }

    companion object {

        private const val DAY_OF_THE_WEEK_TEXT = "dayOfTheWeekText"
        private const val DAY_OF_THE_WEEK_LAYOUT = "dayOfTheWeekLayout"
        private const val DAY_OF_THE_MONTH_LAYOUT = "dayOfTheMonthLayout"
        private const val DAY_OF_THE_MONTH_TEXT = "dayOfTheMonthText"
        private const val DAY_OF_THE_MONTH_BACKGROUND = "dayOfTheMonthBackground"
        private const val DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 = "dayOfTheMonthCircleImage1"
        private const val DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 = "dayOfTheMonthCircleImage2"
    }

    interface CustomCalendarListener {

        fun onDayClick(date: Date)

        fun onDayLongClick(date: Date)

        fun onRightButtonClick()

        fun onLeftButtonClick()
    }

    fun showDateTitle(show: Boolean) {
        calendarMonthLayout?.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun setShortWeekDays(shortWeekDays: Boolean) {
        this.shortWeekDays = shortWeekDays
        setUpWeekDaysLayout()
    }

    fun setDisableWeek(week: Int) {
        disableWeek = if (week in 1..7) week else 0
    }

    fun setCustomCalendarListener(listener: CustomCalendarListener) {
        this.listener = listener
    }

    fun markCircleImage1(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val circleImage1 = getCircleImage1(calendar)
        circleImage1.visibility = View.VISIBLE

        if (lastSelectedDayCalendar != null && isTheSameDay(calendar, lastSelectedDayCalendar!!))
            setDrawbleTint(circleImage1, R.color.calendar_selected_day_font)
        else
            setDrawbleTint(circleImage1, R.color.calendar_circle_1)
    }

    fun markCircleImage2(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date

        val circleImage2 = getCircleImage2(calendar)
        circleImage2.visibility = View.VISIBLE

        if (lastSelectedDayCalendar != null && isTheSameDay(calendar, lastSelectedDayCalendar!!))
            setDrawbleTint(circleImage2, R.color.calendar_selected_day_font)
        else
            setDrawbleTint(circleImage2, R.color.calendar_circle_2)
    }

    fun clearSelectedDay() {
        lastSelectedDayCalendar?.let {

            // If it's today, keep the current day style
            val now = Calendar.getInstance()
            val bg = when {
                now.equals(it, Calendar.YEAR) && now.equals(it, Calendar.DAY_OF_YEAR) -> R.drawable.ring
                else -> android.R.color.transparent
            }
            val dayOfTheMonthBackground = getDayOfMonthBackground(it)
            dayOfTheMonthBackground.setBackgroundResource(bg)

            val dayOfTheMonth = getDayOfMonthText(it)
            dayOfTheMonth.setTextColor(ContextCompat.getColor(context, R.color.calendar_day_of_the_month_font))

            val circleImage1 = getCircleImage1(it)
            val circleImage2 = getCircleImage2(it)

            if (circleImage1.visibility == View.VISIBLE)
                setDrawbleTint(circleImage1, R.color.calendar_circle_1)

            if (circleImage2.visibility == View.VISIBLE)
                setDrawbleTint(circleImage2, R.color.calendar_circle_2)

            lastSelectedDayCalendar = null
        }
    }

    private fun findViewsById(view: View) {

        calendarMonthLayout = view.findViewById(R.id.view_calendar)
        leftButton = view.findViewById(R.id.btn_left)
        rightButton = view.findViewById(R.id.btn_right)
        dateTitle = view.findViewById(R.id.tv_month)

        val inflate = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (i in 0..41) {

            val weekIndex = i % 7 + 1
            val dayOfTheWeekLayout = view.findViewWithTag<ViewGroup>(DAY_OF_THE_WEEK_LAYOUT + weekIndex)

            // Create day of the month
            val dayOfTheMonthLayout = inflate.inflate(R.layout.calendar_day, null)
            val dayOfTheMonthText = dayOfTheMonthLayout.findViewWithTag<TextView>(DAY_OF_THE_MONTH_TEXT)
            val dayOfTheMonthBackground = dayOfTheMonthLayout.findViewWithTag<ViewGroup>(DAY_OF_THE_MONTH_BACKGROUND)
            val dayOfTheMonthCircleImage1 = dayOfTheMonthLayout.findViewWithTag<ImageView>(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1)
            val dayOfTheMonthCircleImage2 = dayOfTheMonthLayout.findViewWithTag<ImageView>(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2)

            // Set tags to identify them
            val viewIndex = i + 1
            dayOfTheMonthLayout.setTag(DAY_OF_THE_MONTH_LAYOUT + viewIndex)
            dayOfTheMonthText.setTag(DAY_OF_THE_MONTH_TEXT + viewIndex)
            dayOfTheMonthBackground.setTag(DAY_OF_THE_MONTH_BACKGROUND + viewIndex)
            dayOfTheMonthCircleImage1.setTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 + viewIndex)
            dayOfTheMonthCircleImage2.setTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 + viewIndex)

            dayOfTheWeekLayout.addView(dayOfTheMonthLayout)
        }
    }

    private fun setUpEventListeners() {

        leftButton?.setOnClickListener { view ->
            if (listener == null)
                throw IllegalStateException("You must assign a valid RobotoCalendarListener first!")

            // Decrease month
            currentCalendar.add(Calendar.MONTH, -1)
            lastSelectedDayCalendar = null
            updateView()
            listener?.onLeftButtonClick()
        }

        rightButton?.setOnClickListener { view ->
            if (listener == null)
                throw IllegalStateException("You must assign a valid RobotoCalendarListener first!")

            // Increase month
            currentCalendar.add(Calendar.MONTH, 1)
            lastSelectedDayCalendar = null
            updateView()
            listener?.onRightButtonClick()
        }
    }

    private fun setUpMonthLayout() {
        val calendar = Calendar.getInstance()
        var dateText = DateFormatSymbols(Locale.getDefault()).months[currentCalendar.get(Calendar.MONTH)]
        dateText = dateText.substring(0, 1).toUpperCase() + dateText.subSequence(1, dateText.length)

        dateTitle?.text = if (calendar.equals(currentCalendar, Calendar.YEAR)) dateText
                        else String.format("%s %s", dateText, currentCalendar.get(Calendar.YEAR))
    }

    private fun setUpWeekDaysLayout() {
        var dayOfWeek: TextView
        var dayOfTheWeekString: String
        val weekDaysArray = DateFormatSymbols(Locale.getDefault()).shortWeekdays
        val length = weekDaysArray.size

        for (i in 1 until length) {
            dayOfWeek = root!!.findViewWithTag(DAY_OF_THE_WEEK_TEXT + getWeekIndex(i, currentCalendar))
            dayOfTheWeekString = weekDaysArray[i]

            if (shortWeekDays)
                dayOfTheWeekString =
                    if ("TW" == Locale.getDefault().country)
                        dayOfTheWeekString.substring(dayOfTheWeekString.length-1, dayOfTheWeekString.length)
                    else
                        dayOfTheWeekString.substring(0, 1).toUpperCase()

            dayOfWeek.text = dayOfTheWeekString
        }
    }

    private fun setUpDaysOfMonthLayout() {

        var dayOfTheMonthText: TextView
        var circleImage1: View
        var circleImage2: View
        var dayOfTheMonthContainer: ViewGroup
        var dayOfTheMonthBackground: ViewGroup

        for (i in 1..42) {
            root?.let {
                dayOfTheMonthContainer = it.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + i)
                dayOfTheMonthBackground = it.findViewWithTag(DAY_OF_THE_MONTH_BACKGROUND + i)
                dayOfTheMonthText = it.findViewWithTag(DAY_OF_THE_MONTH_TEXT + i)
                circleImage1 = it.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1 + i)
                circleImage2 = it.findViewWithTag(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2 + i)

                dayOfTheMonthText.visibility = View.INVISIBLE
                circleImage1.visibility = View.GONE
                circleImage2.visibility = View.GONE

                // Apply styles
                dayOfTheMonthText.setBackgroundResource(android.R.color.transparent)
                dayOfTheMonthText.setTypeface(null, Typeface.NORMAL)
                dayOfTheMonthText.setTextColor(ContextCompat.getColor(context, R.color.calendar_day_of_the_month_font))
                dayOfTheMonthContainer.setBackgroundResource(android.R.color.transparent)
                dayOfTheMonthContainer.setOnClickListener(null)
                dayOfTheMonthBackground.setBackgroundResource(android.R.color.transparent)
            }
        }
    }

    private fun setUpDaysInCalendar() {

        val auxCalendar = Calendar.getInstance(Locale.getDefault())
        auxCalendar.time = currentCalendar.time
        auxCalendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayOfMonth = auxCalendar.get(Calendar.DAY_OF_WEEK)
        var dayOfTheMonthText: TextView?
        var dayOfTheMonthContainer: ViewGroup
        var dayOfTheMonthLayout: ViewGroup

        // Calculate dayOfTheMonthIndex
        var dayOfTheMonthIndex = getWeekIndex(firstDayOfMonth, auxCalendar)

        run {
            var i = 1
            while (i <= auxCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)) {
                dayOfTheMonthContainer = root!!.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + dayOfTheMonthIndex)
                dayOfTheMonthText = root!!.findViewWithTag(DAY_OF_THE_MONTH_TEXT + dayOfTheMonthIndex)

                if (dayOfTheMonthText == null) break

                dayOfTheMonthContainer.setOnClickListener(onClickListener)
                dayOfTheMonthContainer.setOnLongClickListener(onLongClickListener)
                dayOfTheMonthText?.visibility = View.VISIBLE
                dayOfTheMonthText?.text = i.toString()
                i++
                dayOfTheMonthIndex++
            }
        }

        for (i in 36..42) {
            dayOfTheMonthText = root!!.findViewWithTag(DAY_OF_THE_MONTH_TEXT + i)
            dayOfTheMonthLayout = root!!.findViewWithTag(DAY_OF_THE_MONTH_LAYOUT + i)

            dayOfTheMonthLayout.visibility =
                if (dayOfTheMonthText?.visibility == View.INVISIBLE)
                    View.GONE
                else
                    View.VISIBLE
        }
    }

    private fun markDayAsCurrentDay() {
        // If it's the current month, mark current day
        val calendar = Calendar.getInstance()

        if (calendar.equals(currentCalendar, Calendar.YEAR) &&
            calendar.equals(currentCalendar, Calendar.MONTH)) {

            val dayOfTheMonthBackground = getDayOfMonthBackground(calendar)
            dayOfTheMonthBackground.setBackgroundResource(R.drawable.ring)
        }
    }

    private fun markDayAsSelectedDay(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date

        // Clear previous current day mark
        clearSelectedDay()

        // Store current values as last values
        lastSelectedDayCalendar = calendar

        // Mark current day as selected
        val dayOfTheMonth = getDayOfMonthText(calendar)
        val dayOfTheMonthBackground = getDayOfMonthBackground(calendar)
        val selectedColor = R.color.calendar_selected_day_font
        val circleImage1 = getCircleImage1(calendar)
        val circleImage2 = getCircleImage2(calendar)

        dayOfTheMonth.setTextColor(ContextCompat.getColor(context, selectedColor))
        dayOfTheMonthBackground.setBackgroundResource(R.drawable.circle)

        if (circleImage1.visibility == View.VISIBLE)
            setDrawbleTint(circleImage1, selectedColor)

        if (circleImage2.visibility == View.VISIBLE)
            setDrawbleTint(circleImage2, selectedColor)
    }

    private fun updateView() {
        setUpMonthLayout()
        setUpWeekDaysLayout()
        setUpDaysOfMonthLayout()
        setUpDaysInCalendar()
        markDayAsCurrentDay()
    }

    /**
     * Get view
     */
    private fun getDayOfMonthBackground(currentCalendar: Calendar): ViewGroup {
        return getView(DAY_OF_THE_MONTH_BACKGROUND, currentCalendar) as ViewGroup
    }

    private fun getDayOfMonthText(currentCalendar: Calendar): TextView {
        return getView(DAY_OF_THE_MONTH_TEXT, currentCalendar) as TextView
    }

    private fun getCircleImage1(currentCalendar: Calendar): ImageView {
        return getView(DAY_OF_THE_MONTH_CIRCLE_IMAGE_1, currentCalendar) as ImageView
    }

    private fun getCircleImage2(currentCalendar: Calendar): ImageView {
        return getView(DAY_OF_THE_MONTH_CIRCLE_IMAGE_2, currentCalendar) as ImageView
    }

    private fun getView(key: String, currentCalendar: Calendar): View {
        val index = getDayIndexByDate(currentCalendar)
        return root!!.findViewWithTag(key + index)
    }

    /**
     * Get correct time
     */
    private fun getDayIndexByDate(currentCalendar: Calendar): Int {
        val monthOffset = getMonthOffset(currentCalendar)
        val currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
        return currentDay + monthOffset
    }

    private fun getMonthOffset(currentCalendar: Calendar): Int {
        val calendar = Calendar.getInstance()
        calendar.time = currentCalendar.time
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val firstDayWeekPosition = calendar.firstDayOfWeek
        val dayPosition = calendar.get(Calendar.DAY_OF_WEEK)

        return when (firstDayWeekPosition) {
            1 -> dayPosition - 1
            else -> if (dayPosition == 1) 6 else dayPosition - 2
        }
    }

    private fun getWeekIndex(weekIndex: Int, currentCalendar: Calendar): Int {
        val firstDayWeekPosition = currentCalendar.firstDayOfWeek

        return when (firstDayWeekPosition) {
            1 -> weekIndex
            else -> if (weekIndex == 1) 7 else weekIndex - 1
        }
    }

    /**
     * Other tool
     */
    private fun setDrawbleTint(image: ImageView, color: Int) {
        DrawableCompat.setTint(image.drawable, ContextCompat.getColor(context, color))
    }

    private fun isTheSameDay(calendarOne: Calendar, calendarTwo: Calendar): Boolean {

        return calendarOne.equals(calendarTwo, Calendar.YEAR) &&
                calendarOne.equals(calendarTwo, Calendar.DAY_OF_YEAR)
    }

    private fun Calendar.equals(other: Calendar, unit: Int): Boolean {
        return this.get(unit) == other.get(unit)
    }
}
