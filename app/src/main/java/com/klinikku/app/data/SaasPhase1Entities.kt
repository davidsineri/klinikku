package com.klinikku.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class UserRole(val code: String, val label: String, val description: String) {
    PATIENT("PATIENT", "Pasien (Mobile)", "Booking slot, pantau antrean, persetujuan PDP & hak hapus data"),
    STAFF_DOCTOR("STAFF_DOCTOR", "Dokter & Staf (Tablet)", "Atur durasi slot, jam istirahat, panggil antrean & sapu No-Show"),
    SUPER_ADMIN("SUPER_ADMIN", "Super Admin (SaaS)", "Kelola multi-klinik (tenant), langganan SaaS & audit log UU PDP")
}

enum class IndonesianZone(val code: String, val zoneId: String, val utcOffsetHours: Int) {
    UTC("UTC", "UTC", 0),
    WIB("WIB", "Asia/Jakarta", 7),
    WITA("WITA", "Asia/Makassar", 8),
    WIT("WIT", "Asia/Jayapura", 9)
}

@Entity(tableName = "saas_tenants")
data class TenantClinicEntity(
    @PrimaryKey val tenantId: String,
    val clinicName: String,
    val clinicType: String, // Umum, Gigi, Kecantikan, Praktik Mandiri
    val city: String,
    val defaultZoneCode: String, // WIB, WITA, WIT
    val subscriptionPlan: String, // Starter, Pro, Enterprise
    val subscriptionActive: Boolean,
    val monthlyFeeIdr: Long,
    val noShowGraceMinutes: Int = 15,
    val isHolidayClosed: Boolean = false
)

@Entity(tableName = "saas_schedule_rules")
data class ScheduleRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: String,
    val doctorId: Int,
    val doctorName: String,
    val serviceUnit: String,
    val workStartHour: Int, // e.g., 8 (08:00 local)
    val workEndHour: Int,   // e.g., 15 (15:00 local)
    val breakStartHour: Int, // e.g., 12 (12:00 local)
    val breakEndHour: Int,   // e.g., 13 (13:00 local)
    val slotDurationMinutes: Int // e.g., 20 or 30
)

/**
 * Fase 1 Reservation Entity:
 * - TIDAK menyimpan diagnosis/SOAP/data klinis sensitif (disimpan di Fase 3).
 * - Memiliki UNIQUE INDEX pada (tenantId, doctorId, slotStartUtcMillis, activeLockKey)
 *   untuk mencegah Double Booking di level database SQLite/PostgreSQL.
 */
@Entity(
    tableName = "saas_phase1_reservations",
    indices = [
        Index(
            value = ["tenantId", "doctorId", "slotStartUtcMillis", "activeLockKey"],
            unique = true
        )
    ]
)
data class Phase1ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookingRef: String,
    val tenantId: String,
    val doctorId: Int,
    val doctorName: String,
    val serviceUnit: String,
    val patientPhoneHash: String,
    val patientDisplayName: String,
    val visitCategory: String, // Hanya kategori administratif Fase 1 (Tanpa diagnosis medis!)
    val slotStartUtcMillis: Long,
    val slotEndUtcMillis: Long,
    val queueCode: String,
    val status: String, // BOOKED, CHECKED_IN, SERVING, COMPLETED, CANCELLED, NO_SHOW
    val activeLockKey: Int = 1, // 1 jika slot aktif terkunci; jika CANCELLED/NO_SHOW diubah ke -id agar slot bisa dipesan ulang
    val pdpConsentVersion: String = "PDP-v1.0-2026",
    val pdpConsentTimestampUtc: Long = System.currentTimeMillis(),
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "saas_audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: String,
    val actorRole: String,
    val actorName: String,
    val actionType: String,
    val targetResource: String,
    val ipOrDeviceNote: String,
    val timestampUtcMillis: Long = System.currentTimeMillis()
)

data class GeneratedSlotItem(
    val slotStartUtcMillis: Long,
    val slotEndUtcMillis: Long,
    val displayLocalTime: String,
    val displayUtcTime: String,
    val isBreakTime: Boolean,
    val isOccupied: Boolean,
    val occupiedByRef: String? = null
)

