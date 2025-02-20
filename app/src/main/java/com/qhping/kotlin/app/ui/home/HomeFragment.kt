package com.qhping.kotlin.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.qhping.kotlin.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val repository = HomeRepository(requireActivity().application)
        val factory = HomeViewModelFactory(requireActivity().application, repository)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel.previewUseCase.observe(viewLifecycleOwner) { preview ->
            if (preview != null) {
                val parent = binding.preview.parent as? ViewGroup
                parent?.removeView(binding.preview)
                parent?.addView(preview)
            }
        }
        homeViewModel.startCamera(viewLifecycleOwner,requireContext(),binding.preview.surfaceProvider)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}