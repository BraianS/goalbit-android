package dev.braian.goalbit.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import dev.braian.goalbit.view.adapter.HabitHistoricAdapter
import dev.braian.goalbit.databinding.FragmentHistoricBinding
import dev.braian.goalbit.model.Habit
import dev.braian.goalbit.view.viewholder.HabitViewModel


class HistoricFragment : Fragment() {

    private var _binding: FragmentHistoricBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var habitViewModel: HabitViewModel
    lateinit var habits: MutableList<Habit>
    private val adapter = HabitHistoricAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        habitViewModel = HabitViewModel()
    }

    override fun onResume() {
        super.onResume()
        habitViewModel.getHabitsWithYearlyOccurrencies(auth.currentUser!!.uid)

        habitViewModel.errorMessageLiveData.observe(this) {
            it?.let { errorMessage ->
                // Show a Toast with the error message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        habits = mutableListOf()

        _binding = FragmentHistoricBinding.inflate(inflater, container, false)

        binding.recycleViewhabits.layoutManager = LinearLayoutManager(context)
        binding.recycleViewhabits.adapter = adapter

        auth = FirebaseAuth.getInstance()
        observer()
        return binding.root
    }

    private fun observer() {
        habitViewModel.habitsWithYearStreak.observe(viewLifecycleOwner) { habits ->
            adapter.addList(habits!!)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}