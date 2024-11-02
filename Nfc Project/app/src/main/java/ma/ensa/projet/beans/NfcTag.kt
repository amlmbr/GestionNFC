package ma.ensa.projet.beans

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "nfc_tags")
@TypeConverters(Converters::class)
data class NFCTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uuid: String,
    val type: NFCDataType,
    val content: String,
    val timestamp: Date
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        uuid = parcel.readString() ?: "",
        type = parcel.readSerializable() as NFCDataType,
        content = parcel.readString() ?: "",
        timestamp = Date(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(uuid)
        parcel.writeSerializable(type)
        parcel.writeString(content)
        parcel.writeLong(timestamp.time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NFCTag> {
        override fun createFromParcel(parcel: Parcel): NFCTag {
            return NFCTag(parcel)
        }

        override fun newArray(size: Int): Array<NFCTag?> {
            return arrayOfNulls(size)
        }
    }
}

// Updated NFCDataType enum with PDF instead of CUSTOM
enum class NFCDataType {
    TEXT, URL, CONTACT, PDF
}

// Type converters for handling Date and NFCDataType conversions
object Converters {
    @JvmStatic
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @JvmStatic
    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @JvmStatic
    @androidx.room.TypeConverter
    fun fromDataType(value: NFCDataType): String {
        return value.name
    }

    @JvmStatic
    @androidx.room.TypeConverter
    fun toDataType(value: String): NFCDataType {
        return NFCDataType.valueOf(value)
    }
}
