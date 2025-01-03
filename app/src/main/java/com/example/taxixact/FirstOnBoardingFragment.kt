package com.example.taxixact

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.viewpager2.widget.ViewPager2


class FirstOnBoardingFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_on_boarding, container, false)

        val next = view.findViewById<TextView>(R.id.first_next)
        val viewPager = activity?.findViewById<ViewPager2>(R.id.view_pager_onboarding)

        next.setOnClickListener {
            viewPager?.currentItem = 1
        }
        return view
    }
}