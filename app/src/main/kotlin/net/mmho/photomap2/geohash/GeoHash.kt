package net.mmho.photomap2.geohash

import android.os.Parcel
import android.os.Parcelable

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

import java.util.Locale

@SuppressWarnings("unused")
class GeoHash : Parcelable {

    var long: Long = 0
        internal set
    var significantBits: Int = 0
        internal set

    internal class Divider(var upper: Double) {
        var lower = 0.toDouble()

        init {
            lower = -upper
        }

        fun middle(): Double {
            return (upper + lower) / 2
        }
    }

    private constructor()

    val binaryString: String
        get() = java.lang.Long.toBinaryString(long).substring(0, significantBits)

    fun toBase32(): String {
        val b = StringBuilder()
        if (significantBits % BASE32_BITS != 0) {
            throw IllegalArgumentException()
        }
        val maskBits = 0L.inv().ushr(MAX_SIGNIFICANT_BITS - BASE32_BITS)
        for (i in 0 until significantBits / 5) {
            b.append(BASE32[(long.ushr(MAX_SIGNIFICANT_BITS - (i + 1) * BASE32_BITS) and maskBits).toInt()])
        }
        return b.toString()
    }

    fun extend(ext: GeoHash): GeoHash {
        var significant = Math.min(significantBits, ext.significantBits)
        var xorBit = long xor ext.long and 0L.inv().ushr(significant).inv()
        while (xorBit != 0L) {
            significant--
            xorBit = long xor ext.long and 0L.inv().ushr(significant).inv()
        }
        val hash = GeoHash()
        hash.long = long and 0L.inv().ushr(significant).inv()
        hash.significantBits = significant
        return hash
    }

    fun within(base: GeoHash): Boolean {
        return significantBits >= base.significantBits && within(base, base.significantBits)
    }

    fun within(base: GeoHash, significant: Int): Boolean {
        return long xor base.long and 0L.inv().ushr(significant).inv() == 0L
    }

    private val area: Array<Divider>
        get() {
            val dividers = arrayOf(Divider(180.0), Divider(90.0))
            var checkBit = 0L.inv().ushr(1).inv()
            for (i in 0 until significantBits) {
                val div = dividers[i % 2]
                if (long and checkBit != 0L) {
                    div.lower = div.middle()
                } else {
                    div.upper = div.middle()
                }
                checkBit = checkBit ushr 1
            }
            return dividers
        }

    val bounds: LatLngBounds
        get() {
            val dividers = area
            val northeast = LatLng(dividers[1].upper, dividers[0].upper)
            val southwest = LatLng(dividers[1].lower, dividers[0].lower)
            return LatLngBounds(southwest, northeast)
        }

    val center: LatLng
        get() {
            val dividers = area
            return LatLng(dividers[1].middle(), dividers[0].middle())
        }

    fun equals(other: GeoHash): Boolean {
        return long == other.long && significantBits == other.significantBits
    }

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeLong(long)
        dest?.writeInt(significantBits)
    }

    private constructor(source: Parcel) {
        long = source.readLong()
        significantBits = source.readInt()
    }

    companion object {

        private const val MAX_SIGNIFICANT_BITS = 64
        private const val BASE32_BITS = 5
        @Suppress("SpellCheckingInspection")
        private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

        fun createWithCharacterCount(latLng: LatLng, length: Int): GeoHash {
            return create(latLng, length * BASE32_BITS)
        }

        fun create(latlng: LatLng, significant: Int): GeoHash {
            return create(latlng.latitude, latlng.longitude, significant)
        }

        fun createFromLong(bit: Long, significant: Int): GeoHash {
            val hash = GeoHash()
            hash.long = bit
            hash.significantBits = significant
            return hash
        }

        fun createFromHash(hash: String): GeoHash {
            val geoHash = GeoHash()
            geoHash.significantBits = hash.length * BASE32_BITS
            geoHash.long = 0L
            if (geoHash.significantBits > MAX_SIGNIFICANT_BITS) {
                throw IllegalArgumentException("hash is too long.")
            }
            var offset = MAX_SIGNIFICANT_BITS - BASE32_BITS
            for (c in hash.toLowerCase(Locale.US).toCharArray()) {
                val bit = BASE32.indexOf(c).toLong()
                if (bit < 0) throw IllegalArgumentException("hash string is invalid")
                geoHash.long = geoHash.long or (bit shl offset)
                offset -= MAX_SIGNIFICANT_BITS
            }
            return geoHash
        }

        fun create(latitude: Double, longitude: Double, significant: Int): GeoHash {

            if (significant > MAX_SIGNIFICANT_BITS) {
                throw IllegalArgumentException("significant bit must under $MAX_SIGNIFICANT_BITS")
            }

            val hash = GeoHash()
            hash.significantBits = significant
            var bit = 0L.inv().ushr(1).inv()       // MSB = 1;
            hash.long = 0

            val p = doubleArrayOf(longitude, latitude)
            val dividers = arrayOf(Divider(180.0), Divider(90.0))
            for (i in 0 until significant) {
                val index = i % 2
                val div = dividers[index]
                val middle = div.middle()
                if (p[index] > middle) {
                    hash.long = hash.long or bit
                    div.lower = middle
                } else {
                    div.upper = middle
                }
                bit = bit ushr 1
            }
            return hash
        }

        @JvmField val CREATOR = object : Parcelable.Creator<GeoHash> {
            override fun createFromParcel(source: Parcel): GeoHash = GeoHash(source)
            override fun newArray(size: Int): Array<GeoHash?> = arrayOfNulls(size)
        }
    }
}
