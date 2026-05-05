package com.example.demo_gis_wearos.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import org.mapsforge.map.android.view.MapView
import org.mapsforge.map.layer.cache.TileCache
import org.mapsforge.map.android.graphics.AndroidGraphicFactory
import org.mapsforge.map.layer.renderer.TileRendererLayer
import org.mapsforge.map.reader.MapFile
import org.mapsforge.core.model.MapPosition
import org.mapsforge.core.model.LatLong
import org.mapsforge.map.rendertheme.InternalRenderTheme
import java.io.File

import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class MainActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidGraphicFactory.createInstance(application)

        mapView = MapView(this)


        val layout = android.widget.FrameLayout(this)

// Añades el mapa
        layout.addView(mapView)

        // FUNCIÓN para crear botón redondo
        fun createCircleButton(text: String): android.widget.TextView {
            val button = android.widget.TextView(this)
            button.text = text
            button.textSize = 18f
            button.gravity = android.view.Gravity.CENTER

            val size = 90

            val params = android.widget.FrameLayout.LayoutParams(size, size)

            // Fondo redondo blanco
            val shape = android.graphics.drawable.GradientDrawable()
            shape.shape = android.graphics.drawable.GradientDrawable.OVAL
            shape.setColor(android.graphics.Color.WHITE)

            button.background = shape

            // Sombra (efecto material)
            button.elevation = 8f

            return button
        }


// BOTÓN +
        val zoomIn = createCircleButton("+")

        val paramsIn = android.widget.FrameLayout.LayoutParams(90, 90)
        paramsIn.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
        paramsIn.bottomMargin = 40
        paramsIn.marginEnd = 60

        layout.addView(zoomIn, paramsIn)


// BOTÓN -
        val zoomOut = createCircleButton("−")

        val paramsOut = android.widget.FrameLayout.LayoutParams(90, 90)
        paramsOut.gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
        paramsOut.bottomMargin = 40
        paramsOut.marginStart = 60

        layout.addView(zoomOut, paramsOut)

// Activar botones
        zoomIn.setOnClickListener {
            val zoom = mapView.model.mapViewPosition.zoomLevel
            mapView.model.mapViewPosition.zoomLevel = (zoom + 1).toByte()
        }

        zoomOut.setOnClickListener {
            val zoom = mapView.model.mapViewPosition.zoomLevel
            mapView.model.mapViewPosition.zoomLevel = (zoom - 1).toByte()
        }

// Mostrar layout
        setContentView(layout)
        mapView.setBuiltInZoomControls(false)
        mapView.mapScaleBar.isVisible = false

        val mapFileName = "madrid.map"
        val mapFilePath = File(filesDir, mapFileName)

        if (!mapFilePath.exists()) {
            assets.open(mapFileName).use { input ->
                mapFilePath.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        val mapFile = MapFile(mapFilePath)

        val tileCache = org.mapsforge.map.android.util.AndroidUtil.createTileCache(
            this,
            "mapcache",
            mapView.model.displayModel.tileSize,
            1f,
            mapView.model.frameBufferModel.overdrawFactor
        )

        val rendererLayer = TileRendererLayer(
            tileCache,
            mapFile,
            mapView.model.mapViewPosition,
            AndroidGraphicFactory.INSTANCE
        )

        rendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT)

        mapView.layerManager.layers.add(rendererLayer)

        // ICONO 1
        val inputStream1 = assets.open("unit-app6-128.png")
        val drawable1 = android.graphics.drawable.Drawable.createFromStream(inputStream1, null)

        if (drawable1 == null) {
            throw RuntimeException("Drawable1 es null")
        }

        val bitmap1 = AndroidGraphicFactory.convertToBitmap(drawable1)

        val marker1 = org.mapsforge.map.layer.overlay.Marker(
            LatLong(40.455, -3.475), // posición 1
            bitmap1,
            0,
            -bitmap1.height / 2
        )

        mapView.layerManager.layers.add(marker1)


// ICONO 2
        val inputStream2 = assets.open("unit2-app6-128.png")
        val drawable2 = android.graphics.drawable.Drawable.createFromStream(inputStream2, null)

        if (drawable2 == null) {
            throw RuntimeException("Drawable2 es null")
        }

        val bitmap2 = AndroidGraphicFactory.convertToBitmap(drawable2)

        val marker2 = org.mapsforge.map.layer.overlay.Marker(
            LatLong(40.458, -3.472), // posición 2 (distinta)
            bitmap2,
            0,
            -bitmap2.height / 2
        )

        mapView.layerManager.layers.add(marker2)

        mapView.model.mapViewPosition.mapPosition =
            MapPosition(LatLong(40.455, -3.475), 14.toByte()) // Madrid


        //UBICACION
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {

                val lat = location.latitude
                val lon = location.longitude

                val latLong = LatLong(lat, lon)

                // ICONO DE TU UBICACIÓN
                val inputStream = assets.open("ubicacion-256.png")
                val drawable = android.graphics.drawable.Drawable.createFromStream(inputStream, null)

                if (drawable != null) {
                    val bitmap = AndroidGraphicFactory.convertToBitmap(drawable)

                    val marker = org.mapsforge.map.layer.overlay.Marker(
                        latLong,
                        bitmap,
                        0,
                        -bitmap.height / 2
                    )

                    mapView.layerManager.layers.add(marker)
                }

                // CENTRAR MAPA EN TU POSICIÓN
                mapView.model.mapViewPosition.mapPosition =
                    MapPosition(latLong, 14.toByte())
            }
        }
    }


}