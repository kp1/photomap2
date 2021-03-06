package net.mmho.photomap2

import android.provider.BaseColumns
import android.provider.MediaStore.Images.ImageColumns.*
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*

internal object QueryBuilder {
    fun createQuery(bounds: LatLngBounds): String {
        val start = bounds.southwest
        val end = bounds.northeast
        val latitude = "$LATITUDE between %f and %f and ".format(Locale.US,start.latitude,end.latitude)
        val longitude: String =
        if (start.longitude < end.longitude)
            "$LONGITUDE between %f and %f".format(Locale.US,start.longitude,end.longitude)
        else "($LONGITUDE between -180.0 and %f or $LONGITUDE between %f and 180.0)"
            .format(Locale.US,end.longitude,start.longitude)

        return latitude + longitude

    }

    fun createQuery(id: Long): String = "${BaseColumns._ID} is $id"

    fun createQuery(): String = "$LATITUDE not null and $LONGITUDE not null"

    fun createQueryNoLocation(): String = "$LATITUDE is null or $LONGITUDE is null"

    fun sortDateNewest(): String = "$DATE_TAKEN desc"

    fun sortDateOldest(): String = "$DATE_TAKEN asc"
}
