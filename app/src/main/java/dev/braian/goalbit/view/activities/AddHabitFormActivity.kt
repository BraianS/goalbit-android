package dev.braian.goalbit.view.activities


import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.maltaisn.icondialog.IconDialog
import com.maltaisn.icondialog.IconDialogSettings
import com.maltaisn.icondialog.data.Icon
import com.maltaisn.icondialog.pack.IconPack
import dev.braian.goalbit.R
import dev.braian.goalbit.utils.App
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.ActivityAddHabitFormBinding
import dev.braian.goalbit.model.Day
import dev.braian.goalbit.model.DurationTimer
import dev.braian.goalbit.model.GoalType
import dev.braian.goalbit.model.Habit
import dev.braian.goalbit.model.RangeOption
import dev.braian.goalbit.model.Reminder
import dev.braian.goalbit.view.viewholder.HabitViewModel
import dev.braian.goalbit.utils.DateFormatUtils
import dev.braian.goalbit.utils.HabitReminderReceiver
import dev.braian.goalbit.utils.NotificationHelper
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale


class AddHabitFormActivity : AppCompatActivity(), IconDialog.Callback {

    private lateinit var binding: ActivityAddHabitFormBinding
    private lateinit var editName: EditText
    private lateinit var editDesc: EditText
    private lateinit var editReminderTimepick: EditText
    private lateinit var imageSelectIconDialog: ImageView
    private lateinit var buttonDuration: Button
    private lateinit var buttonCreateHabit: Button
    private lateinit var chipGroup: ChipGroup
    private lateinit var autoCompleteTextView: AutoCompleteTextView
    private lateinit var imageRemoveReminder: ImageView


    private lateinit var auth: FirebaseAuth

    private lateinit var habit: Habit
    private var dataEnd: String? = ""
    private var goalType: GoalType = GoalType.Off

    private lateinit var adapterItems: ArrayAdapter<String>
    private var items = GoalType.values().map { it.name }

    private lateinit var autoCompleteRangeDatePick: AutoCompleteTextView
    private lateinit var adapterItemsRangeDatePick: ArrayAdapter<String>
    private var rangeOption = arrayListOf("Off", "Yes")

    private var rangeOptionEnumClass: RangeOption = RangeOption.Off

    private var reminder: Reminder = Reminder()

    private var durationTimer: DurationTimer = DurationTimer()

    private var selectedDay = mutableSetOf<Day>()

    private lateinit var habitViewModel: HabitViewModel


    var icon = 0

    private var calendar: Calendar = Calendar.getInstance()

    private fun bindViews() {
        editName = findViewById(binding.editName.id)
        editDesc = findViewById(binding.editDescription.id)
        editReminderTimepick = findViewById(binding.editReminderTimepick.id)
        chipGroup = findViewById(binding.childGroup.id)
        autoCompleteTextView = findViewById(binding.autoCompleteText.id)
        buttonDuration = findViewById(binding.buttonDurationPick.id)
        autoCompleteRangeDatePick = findViewById(binding.autoCompleteRangeDatePick.id)
        buttonCreateHabit = findViewById(binding.buttonCreateHabit.id)
        imageSelectIconDialog = findViewById(binding.imageSelectIconDialog.id)
        imageRemoveReminder = findViewById(binding.imageRemoveReminder.id)

        adapterItems = ArrayAdapter<String>(this, R.layout.list_item, items)
        autoCompleteTextView.setAdapter(adapterItems)

        adapterItemsRangeDatePick =
            ArrayAdapter<String>(this, R.layout.list_range_date, rangeOption)
        autoCompleteRangeDatePick.setAdapter(adapterItemsRangeDatePick)
    }

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        notificationHelper = NotificationHelper(context = applicationContext)

        val iconDialog =
            supportFragmentManager.findFragmentByTag(DataBaseConstants.TAG.ICON_DIALOG_TAG) as IconDialog?
                ?: IconDialog.newInstance(IconDialogSettings())

        binding = ActivityAddHabitFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        habitViewModel = HabitViewModel()

        val extras = intent.extras
        val id = extras?.getString(DataBaseConstants.HABIT.ID)
        Log.i(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY, "Id is: $id")

        bindViews()
        getAllChipGroups()

        imageRemoveReminder.isClickable = true