object SaasTimeAndSlotEngine {
    fun formatUtcMillisToZone(utcMillis: Long, zone: IndonesianZone): String {
        val sdf = SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
        sdf.timeZone = TimeZone.getTimeZone(zone.zoneId)
        return "${sdf.format(Date(utcMillis))} ${zone.code}"
    }

    fun formatTimeOnly(utcMillis: Long, zone: IndonesianZone): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone(zone.zoneId)
        return "${sdf.format(Date(utcMillis))} ${zone.code}"
    }

    fun formatIsoUtc(utcMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(utcMillis))
    }

    /**
     * Menghasilkan daftar slot dinamis berdasarkan:
     * - Jam kerja dokter di zona waktu klinik
     * - Pemotongan jam istirahat (breakStartHour..breakEndHour)
     * - Durasi layanan dinamis (15 / 20 / 30 / 45 menit)
     * - Status okupansi dari database
     */
    fun generateDynamicSlotsForToday(
        rule: ScheduleRuleEntity,
        clinicZone: IndonesianZone,
        existingReservations: List<Phase1ReservationEntity>
    ): List<GeneratedSlotItem> {
        val tz = TimeZone.getTimeZone(clinicZone.zoneId)
        val cal = Calendar.getInstance(tz)
        cal.set(Calendar.HOUR_OF_DAY, rule.workStartHour)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val endCal = Calendar.getInstance(tz)
        endCal.timeInMillis = cal.timeInMillis
        endCal.set(Calendar.HOUR_OF_DAY, rule.workEndHour)
        endCal.set(Calendar.MINUTE, 0)

        val breakStartCal = Calendar.getInstance(tz)
        breakStartCal.timeInMillis = cal.timeInMillis
        breakStartCal.set(Calendar.HOUR_OF_DAY, rule.breakStartHour)
        breakStartCal.set(Calendar.MINUTE, 0)

        val breakEndCal = Calendar.getInstance(tz)
        breakEndCal.timeInMillis = cal.timeInMillis
        breakEndCal.set(Calendar.HOUR_OF_DAY, rule.breakEndHour)
        breakEndCal.set(Calendar.MINUTE, 0)

        val activeByStartUtc = existingReservations
            .filter { it.activeLockKey == 1 && it.doctorId == rule.doctorId && it.tenantId == rule.tenantId }
            .associateBy { it.slotStartUtcMillis }

        val slots = mutableListOf<GeneratedSlotItem>()
        val stepMillis = rule.slotDurationMinutes.coerceAtLeast(10) * 60_000L

        var cursor = cal.timeInMillis
        while (cursor + stepMillis <= endCal.timeInMillis) {
            val slotEnd = cursor + stepMillis
            val isDuringBreak = cursor < breakEndCal.timeInMillis && slotEnd > breakStartCal.timeInMillis
            val existing = activeByStartUtc[cursor]
            slots.add(
                GeneratedSlotItem(
                    slotStartUtcMillis = cursor,
                    slotEndUtcMillis = slotEnd,
                    displayLocalTime = formatTimeOnly(cursor, clinicZone),
                    displayUtcTime = formatTimeOnly(cursor, IndonesianZone.UTC),
                    isBreakTime = isDuringBreak,
                    isOccupied = existing != null,
                    occupiedByRef = existing?.bookingRef
                )
            )
            cursor = slotEnd
        }
        return slots
    }

    /**
     * Template notifikasi WhatsApp/Push yang patuh privasi (UU PDP):
     * TIDAK mencantumkan keluhan, diagnosis, atau poli spesifik yang sensitif.
     */
    fun buildPrivacySafeNotificationMessage(
        clinicName: String,
        bookingRef: String,
        queueCode: String,
        slotUtcMillis: Long,
        zone: IndonesianZone
    ): String {
        val localTimeStr = formatUtcMillisToZone(slotUtcMillis, zone)
        return "[Info Jadwal $clinicName]\n" +
            "Halo, reservasi kunjungan Anda telah tercatat.\n" +
            "• Kode Referensi: $bookingRef\n" +
            "• Nomor Antrean: $queueCode\n" +
            "• Waktu Kunjungan: $localTimeStr\n" +
            "Harap melakukan Check-In maksimal 15 menit sebelum jadwal. Demi privasi Anda, detail kunjungan hanya dapat dilihat di dalam aplikasi setelah masuk."
    }
}
