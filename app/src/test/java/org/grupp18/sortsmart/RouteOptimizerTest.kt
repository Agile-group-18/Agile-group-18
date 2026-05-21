package org.grupp18.sortsmart

import com.google.android.gms.maps.model.LatLng
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import org.grupp18.sortsmart.data.model.WasteCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteOptimizerTest {

    private val optimizer = RouteOptimizer()
    private val plastic = WasteCategory(id = 1, name = "Plastic", imageUrl = null)
    private val paper = WasteCategory(id = 2, name = "Paper", imageUrl = null)
    private val glass = WasteCategory(id = 3, name = "Glass", imageUrl = null)

    @Test
    fun `Fewest Stops strategy picks one far station over two close stations even if total distance is longer`() {
        // Arrange
        val startLocation = LatLng(0.0, 0.0)

        // Station A: Very close to the user, accepts Paper
        val markerA = RecyclingStationMarker("1", "Station A", LatLng(0.01, 0.00), false)
        val stationA = RouteOptimizer.StationNode(markerA, setOf(paper.id))

        // Station B: Very close to Station A, accepts Plastic
        val markerB = RecyclingStationMarker("2", "Station B", LatLng(0.02, 0.00), false)
        val stationB = RouteOptimizer.StationNode(markerB, setOf(plastic.id))

        // Station C: Very far away, but accepts BOTH
        val markerC = RecyclingStationMarker("3", "Station C", LatLng(0.10, 0.00), false)
        val stationC = RouteOptimizer.StationNode(markerC, setOf(plastic.id, paper.id))

        val basket = listOf(plastic, paper)
        val availableStations = listOf(stationA, stationB, stationC)

        // Act - Using FEWEST_STOPS
        val route = optimizer.calculateOptimalRoute(
            startLocation, basket, availableStations, RouteOptimizer.OptimizationStrategy.FEWEST_STOPS
        )

        // Assert
        // Walking Start -> A -> B is a short trip. Walking Start -> C is a long trip.
        // Because we are in FEWEST_STOPS mode, it MUST choose the long trip to C!
        assertEquals(1, route.size)
        assertEquals("Station C", route.first().stationType)
    }

    @Test
    fun `Shortest Distance strategy prioritizes distance over coverage`() {
        // Arrange
        val startLocation = LatLng(0.0, 0.0)

        // Station A: Close, but only takes plastic
        val closeMarker = RecyclingStationMarker("1", "Close Station", LatLng(0.01, 0.01), false)
        val closeStation = RouteOptimizer.StationNode(closeMarker, setOf(plastic.id))

        // Station B: Far, but takes BOTH
        val farMarker = RecyclingStationMarker("2", "Super Station", LatLng(1.0, 1.0), false)
        val farStation = RouteOptimizer.StationNode(farMarker, setOf(plastic.id, paper.id))

        val basket = listOf(plastic, paper)
        val availableStations = listOf(closeStation, farStation)

        // Act - Using SHORTEST_DISTANCE
        val route = optimizer.calculateOptimalRoute(
            startLocation, basket, availableStations, RouteOptimizer.OptimizationStrategy.SHORTEST_DISTANCE
        )

        // Assert: It should stop at the Close Station first, THEN travel to the Super station for the remaining paper
        assertEquals(2, route.size)
        assertEquals("Close Station", route[0].stationType)
        assertEquals("Super Station", route[1].stationType)
    }

    @Test
    fun `Bug Fix - completely ignores useless closest stations`() {
        // Arrange
        val startLocation = LatLng(0.0, 0.0)

        // Station A: INCREDIBLY close, but takes Glass (which we don't have!)
        val uselessMarker = RecyclingStationMarker("1", "Useless Station", LatLng(0.001, 0.001), false)
        val uselessStation = RouteOptimizer.StationNode(uselessMarker, setOf(glass.id))

        // Station B: Farther away, but actually takes our Plastic
        val usefulMarker = RecyclingStationMarker("2", "Useful Station", LatLng(0.1, 0.1), false)
        val usefulStation = RouteOptimizer.StationNode(usefulMarker, setOf(plastic.id))

        val basket = listOf(plastic) // We ONLY have plastic
        val availableStations = listOf(uselessStation, usefulStation)

        // Act
        val route = optimizer.calculateOptimalRoute(
            startLocation, basket, availableStations, RouteOptimizer.OptimizationStrategy.SHORTEST_DISTANCE
        )

        // Assert: It should skip the useless station completely and only give us a 1-stop route
        assertEquals(1, route.size)
        assertEquals("Useful Station", route.first().stationType)
    }
}