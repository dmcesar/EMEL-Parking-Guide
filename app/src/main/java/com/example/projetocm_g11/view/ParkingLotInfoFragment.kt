package com.example.projetocm_g11.view

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import butterknife.ButterKnife
import butterknife.OnClick

import com.example.projetocm_g11.R
import com.example.projetocm_g11.domain.data.ParkingLot
import com.example.projetocm_g11.domain.data.Type
import com.example.projetocm_g11.interfaces.OnNavigateToFragment
import kotlinx.android.synthetic.main.fragment_parking_lot_info.view.*
import java.text.SimpleDateFormat

class ParkingLotInfoFragment : Fragment() {

    private var listener: OnNavigateToFragment? = null
    private lateinit var parkingLot: ParkingLot

    @OnClick(R.id.button_go_map)
    fun onClickSetCourse() {

        val args = Bundle()

        args.putParcelable(EXTRA_PARKING_LOT, this.parkingLot)

        val navigationFragment = NavigationFragment()
        navigationFragment.arguments = args

        this.listener?.onNavigateToFragment(navigationFragment)
    }

    private fun registerListener() {

        this.listener = activity as OnNavigateToFragment
    }

    private fun unregisterListener() {

        this.listener = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_parking_lot_info, container, false)

        ButterKnife.bind(this, view)

        this.parkingLot = this.arguments?.getParcelable(EXTRA_PARKING_LOT) as ParkingLot

        val coordinates = "Coordinates: (${this.parkingLot.latitude}, ${this.parkingLot.longitude})"
        val type = if(this.parkingLot.type == Type.SURFACE) "Type: Surface" else "Type: Structure"
        val active = if(this.parkingLot.active) "Open" else "Closed"
        val lastUpdatedAt = "Last updated at: ${SimpleDateFormat("dd-MM-yyyy").format(this.parkingLot.lastUpdatedAt)}"

        view.park_capacity_bar.progress = this.parkingLot.capacityPercent
        view.park_capacity_text.text = this.parkingLot.capacityPercent.toString()
        view.park_occupancy_state.text = this.parkingLot.getState()
        view.park_name.text = this.parkingLot.name
        view.park_type.text = type
        view.park_availability.text = active
        view.park_coordinates.text = coordinates
        view.last_updated_at.text = lastUpdatedAt

        if(parkingLot.active) {
            view.park_availability.setTextColor(ContextCompat.getColor(activity as Context, R.color.forest_green))

        } else view.park_availability.setTextColor(ContextCompat.getColor(activity as Context, R.color.amber))

        return view
    }

    override fun onStart() {

        registerListener()

        super.onStart()
    }

    override fun onStop() {

        unregisterListener()

        super.onStop()
    }
}