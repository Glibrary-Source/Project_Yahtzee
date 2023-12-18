package com.twproject.projectyahtzee.view.main

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.findNavController
import com.twproject.banyeomiji.vbutility.BackPressCallBackManager
import com.twproject.projectyahtzee.vbutils.ButtonAnimation
import com.twproject.projectyahtzee.databinding.FragmentMenuBinding
import com.twproject.projectyahtzee.vbutils.onThrottleClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentMenu : Fragment() {

    private lateinit var binding: FragmentMenuBinding
    private lateinit var callback: OnBackPressedCallback
    private lateinit var mContext: Context
    private lateinit var activity: FragmentActivity
    private val animScale = 0.97F

    override fun onAttach(context: Context) {
        super.onAttach(context)

        mContext = context
        activity = context as MainActivity

        callback = BackPressCallBackManager.setBackPressCallBack(activity, context)
        activity.onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(layoutInflater)

        binding.linearCreateRoom.onThrottleClick { navigateToDestination(FragmentCreateRoom(),it) }
        binding.linearRoomList.onThrottleClick { navigateToDestination(FragmentRoomList(),it) }
        binding.linearMyProfile.onThrottleClick { navigateToDestination(FragmentMyProfile(),it) }

        return binding.root
    }

    private fun navigateToDestination(destinationFragment: Fragment, view: View) {
        ButtonAnimation().startAnimation(view, animScale)
        val action = when (destinationFragment) {
            is FragmentCreateRoom -> FragmentMenuDirections.actionFragmentMenuToFragmentCreateRoom()
            is FragmentRoomList -> FragmentMenuDirections.actionFragmentMenuToFragmentRoomList()
            is FragmentMyProfile -> FragmentMenuDirections.actionFragmentMenuToFragmentMyProfile()
            else -> throw IllegalArgumentException("Unknown destination fragment")
        }
        CoroutineScope(Main).launch{
            delay(300)
            findNavController().navigate(action)
        }
    }
}