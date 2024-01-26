package dev.braian.goalbit.view.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.kizitonwose.calendar.core.WeekDay
import com.kizitonwose.calendar.core.atStartOfMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.view.ViewContainer
import com.kizitonwose.calendar.view.WeekDayBinder
import dev.braian.goalbit.view.activities.HabitDetailsActivity
import dev.braian.goalbit.R
import dev.braian.goalbit.view.adapter.HabitAdapter
import dev.braian.goalbit.view.activities.AddHabitFormActivity
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.FragmentHomeBinding
import dev.braian.goalbit.view.listener.OnHabitListener
import dev.braian.goalbit.view.viewholder.HabitViewModel
import dev.braian.goalbit.utils.DateFormatUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val adapter = HabitAdapter()
    private var selectedDate: Date? = Date()
    private var selectedDateLocal = LocalDate.now()
    private lateinit var habitViewModel: HabitViewModel
    private lateinit var auth: FirebaseAuth

    private fun updateUi(user: FirebaseUser?) {
        if (user != null && user.isAnonymous == false) {

          val name = user?.displayName

            binding.welcomeTextView.text = "Welcome, $name"
            // User is already signed in, update UI accordingly
            Log.d(DataBaseConstants.TAG.HOME_FRAGMENT, "User signed in: ${user.displayName}")

            habitViewModel.getHabitsByUserIdAndOrderByDateEnd(
                selectedDateLocal,
                user?.uid!!
            )
            // Update UI for signed-in user
        } else {
            // User is not signed in, attempt to sign in anonymously
            auth.signInAnonymously()
                .addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        val anonymousUser = auth.currentUser
                        Log.d(
                            DataBaseConstants.TAG.MAIN_ACTIVITY,
                            "Anonymous sign-in successful: ${anonymousUser?.displayName}"
                        )
                        // Update UI or perform other actions for anonymous user

                        // Continue with the initialization of HabitViewModel and fetching habits
                        binding.welcomeTextView.text = "Welcome, Anonymous"
                        habitViewModel.getHabitsByUserIdAndOrderByDateEnd(
                            selectedDateLocal,
                            anonymousUser?.uid!!
                        )
                    } else {
                        Log.e(
                            DataBaseConstants.TAG.MAIN_ACTIVITY,
                            "Anonymous sign-in failed",
                            signInTask.exception
                        )
                    }
                }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.i(DataBaseConstants.TAG.HOME_FRAGMENT, "OnCreateView foi criado")
        habitViewModel = ViewModelProvider(this).get(HabitViewModel::class.java)
        auth = FirebaseAuth.getInstance()


        Log.i("Main Activity", "Usuario criado  ${auth.currentUser?.uid}")


        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        habitViewModel = ViewModelProvider(this).get(HabitViewModel::class.java)

        binding.recycleViewHabit.layoutManager = LinearLayoutManager(context)
        binding.recycleViewHabit.adapter = adapter

        val view = inflater.inflate(R.layout.fragment_home, container, false)



        val listener = object : OnHabitListener {
            override fun onClick(id: String, name: String) {

                val intent = Intent(context, HabitDetailsActivity::class.java)
                val bundle = Bundle()
                bundle.putString(DataBaseConstants.HABIT.ID, id)
                bundle.putString(DataBaseConstants.HABIT.NAME, name)

                intent.putExtras(bundle)
                startActivity(intent)
            }

            override fun onEdit(id: String) {
                val intent = Intent(context, AddHabitFormActivity::class.java)

                val bundle = Bundle()
                bundle.putString(DataBaseConstants.HABIT.ID, id)

                intent.putExtras(bundle)
                startActivity(intent)
            }
        }
        updateUi(auth.currentUser)
        adapter.attachListener(listener)
        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()



        class DayViewContainer(view: View) : ViewContainer(view) {
            val textView = view.findViewById<TextView>(R.id.calendarDayText)
            lateinit var day: WeekDay


            init {
                view.setOnClickListener {

                    Log.i(DataBaseConstants.TAG.HOME_FRAGMENT, "DATA $selectedDate Clicado")

                    if (selectedDateLocal != day.date) {
                        val oldDate = selectedDateLocal
                        selectedDateLocal = day.date
                        binding.weekCalendarView.notifyDateChanged(day.date)
                        oldDate?.let { binding.weekCalendarView.notifyDateChanged(it) }

                        Log.i(DataBaseConstants.TAG.HOME_FRAGMENT, "CLICADO class")
                        habitViewModel.getHabitsByUserIdAndOrderByDateEnd(
                            selectedDateLocal,
                            auth.currentUser!!.uid
                        )
                        adapter.notifyDataSetChanged()
                    }
                }
            }

            fun bind(day: WeekDay) {
                this.day = day

                binding.monthText.text = DateFormatUtils.formatMonthWithYear(selectedDateLocal)
                textView.text = day.date.dayOfMonth.toString()


                val colorRes = if (day.date == selectedDateLocal) {
                    R.color.white
                } else {
                    R.color.black
                }
                val drawables = if (day.date == selectedDateLocal) {
                    R.drawable.highlight_oval
                } else {
                    R.drawable.highlight_white
                }
                textView.setBackgroundResource(drawables)
                textView.setTextColor(view.context.getColor(colorRes))
            }
        }

        val daysOfWeek = daysOfWeek()
        val titlesContainer = view.findViewById<ViewGroup>(R.id.titlesContainer)
        titlesContainer.children
            .map { it as TextView }
            .forEachIndexed { index, textView ->
                val dayOfWeek = daysOfWeek[index]
                val title = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                textView.text = title
            }

        binding.weekCalendarView.dayBinder = object : WeekDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: WeekDay) = container.bind(data)
        }

        val currentMonth = YearMonth.now()

        binding.weekCalendarView.setup(
            currentMonth.minusMonths(5).atStartOfMonth(),
            currentMonth.plusMonths(5).atEndOfMonth(),
            firstDayOfWeekFromLocale()
        )
        binding.weekCalendarView.scrollToDate(LocalDate.now())
    }

    private fun observer() {
        var count = 0

        habitViewModel.habitsLiveData.observe(viewLifecycleOwner) { habitsWithDailyActivities ->
            println("Selected Date:::count $count: $selectedDate + ")

            count++
            adapter.addList(selectedDateLocal!!, habitsWithDailyActivities)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}