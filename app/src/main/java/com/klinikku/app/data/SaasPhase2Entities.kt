package com.klinikku.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fase 2 Entity 1: Tagihan Pembayaran & DP (Down Payment)
 * - Memiliki UNIQUE INDEX pada idempotencyKey agar jika Payment Gateway (Midtrans/Xendit)
 *   mengirimkan webhook berulang kali, pembayaran tidak pernah tercatat ganda.
 */
@Entity(
    tableName = "saas_phase2_invoices",
    indices = [
        Index(value = ["idempotencyKey"], unique = true),
        Index(value = ["tenantId", "bookingRef"])
    ]
)
data class PaymentInvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceNumber: String,
    val tenantId: String,
    val bookingRef: String,
    val patientName: String,
    val familyRelation: String, // "Diri Sendiri", "Anak", "Pasangan", "Orang Tua"
    val doctorName: String,
    val serviceUnit: String,
    val paymentTypeLabel: String, // "DP Reservasi (Rp 50.000)" | "Pelunasan Penuh"
    val paymentChannel: String, // "QRIS Dinamis", "BCA Virtual Account", "GoPay / OVO", "Tunai Kasir"
    val totalConsultationFee: Long,
    val billedAmount: Long, // Nominal DP atau Pelunasan
    val status: String, // "PENDING", "PAID", "EXPIRED", "REFUNDED"
    val idempotencyKey: String,
    val webhookCallbackCount: Int = 0,
    val expiresAtUtcMillis: Long,
    val paidAtUtcMillis: Long? = null,
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 2 Entity 2: Chat Administrasi Klinik <-> Pasien
 * - Dilengkapi sensor privasi otomatis agar percakapan jadwal/pembayaran tidak bocor
 *   menjadi konsultasi diagnosis medis liar sebelum Fase 3.
 */
@Entity(
    tableName = "saas_phase2_chats",
    indices = [Index(value = ["tenantId", "createdAtUtcMillis"])]
)
data class ClinicChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: String,
    val bookingRef: String,
    val senderRole: String, // "PATIENT" | "CLINIC_STAFF"
    val senderName: String,
    val messageText: String,
    val hasPrivacyWarning: Boolean = false,
    val isReadByRecipient: Boolean = false,
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 2 Entity 3: Ulasan & Rating Kunjungan Terverifikasi
 * - UNIQUE INDEX pada (tenantId, bookingRef) memastikan 1 kunjungan selesai hanya bisa
 *   memberi 1 ulasan asli (mencegah spam rating palsu).
 */
@Entity(
    tableName = "saas_phase2_reviews",
    indices = [Index(value = ["tenantId", "bookingRef"], unique = true)]
)
data class ClinicReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: String,
    val bookingRef: String,
    val doctorName: String,
    val serviceUnit: String,
    val patientDisplayName: String,
    val maskPatientName: Boolean = true, // Tampilkan sebagai "B*** R***" demi privasi UU PDP
    val ratingStars: Int, // 1..5
    val reviewComment: String,
    val clinicOfficialReply: String = "",
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

data class Phase2TenantReport(
    val tenantId: String,
    val clinicName: String,
    val totalGrossRevenueIdr: Long,
    val totalDpCollectedIdr: Long,
    val pendingReceivableIdr: Long,
    val paidInvoicesCount: Int,
    val pendingInvoicesCount: Int,
    val refundedInvoicesCount: Int,
    val noShowRateBeforeDpPercent: Float,
    val noShowRateWithDpPercent: Float,
    val averageRating: Float,
    val totalReviewsCount: Int,
    val qrisSharePercent: Float,
    val vaSharePercent: Float,
    val ewalletSharePercent: Float
)

object Phase2PrivacyHelper {
    private val sensitiveMedicalKeywords = listOf(
        "hiv", "sifilis", "gonore", "aborsi", "psikiatri", "depresi berat", "resep antibiotik keras"
    )

    fun maskNameForPublicReview(fullName: String, shouldMask: Boolean): String {
        if (!shouldMask) return fullName
        val parts = fullName.trim().split(" ").filter { it.isNotEmpty() }
        if (parts.isEmpty()) return "Pasien Terverifikasi"
        return parts.joinToString(" ") { part ->
            if (part.length <= 2) "${part.first()}*" else "${part.first()}***${part.last()}"
        }
    }

    /**
     * Memeriksa apakah pesan chat mengandung kata sensitif klinis atau nomor NIK 16 digit
     * yang seharusnya masuk ke modul Rekam Medis Fase 3 (bukan chat admin Fase 2).
     */
    fun checkChatPrivacyWarning(rawText: String): Boolean {
        val has16Digits = Regex("\\b\\d{16}\\b").containsMatchIn(rawText)
        val hasSensitiveMed = sensitiveMedicalKeywords.any {
            rawText.contains(it, ignoreCase = true)
        }
        return has16Digits || hasSensitiveMed
    }
}
