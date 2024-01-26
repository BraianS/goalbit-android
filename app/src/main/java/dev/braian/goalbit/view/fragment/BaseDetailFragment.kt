package dev.braian.goalbit.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.FragmentCommonDetailBinding

abstract class BaseDetailFragment : Fragment() {

    private var _binding: FragmentCommonDetailBinding? = null
    private val binding get() = _binding!!

    protected abstract fun getNameTitle(): String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentCommonDetailBinding.inflate(inflater, container, false)

        binding.textName.text = getNameTitle()
        binding.textFinished.text = ""
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}