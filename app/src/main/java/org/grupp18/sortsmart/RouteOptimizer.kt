package org.grupp18.sortsmart

import com.google.android.gms.maps.model.LatLng
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import org.grupp18.sortsmart.data.model.WasteCategory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RouteOptimizer {

    /**
     * Wrapper to associate a map marker with the categories that station supports.
     */
    data class StationNode(
        val marker: RecyclingStationMarker,
        val supportedCategoryIds: Set<Int>
    )

    class RouteNotFoundException(message: String) : Exception(message)

    /**
     * Calculates the optimal route to cover all waste categories.
     * * @param currentLocation The user's starting point.
     * @param basketCategories The items currently in the wastebasket.
     * @param availableStations All available stations with their supported categories.
     * @return An ordered list of stations. The last item is the destination, preceding items are waypoints.
     */
    fun calculateOptimalRoute(
        currentLocation: LatLng,
        basketCategories: List<WasteCategory>,
        availableStations: List<StationNode>
    ): List<RecyclingStationMarker> {
        val route = mutableListOf<RecyclingStationMarker>()

        val uncoveredCategories = basketCategories.map { it.id }.toMutableSet()
        var currentPos = currentLocation

        val usefulStations = availableStations.filter { station ->
            station.supportedCategoryIds.any { it in uncoveredCategories }
        }.toMutableList()

        while (uncoveredCategories.isNotEmpty()) {
            val bestStation = usefulStations.maxWithOrNull { a, b ->
                val coverageA = a.supportedCategoryIds.intersect(uncoveredCategories).size
                val coverageB = b.supportedCategoryIds.intersect(uncoveredCategories).size

                if (coverageA != coverageB) {
                    coverageA.compareTo(coverageB)
                } else {
                    val distA = calculateDistance(currentPos, a.marker.location)
                    val distB = calculateDistance(currentPos, b.marker.location)
                    distB.compareTo(distA)
                }
            }

            if (bestStation == null) {
                throw RouteNotFoundException("Could not find stations to cover all items in the basket.")
            }

            route.add(bestStation.marker)
            val solvedCategories = bestStation.supportedCategoryIds.intersect(uncoveredCategories)
            uncoveredCategories.removeAll(solvedCategories)
            usefulStations.remove(bestStation)

            currentPos = bestStation.marker.location
        }

        return route
    }

    /**
     * Calculates distance in meters between two LatLng points using the Haversine formula.
     * This avoids Android framework dependencies, making unit testing easier.
     */
    private fun calculateDistance(p1: LatLng, p2: LatLng): Double {
        val r = 6371e3
        val phi1 = Math.toRadians(p1.latitude)
        val phi2 = Math.toRadians(p2.latitude)
        val deltaPhi = Math.toRadians(p2.latitude - p1.latitude)
        val deltaLambda = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(deltaPhi / 2) * sin(deltaPhi / 2) +
                cos(phi1) * cos(phi2) *
                sin(deltaLambda / 2) * sin(deltaLambda / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}