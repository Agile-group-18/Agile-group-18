package org.grupp18.sortsmart.domain

import com.google.android.gms.maps.model.LatLng
import org.grupp18.sortsmart.data.model.RecyclingStationMarker
import org.grupp18.sortsmart.data.model.WasteCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class RouteOptimizerTest {

    private lateinit var optimizer: RouteOptimizer
    private val startLocation = LatLng(57.708870, 11.974560)

    private val catPlastic = WasteCategory(1, "Plastic", null)
    private val catPaper = WasteCategory(2, "Paper", null)
    private val catGlass = WasteCategory(3, "Glass", null)
    private val catBatteries = WasteCategory(4, "Batteries", null)

    @Before
    fun setup() {
        optimizer = RouteOptimizer()
    }

    private fun createMarker(id: String, lat: Double, lng: Double): RecyclingStationMarker {
        return RecyclingStationMarker(id, "FTI", LatLng(lat, lng), false)
    }

    @Test
    fun `single station covers everything, selects closest one`() {
        val basket = listOf(catPlastic, catPaper)

        val stationFar = RouteOptimizer.StationNode(
            createMarker("far", 57.75, 12.00),
            setOf(1, 2, 3)
        )
        val stationClose = RouteOptimizer.StationNode(
            createMarker("close", 57.71, 11.98),
            setOf(1, 2)
        )

        val result = optimizer.calculateOptimalRoute(
            startLocation,
            basket,
            listOf(stationFar, stationClose)
        )

        assertEquals(1, result.size)
        assertEquals("close", result[0].id)
    }

    @Test
    fun `requires multiple stations to cover basket`() {
        val basket = listOf(catPlastic, catPaper, catBatteries)

        val station1 = RouteOptimizer.StationNode(
            createMarker("station1", 57.71, 11.98),
            setOf(1, 2)
        )

        val station2 = RouteOptimizer.StationNode(
            createMarker("station2", 57.73, 11.99),
            setOf(4)
        )

        val station3 = RouteOptimizer.StationNode(
            createMarker("station3", 57.80, 12.00),
            setOf(3)
        )

        val result = optimizer.calculateOptimalRoute(
            startLocation,
            basket,
            listOf(station1, station2, station3)
        )

        assertEquals(2, result.size)
        assertEquals("station1", result[0].id)
        assertEquals("station2", result[1].id)
    }

    @Test
    fun `throws exception when items cannot be covered by available stations`() {
        val basket = listOf(catPlastic, catBatteries)

        val station = RouteOptimizer.StationNode(
            createMarker("station1", 57.71, 11.98),
            setOf(1)
        )

        assertThrows(RouteOptimizer.RouteNotFoundException::class.java) {
            optimizer.calculateOptimalRoute(
                startLocation,
                basket,
                listOf(station)
            )
        }
    }
}