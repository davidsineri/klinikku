package com.klinikku.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.security.MessageDigest

/**
 * Fase 3 Entity 1: Rekam Medis Elektronik (RME) berbasis SOAP + ICD-10
 * - Patuh Permenkes No. 24 Tahun 2022 Pasal 29: Retensi minimal 25 tahun (retentionUntilYear = 2051).
 * - Jika isSignedLocked = true, rekam medis bersifat IMMUTABLE (tidak boleh dihapus;
 *   koreksi hanya boleh melalui Addendum/Catatan Tambahan).
 * - Siap disambungkan ke Fase 4 (SATUSEHAT FHIR Encounter & Condition) lewat icd10Code & satusehatRef.
 */
@Entity(
    tableName = "saas_phase3_emr_soap",
    indices = [
        Index(value = ["encounterCode"], unique = true),
        Index(value = ["tenantId", "patientRm"])
    ]
)
data class EmrSoapRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val encounterCode: String,
    val tenantId: String,
    val bookingRef: String,
    val patientName: String,
    val patientRm: String,
    val patientAllergies: String,
    val doctorName: String,
    val polyclinic: String,
    val bloodPressureMmHg: String, // e.g., "120/78 mmHg"
    val heartRateBpm: Int,         // e.g., 76
    val temperatureCelsius: Double, // e.g., 36.8
    val weightKg: Double,          // e.g., 64.5
    val subjectiveText: String,    // S: Keluhan utama & riwayat perjalanan penyakit
    val objectiveText: String,     // O: Pemeriksaan fisik & tanda vital
    val icd10Code: String,         // A: Kode ICD-10 standar WHO/Kemenkes (siap Fase 4 SATUSEHAT)
    val assessmentDiagnosis: String, // A: Diagnosis kerja / banding
    val planTherapy: String,       // P: Rencana terapi, edukasi & tindakan
    val addendumNotes: String = "", // Hanya boleh ditambah jika sudah dikunci TTE
    val isSignedLocked: Boolean = true,
    val encryptedCipherPreview: String,
    val retentionUntilYear: Int = 2051, // 2026 + 25 Tahun (Permenkes 24/2022)
    val satusehatEncounterRef: String = "FHIR-ENC-PENDING-PHASE4",
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 3 Entity 2: E-Resep Obat Elektronik + Deteksi Alergi Obat Otomatis
 */
@Entity(
    tableName = "saas_phase3_prescriptions",
    indices = [Index(value = ["tenantId", "rxNumber"], unique = true)]
)
data class ElectronicPrescriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rxNumber: String,
    val tenantId: String,
    val encounterCode: String,
    val patientName: String,
    val patientAllergies: String,
    val doctorName: String,
    val medicationListPipe: String, // Dipisahkan '|'
    val hasAllergyConflict: Boolean,
    val allergyWarningMessage: String,
    val pharmacyStatus: String, // "MENUNGGU_APOTEK" | "DISIAPKAN" | "DISERAHKAN"
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 3 Entity 3: Order & Hasil Laboratorium Diagnostik
 */
@Entity(
    tableName = "saas_phase3_lab_results",
    indices = [Index(value = ["tenantId", "labOrderNumber"])]
)
data class LabResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val labOrderNumber: String,
    val tenantId: String,
    val encounterCode: String,
    val patientName: String,
    val doctorName: String,
    val panelCategory: String, // "Hematologi", "Metabolik & Kimia Darah", "Imunologi"
    val loincCode: String,     // Kode standar LOINC untuk Fase 4 SATUSEHAT Observation
    val parameterName: String,
    val resultValue: String,
    val unit: String,
    val referenceRange: String,
    val flagStatus: String,    // "NORMAL" | "HIGH" | "CRITICAL"
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 3 Entity 4: Surat Keterangan Medis Resmi (Surat Sakit & Surat Sehat)
 */
