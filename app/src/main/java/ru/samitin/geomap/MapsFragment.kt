package ru.samitin.geomap

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import ru.samitin.geomap.databinding.FragmentMapsBinding
import java.io.IOException

class MapsFragment : Fragment() {

    private var _binding : FragmentMapsBinding?= null
    private val binding get() = _binding!!
    private lateinit var  map : GoogleMap
    private val markers :ArrayList<Marker> = arrayListOf()
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        map = googleMap
        val initialPlace = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(initialPlace).title(getString(R.string.marker_start)))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(initialPlace))
        googleMap.setOnMapLongClickListener { latLng ->
            getAddressAsync(latLng)
            addMarkerToArray(latLng)
            drawLine()
        }
    }

    private fun addMarkerToArray(location: LatLng) {
        val marker = setMarker(location,markers.size.toString(),R.drawable.ic_map_pin)
        if (marker != null) {
            markers.add(marker)
        }
    }

    private fun setMarker(location: LatLng, searchText: String, resourceId: Int): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(location)
                .title(searchText)
                .icon(BitmapDescriptorFactory.fromResource(resourceId))
        )
    }
    private fun drawLine() {
        val last: Int = markers.size - 1
        if (last >= 1) {
            val previous: LatLng = markers[last - 1].position
            val current: LatLng = markers[last].position
            map.addPolyline(
                PolylineOptions()
                    .add(previous, current)
                    .color(Color.RED)
                    .width(5f)
            )
        }
    }

    private fun getAddressAsync(location: LatLng) {
        context?.let {
            val geocoder = Geocoder(it)
            Thread{
                try {
                    val addresses = geocoder.getFromLocation(location.latitude,location.longitude,1)
                    binding.apply {
                        textAddress.post { textAddress.text = addresses?.get(0)?.getAddressLine(0)  }
                    }
                }catch (e:IOException){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMapsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        initSearchByAddress()
    }

    private fun initSearchByAddress() {
        binding.buttonSearch.setOnClickListener {
            val geoCoder = Geocoder(it.context)
            val searchText = binding.searchAddress.text.toString()
            Thread {
                try {
                    val addresses = geoCoder.getFromLocationName(searchText, 1)
                    if (addresses != null) {
                        if (addresses.size > 0) {
                            goToAddress(addresses, it, searchText)
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
    private fun goToAddress(
        addresses: MutableList<Address>,
        view: View,
        searchText: String
    ) {
        val location = LatLng(
            addresses.first().latitude,
            addresses.first().longitude
        )
        view.post {
            setMarker(location, searchText, R.drawable.ic_map_marker)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    location,
                    15f
                )
            )
        }
    }

    companion object{
        fun newInstance() = MapsFragment()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}