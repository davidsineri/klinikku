package com.klinikku.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "doctors")
data class DoctorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val polyclinic: String,
    val specialtyTitle: String,
    val strNumber: String,
    val experienceYears: Int,
    val consultationFee: Long,
    val rating: Double,
    val reviewCount: Int,
    val roomNumber: String,
    val availableDays: String, // e.g. "Senin,Selasa,Rabu,Kamis,Jumat,Sabtu"
    val timeSlots: String, // e.g. "08:30,09:30,10:30,13:30,15:00,16:30"
    val dailyQuota: Int,
    val acceptsBpjs: Boolean,
    val bio: String,
    val avatarColorHex: Long
)

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val nik: String,
    val medicalRecordNumber: String,
    val birthDate: String,
    val gender: String,
    val bloodType: String,
    val phone: String,
    val paymentMethodDefault: String,
    val bpjsOrInsuranceNumber: String,
    val allergies: String,
    val relationship: String
)

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingCode: String,
    val queueNumber: String,
    val queueOrder: Int,
    val patientId: Int,
    val patientName: String,
    val patientRm: String,
    val doctorId: Int,
    val doctorName: String,
    val polyclinic: String,
    val roomNumber: String,
    val appointmentDate: String, // YYYY-MM-DD
    val appointmentTime: String, // HH:mm
    val bookingType: String, // "Konsultasi Dokter" | "Medical Check-Up"
    val packageName: String,
    val symptomsOrNotes: String,
    val paymentType: String, // "Umum / Mandiri" | "BPJS Kesehatan" | "Asuransi Swasta"
    val totalCost: Long,
    val status: String, // "TERJADWAL" | "CHECK_IN" | "DIPERIKSA" | "SELESAI" | "DIBATALKAN"
    val estimatedTurnTime: String,
    val clinicalSummary: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mcu_packages")
data class McuPackageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val price: Long,
    val originalPrice: Long,
    val durationMinutes: Int,
    val preparationNote: String,
    val includedTests: String, // pipe-separated
    val assignedDoctorId: Int
)

@Entity(tableName = "polyclinic_queues")
data class PolyclinicQueueEntity(
    @PrimaryKey val polyclinic: String,
    val prefixCode: String,
    val currentNumber: Int,
    val totalIssuedToday: Int,
    val roomNumber: String,
    val activeDoctorName: String,
    val avgMinutesPerPatient: Int
)

data class DateOption(
    val isoDate: String, // YYYY-MM-DD
    val dayName: String, // Senin, Selasa, ...
    val dayNumber: String, // 04
    val monthShort: String, // Okt
    val fullDisplay: String // Minggu, 04 Okt 2026
)

object ClinicFormatters {
    private val idLocale = Locale("id", "ID")

    fun formatRupiah(amount: Long): String {
        if (amount == 0L) return "Rp 0 (Ditanggung)"
        val formatter = NumberFormat.getNumberInstance(idLocale)
        return "Rp ${formatter.format(amount)}"
    }

    fun getTodayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getOffsetDateIso(daysOffset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, daysOffset)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun getUpcomingDateOptions(daysCount: Int = 7): List<DateOption> {
        val dayNames = mapOf(
            Calendar.SUNDAY to "Minggu",
            Calendar.MONDAY to "Senin",
            Calendar.TUESDAY to "Selasa",
            Calendar.WEDNESDAY to "Rabu",
            Calendar.THURSDAY to "Kamis",
            Calendar.FRIDAY to "Jumat",
            Calendar.SATURDAY to "Sabtu"
        )
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
            "Jul", "Ags", "Sep", "Okt", "Nov", "Des"
        )
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val result = mutableListOf<DateOption>()
        for (i in 0 until daysCount) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i)
            val iso = isoFormat.format(cal.time)
            val dName = dayNames[cal.get(Calendar.DAY_OF_WEEK)] ?: "Senin"
            val dNum = String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH))
            val mShort = monthNames[cal.get(Calendar.MONTH)]
            val year = cal.get(Calendar.YEAR)
            val labelPrefix = when (i) {
                0 -> "Hari Ini • $dName"
                1 -> "Besok • $dName"
                else -> dName
            }
            result.add(
                DateOption(
                    isoDate = iso,
                    dayName = dName,
                    dayNumber = dNum,
                    monthShort = mShort,
                    fullDisplay = "$labelPrefix, $dNum $mShort $year"
                )
            )
        }
        return result
    }

    fun formatIsoDateReadable(isoDate: String): String {
        val options = getUpcomingDateOptions(14)
        options.find { it.isoDate == isoDate }?.let { return it.fullDisplay }
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsed = parser.parse(isoDate) ?: return isoDate
            val outFormat = SimpleDateFormat("dd MMM yyyy", idLocale)
            outFormat.format(parsed)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun polyclinicPrefix(polyclinic: String): String = when {
        polyclinic.contains("Umum", ignoreCase = true) -> "A"
        polyclinic.contains("Gigi", ignoreCase = true) -> "B"
        polyclinic.contains("Anak", ignoreCase = true) -> "C"
        polyclinic.contains("Penyakit Dalam", ignoreCase = true) -> "D"
        polyclinic.contains("Kulit", ignoreCase = true) -> "E"
        polyclinic.contains("Mata", ignoreCase = true) -> "F"
        polyclinic.contains("THT", ignoreCase = true) -> "G"
        polyclinic.contains("Kandungan", ignoreCase = true) -> "H"
        else -> "M"
    }
}