@Entity(
    tableName = "saas_phase3_certificates",
    indices = [Index(value = ["tenantId", "certificateNumber"], unique = true)]
)
data class MedicalCertificateEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val certificateNumber: String,
    val tenantId: String,
    val encounterCode: String,
    val certificateType: String, // "SURAT_SAKIT" | "SURAT_SEHAT"
    val patientName: String,
    val patientRm: String,
    val doctorName: String,
    val restDaysCount: Int, // 0 jika Surat Sehat
    val validPeriodText: String,
    val medicalConclusion: String,
    val digitalSignatureHash: String,
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 3 Entity 5: Sesi Konsultasi Online / Telemedisin Terjadwal
 */
@Entity(
    tableName = "saas_phase3_teleconsults",
    indices = [Index(value = ["tenantId", "sessionCode"], unique = true)]
)
data class TeleconsultationSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionCode: String,
    val tenantId: String,
    val patientName: String,
    val doctorName: String,
    val polyclinic: String,
    val status: String, // "WAITING_ROOM" | "LIVE_VIDEO" | "COMPLETED_SOAP_LINKED"
    val clinicalChatTranscript: String,
    val informedConsentAccepted: Boolean = true,
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

object Phase3ClinicalSafetyEngine {
    val commonIcd10Options = listOf(
        "K21.9" to "Gastro-esophageal reflux disease (GERD)",
        "J06.9" to "Infeksi Saluran Pernapasan Akut (ISPA)",
        "K02.1" to "Karies Dentin & Pulpitis Reversibel",
        "I10" to "Hipertensi Esensial (Primer)",
        "E11.9" to "Diabetes Melitus Tipe 2 Tanpa Komplikasi",
        "L20.9" to "Dermatitis Atopik / Alergi Kulit",
        "A09" to "Gastroenteritis & Kolitis Akut"
    )

    /**
     * Memeriksa silang antara Riwayat Alergi Pasien vs Obat yang Diresepkan Dokter
     * (Clinical Decision Support / CDS).
     */
    fun checkDrugAllergyConflict(
        patientAllergies: String,
        medicationListText: String
    ): Pair<Boolean, String> {
        val allergyLower = patientAllergies.lowercase()
        val medsLower = medicationListText.lowercase()

        val penicillinGroup = listOf("amoxicillin", "ampicillin", "penisilin", "penicillin", "cefadroxil")
        val nsaidGroup = listOf("ibuprofen", "asam mefenamat", "natrium diklofenak", "aspirin", "ketorolac")

        if (allergyLower.contains("penisilin") || allergyLower.contains("amoxicillin")) {
            val matched = penicillinGroup.find { medsLower.contains(it) }
            if (matched != null) {
                return true to "BAHAYA ALERGI OBAT: Pasien memiliki riwayat alergi Golongan Penisilin, namun resep memuat '$matched'! Risiko syok anafilaksis."
            }
        }
        if (allergyLower.contains("nsaid") || allergyLower.contains("anti nyeri") || allergyLower.contains("ibuprofen")) {
            val matched = nsaidGroup.find { medsLower.contains(it) }
            if (matched != null) {
                return true to "PERINGATAN ALERGI: Pasien alergi obat golongan NSAID, namun resep memuat '$matched'!"
            }
        }
        return false to "Aman: Tidak ditemukan konflik alergi obat pada profil pasien."
    }

    /**
     * Menghasilkan simulasi tag terenkripsi AES-256-GCM untuk demonstrasi penyimpanan
     * kolom klinis sensitif di database sesuai UU PDP & Permenkes 24/2022.
     */
    fun generateEncryptedCipherPreview(plainSoap: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(plainSoap.toByteArray())
            val hex = digest.take(12).joinToString("") { "%02x".format(it) }
            "AES256-GCM:iv9f2a:$hex..."
        } catch (e: Exception) {
            "AES256-GCM:encrypted-payload"
        }
    }
}
