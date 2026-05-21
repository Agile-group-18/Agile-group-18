package org.grupp18.sortsmart.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import com.google.android.gms.maps.model.LatLng

object MapNavigationUtil {

    /**
     * Launches Google Maps with a calculated route of recycling stations.
     * @param context The Android context.
     * @param route The ordered list of stations to visit.
     * @param origin The starting point of the route.
     */
    fun launchGoogleMapsRoute(context: Context, route: List<RecyclingStationMarker>, origin: LatLng) {
        if (route.isEmpty()) {
            Toast.makeText(context, "No route available to navigate", Toast.LENGTH_SHORT).show()
            return
        }

        val destination = route.last().location
        val waypoints = route.dropLast(1).map { it.location }

        val uriBuilder = StringBuilder("https://www.google.com/maps/dir/?api=1")

        uriBuilder.append("&origin=${origin.latitude},${origin.longitude}")

        uriBuilder.append("&destination=${destination.latitude},${destination.longitude}")

        if (waypoints.isNotEmpty()) {
            val waypointsString = waypoints.joinToString(separator = "|") {
                "${it.latitude},${it.longitude}"
            }
            uriBuilder.append("&waypoints=$waypointsString")
        }

        uriBuilder.append("&waypoints_optimized=false")

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriBuilder.toString())).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Google Maps is not installed.", Toast.LENGTH_LONG).show()
        }
    }
}