package net.mmho.photomap2

import android.content.Context
import android.location.Address
import com.google.android.gms.maps.model.LatLng

internal object AddressUtil {

    private fun removePostalCode(source: String): String {
        return source.replaceFirst("〒[0-9¥-]*".toRegex(), "")
    }

    fun getTitle(a:Address?,c: Context) :String {
        if(a==null) return ""
        return buildString {
            val sep = c.getString(R.string.address_separator)
            if(a.adminArea!=null) append(a.adminArea,sep)
            if(a.subAdminArea!=null) append(a.subAdminArea,sep)
            if(a.locality!=null) append(a.locality)
            if(length==0) append(a.featureName)
        }
    }

    fun getDescription(address: Address): String {
        val description = StringBuilder()
        if (address.maxAddressLineIndex == 0) {
            description.append(address.getAddressLine(0))
        } else {
            var i = 1
            val l = address.maxAddressLineIndex
            while (i <= l) {
                description.append(address.getAddressLine(i)).append(" ")
                i++
            }
        }
        return removePostalCode(description.toString())
    }

    fun addressToLatLng(address: Address): LatLng {
        return LatLng(address.latitude, address.longitude)
    }


}
