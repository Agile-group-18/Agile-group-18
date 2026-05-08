package org.grupp18.sortsmart.ui.map

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import org.grupp18.sortsmart.R

class StationClusterRenderer(
    private val context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<StationClusterItem>
) : DefaultClusterRenderer<StationClusterItem>(context, map, clusterManager) {

    // Rasterized once at display-density-correct size, then cached forever.
    // Uses toBitmap() which handles VectorDrawable — fromResource() only works
    // with raster PNGs and will crash with "must be a Bitmap" on vector XMLs.
    private val sizePx: Int by lazy {
        (36 * context.resources.displayMetrics.density).toInt()
    }

    private val iconFull: BitmapDescriptor by lazy {
        vectorToBitmapDescriptor(R.drawable.ic_station_full)
    }

    private val iconFunctional: BitmapDescriptor by lazy {
        vectorToBitmapDescriptor(R.drawable.ic_station_functional)
    }

    private fun iconFor(item: StationClusterItem) =
        if (item.problemReport) iconFull else iconFunctional

    override fun onBeforeClusterItemRendered(
        item: StationClusterItem,
        markerOptions: MarkerOptions
    ) {
        markerOptions.icon(iconFor(item))
    }

    // onClusterItemUpdated replaces onClusterItemRendered for re-renders
    // after initial placement (e.g. after sync updates hasProblemReport)
    override fun onClusterItemUpdated(item: StationClusterItem, marker: Marker) {
        marker.setIcon(iconFor(item))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<StationClusterItem>): Boolean {
        return cluster.size >= 4
    }

    private fun vectorToBitmapDescriptor(@DrawableRes resId: Int): BitmapDescriptor {
        val drawable = checkNotNull(ContextCompat.getDrawable(context, resId)) {
            "Drawable resource $resId not found"
        }
        val bitmap: Bitmap = drawable.toBitmap(
            width = sizePx,
            height = sizePx,
            config = Bitmap.Config.ARGB_8888
        )
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}