        if (id == null) {
            startSelectAllDaysFromChipGroup()
            bindOnClicks()



            buttonCreateHabit.setOnClickListener {
                if (editName.text.isEmpty()) {
                    Toast.makeText(
                        applicationContext,
                        getString(R.string.name_empty),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {


                    habit = Habit(
                        "",
                        editName.text.toString(),
                        editDesc.text.toString(),
                        auth.currentUser?.uid.toString(),
                        reminder,
                        dataEnd,
                        icon,
                        rangeOptionEnumClass,
                        goalType,
                        durationTimer,
                        arrayListOf()
                    )
                    habit.save()

                    Toast.makeText(
                        applicationContext,
                        getString(R.string.save_success),
                        Toast.LENGTH_SHORT
                    )
                        .show()

                    if (!editReminderTimepick.text.isEmpty()) {

                        val habitId = habit.id.hashCode()

                        var intent = Intent(applicationContext, HabitReminderReceiver::class.java)
                        intent.putExtra(DataBaseConstants.NOTIFICATION.HOUR, reminder.hour)
                        intent.putExtra(DataBaseConstants.NOTIFICATION.MINUTE, reminder.minute)
                        intent.putExtra(DataBaseConstants.NOTIFICATION.habitId, habitId)
                        intent.putExtra(DataBaseConstants.NOTIFICATION.habitName,habit.name)

                        notificationHelper.scheduleNotification(this, intent)

                    }
                    finish()
                }

            }

            binding.imageSelectIconDialog.setOnClickListener {
                iconDialog.show(supportFragmentManager, DataBaseConstants.TAG.ICON_DIALOG_TAG)
            }


        } else {
            habitViewModel.getHabitByIdAndUserId(id, auth.currentUser?.uid) {

                buttonCreateHabit.text = getString(R.string.update)
                editName.setText(it?.name)
                editDesc.setText(it?.description)

                binding.imageSelectIconDialog.setOnClickListener {
                    iconDialog.show(supportFragmentManager, DataBaseConstants.TAG.ICON_DIALOG_TAG)
                }

                val iconPack = (applicationContext as App).iconPack

                icon = it!!.icon

                binding.imageSelectIconDialog.setImageDrawable(iconPack?.getIcon(it.icon)?.drawable)

                if (it.reminder.enable == true) {

                    editReminderTimepick.setOnClickListener {
                        displayReminderPickDialog(reminder.hour!!, reminder.minute!!)
                    }

                    val formattedTimeRange =
                        DateFormatUtils.formatHourAndMinutes(
                            it?.reminder?.hour!!,
                            it?.reminder?.minute!!
                        )

                    editReminderTimepick.setText(formattedTimeRange)

                    reminder.hour = it?.reminder?.hour!!
                    reminder.minute = it?.reminder?.minute!!


                    imageRemoveReminder.setOnClickListener(object : OnClickListener {

                        override fun onClick(p0: View?) {
                            reminder.enable = false
                            reminder.hour = null
                            reminder.minute = null
                            editReminderTimepick.setText("")

                            Toast.makeText(applicationContext, "Imagem deletada", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    editReminderTimepick.setOnClickListener {
                        displayReminderPickDialog()
                    }
                }

                buttonCreateHabit.setOnClickListener {

                    if (editName.text.isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.name_empty),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    } else {

                        habit = Habit(
                            id,
                            editName.text.toString(),
                            editDesc.text.toString(),
                            auth.currentUser?.uid.toString(),
                            reminder,
                            dataEnd,
                            icon,
                            rangeOptionEnumClass,
                            goalType,
                            durationTimer,
                            arrayListOf()
                        )
                        val habitId = habit.id.hashCode()

                        if (!editReminderTimepick.text.isEmpty()) {

                            var intent =
                                Intent(applicationContext, HabitReminderReceiver::class.java)
                            intent.putExtra(DataBaseConstants.NOTIFICATION.HOUR, reminder.hour)
                            intent.putExtra(DataBaseConstants.NOTIFICATION.MINUTE, reminder.minute)
                            intent.putExtra(DataBaseConstants.NOTIFICATION.habitId, habitId)
                            intent.putExtra(DataBaseConstants.NOTIFICATION.habitName, habit.name)

                            notificationHelper.scheduleNotification(this, intent)

                        } else {
                            Log.i(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY,"Notificação cancelada")
                            notificationHelper.cancelNotification(habitId)
                        }

                        habitViewModel.updateHabitById(id, habit)

                        Toast.makeText(
                            applicationContext,
                            getString(R.string.update_success),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        finish()
                    }
                }



                if (it?.goal?.name.equals(GoalType.Duration.name)) {

                    goalType = GoalType.Duration
                    buttonDuration.visibility = View.VISIBLE

                    var formattedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        it?.duration?.hour!!,
                        it?.duration?.minute!!,
                        it?.duration?.second!!
                    )

                    durationTimer.addTimeStamp(
                        it?.duration?.hour!!,
                        it?.duration?.minute!!,
                        it?.duration?.second!!
                    )
                    buttonDuration.text = formattedTime

                    buttonDuration.setOnClickListener {

                        val timePickerDialog =
                            TimePickerDialog(
                                this,
                                { view, hour, minute ->

                                    durationTimer.addTimeStamp(hour, minute, 0)
                                    buttonDuration.text =
                                        DateFormatUtils.formatHourAndMinutes(hour, minute)
                                }, durationTimer.hour!!, durationTimer.minute!!, true
                            )
                        timePickerDialog.show()
                    }

                } else if (it?.goal?.name.equals(GoalType.Off.name)) {
                    goalType = GoalType.Off
                }

                buttonDuration.setOnClickListener {
                    displayDurationTImerPickerDialog(0, 15)
                }

                if (it?.rangeOption?.name.equals(RangeOption.Yes.name)) {

                    rangeOptionEnumClass = RangeOption.Yes
                    dataEnd = it?.dateEnd
                    durationTimer = DurationTimer()
                }

                val days = it?.reminder?.days?.weekdays

                for (i in 0 until chipGroup.childCount) {
                    val chip: Chip = chipGroup.getChildAt(i) as Chip
                    for (day in days!!) {
                        Log.d(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY, "Day: $day")
                        if (chip.text.toString().contains(day.name.substring(0, 3).lowercase())) {
                            selectedDay.add(day)
                            chip.isChecked = true
                        }
                    }
                }

                autoCompleteTextView.setOnItemClickListener { adapterView, view, i, l ->
                    var item: String = adapterView?.getItemAtPosition(i).toString()
                    if (item == GoalType.Duration.toString()) {
                        goalType = GoalType.Duration
                        buttonDuration.visibility = View.VISIBLE
                    } else {
                        goalType = GoalType.Off
                        buttonDuration.visibility = View.INVISIBLE
                    }
                    Toast.makeText(applicationContext, "Item: " + item, Toast.LENGTH_SHORT)
                        .show()
                }

                Log.d(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY, "Data end is : $dataEnd")

                if (dataEnd != "") {
                    val parseDate = LocalDate.parse(dataEnd)


                    autoCompleteRangeDatePick.setOnItemClickListener { adapterView, view, i, l ->
                        var item: String = adapterView?.getItemAtPosition(i).toString()
                        if (item == "Off") {
                            dataEnd = ""
                            rangeOptionEnumClass = RangeOption.Off
                        }
                        if (item == "Yes") {
                            rangeOptionEnumClass = RangeOption.Yes
                            DatePickerDialog(
                                view!!.context,
                                { view, year, month, day ->
                                    dataEnd =
                                        DateFormatUtils.convertToTimestamp(year, month, day)
                                },
                                parseDate.year,
                                parseDate.monthValue - 1,
                                parseDate.dayOfMonth
                            ).show()
                        }
                    }
                } else {
                    autoCompleteRangeDatePick.setOnItemClickListener { adapterView, view, i, l ->
                        val item: String = adapterView?.getItemAtPosition(i).toString()
                        if (item == "Off") {
                            rangeOptionEnumClass = RangeOption.Off
                        }
                        if (item == "Yes") {
                            rangeOptionEnumClass = RangeOption.Yes
                            displayRangeDatePicker()
                        }
                    }
                }
            }

            habitViewModel.errorMessageLiveData.observe(this, Observer {
                it?.let { errorMessage ->
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun bindOnClicks() {
        imageRemoveReminder.setOnClickListener(object : OnClickListener {

            override fun onClick(p0: View?) {
                reminder.enable = false
                reminder.hour = null
                reminder.minute = null
                editReminderTimepick.setText("")

                Toast.makeText(applicationContext, "Imagem deletada", Toast.LENGTH_LONG).show()
            }
        })

        autoCompleteRangeDatePick.setOnItemClickListener { adapterView, view, i, l ->
            val item: String = adapterView?.getItemAtPosition(i).toString()
            if (item == "Off") {
                rangeOptionEnumClass = RangeOption.Off
            }
            if (item == "Yes") {
                rangeOptionEnumClass = RangeOption.Yes
                displayRangeDatePicker()
            }
        }

        autoCompleteTextView.setOnItemClickListener { adapterView, view, i, l ->
            val item: String = adapterView?.getItemAtPosition(i).toString()
            if (item == GoalType.Duration.toString()) {
                goalType = GoalType.Duration
                buttonDuration.visibility = View.VISIBLE
            } else {
                goalType = GoalType.Off
                buttonDuration.visibility = View.INVISIBLE
            }
            Toast.makeText(applicationContext, "Item: $item", Toast.LENGTH_SHORT).show()
        }

        editReminderTimepick.setOnClickListener { displayReminderPickDialog() }

        buttonDuration.setOnClickListener {
            displayDurationTImerPickerDialog(0, 15)
        }
    }

    private fun getAllChipGroups() {
        chipGroup.setOnCheckedStateChangeListener { group, checkedId ->

            selectedDay.clear()
            for (position in checkedId) {
                val chip = findViewById<Chip>(position)
                if (checkedId.size == 1) {
                    chip.isClickable = false
                    chip.isFocusable = false
                } else {
                    chip.isClickable = true
                    chip.isFocusable = true
                    for (day in Day.values()) {
                        if (chip.text.toString()
                                .contains(day.name.substring(0, 3).lowercase())
                        ) {
                            selectedDay.add(day)
                        }
                    }
                    Log.d(
                        DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY,
                        "Selected Days group: $selectedDay"
                    )
                    reminder.days.addSelectedDays(selectedDay)
                }
            }
        }
    }

    private fun startSelectAllDaysFromChipGroup() {
        for (i in 0 until chipGroup.childCount) {
            val chip: Chip = chipGroup.getChildAt(i) as Chip
            chip.isChecked = true

            for (day in Day.values()) {
                if (chip.text.toString().contains(day.name.substring(0, 3).lowercase())) {
                    selectedDay.add(day)
                }
            }
            Log.d(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY, "Days: $selectedDay")
            reminder.days.addSelectedDays(selectedDay)
            Log.d(
                DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY,
                "Weekdays: ${reminder.days.weekdays}"
            )
        }
    }


    private fun displayRangeDatePicker() {

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            { view, year, month, day ->
                dataEnd = DateFormatUtils.convertToTimestamp(year, month, day)
            }, year, month, day
        ).show()
    }

    private fun displayDurationTImerPickerDialog(hour: Int, minute: Int) {

        val timePickerDialog = TimePickerDialog(
            this,
            { view, hour, minute ->
                val formattedTime = DateFormatUtils.formatHourAndMinutes(hour, minute)
                durationTimer.addTimeStamp(hour, minute, 0)
                buttonDuration.text = formattedTime
            }, hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun displayReminderPickDialog(selectedHour: Int = 0, selectedMinute: Int = 0) {

        val timePickerDialog = TimePickerDialog(
            this,
            { view, hour, minute ->
                val formattedTime = DateFormatUtils.formatHourAndMinutes(hour, minute)

                Log.i(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY, "Hora: $hour")

                    reminder.enable = true
                    reminder.hour = hour
                    reminder.minute = minute

                    editReminderTimepick.setText(formattedTime)


            }, selectedHour, selectedMinute, true
        )
        timePickerDialog.show()


    }

    override val iconDialogIconPack: IconPack?
        get() = (application as App).iconPack

    override fun onIconDialogIconsSelected(
        dialog: IconDialog,
        icons: List<Icon>
    ) {
        val selectedIcon = icons[0]
        icon = selectedIcon.id
        imageSelectIconDialog.setImageDrawable(selectedIcon.drawable)
        icons.map { it.id }
        Log.i(DataBaseConstants.TAG.ADD_HABIT_FORM_ACTIVITY, "Icon id: $icon")
    }
}