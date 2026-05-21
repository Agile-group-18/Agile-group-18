package org.grupp18.sortsmart

import com.google.android.gms.maps.model.LatLng
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import org.grupp18.sortsmart.data.model.WasteCategory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class RouteOptimizer {

    data class StationNode(
        val marker: RecyclingStationMarker,
        val supportedCategoryIds: Set<Int>
    )

    enum class OptimizationStrategy {
        FEWEST_STOPS,
        SHORTEST_DISTANCE
    }

    class RouteNotFoundException(message: String) : Exception(message)

    fun calculateOptimalRoute(
        currentLocation: LatLng,
        basketCategories: List<WasteCategory>,
        availableStations: List<StationNode>,
        strategy: OptimizationStrategy = OptimizationStrategy.FEWEST_STOPS
    ): List<RecyclingStationMarker> {
        val route = mutableListOf<RecyclingStationMarker>()

        val uncoveredCategories = basketCategories.map { it.id }.toMutableSet()
        var currentPos = currentLocation

        val remainingStations = availableStations.toMutableList()

        while (uncoveredCategories.isNotEmpty()) {

            val helpfulStations = remainingStations.filter { station ->
                station.supportedCategoryIds.any { it in uncoveredCategories }
            }

            if (helpfulStations.isEmpty()) {
                throw RouteNotFoundException("Could not find stations to cover all items in the basket.")
            }

            val bestStation = helpfulStations.maxWithOrNull { a, b ->
                val coverageA = a.supportedCategoryIds.intersect(uncoveredCategories).size
                val coverageB = b.supportedCategoryIds.intersect(uncoveredCategories).size
                val distA = calculateDistance(currentPos, a.marker.location)
                val distB = calculateDistance(currentPos, b.marker.location)

                if (strategy == OptimizationStrategy.FEWEST_STOPS) {
                    if (coverageA != coverageB) {
                        coverageA.compareTo(coverageB)
                    } else {
                        distB.compareTo(distA)
                    }
                } else {
                    if (distA != distB) {
                        distB.compareTo(distA)
                    } else {
                        coverageA.compareTo(coverageB)
                    }
                }
            } ?: throw RouteNotFoundException("Routing error")

            route.add(bestStation.marker)
            val solvedCategories = bestStation.supportedCategoryIds.intersect(uncoveredCategories)
            uncoveredCategories.removeAll(solvedCategories)

            remainingStations.remove(bestStation)

            currentPos = bestStation.marker.location
        }

        return route
    }

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