package com.klinikku.app.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Fase 4 Entity 1: Outbox Integrasi SATUSEHAT Kemenkes (Standar HL7 FHIR R4)
 * - Menggunakan pola Transactional Outbox agar kegagalan jaringan/timeout ke server
 *   SATUSEHAT tidak pernah menggagalkan penyimpanan RME dokter di klinik.
 * - Wajib memeriksa generalConsentAccepted == true sebelum pengiriman keluar klinik.
 */
@Entity(
    tableName = "saas_phase4_satusehat_outbox",
    indices = [
        Index(value = ["tenantId", "fhirBundleId"], unique = true),
        Index(value = ["encounterCode"])
    ]
)
data class SatusehatFhirOutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fhirBundleId: String,
    val tenantId: String,
    val branchCode: String,
    val encounterCode: String,
    val patientName: String,
    val patientIhsNumber: String,       // Nomor IHS Pasien Kemenkes (e.g., "P1029384756")
    val practitionerIhsNumber: String,  // Nomor IHS Nakes Kemenkes (e.g., "N1000293841")
    val resourceTypesSummary: String,   // e.g., "Encounter + Condition(K21.9) + MedicationRequest(KFA)"
    val icd10Code: String,
    val loincCode: String,
    val kfaCode: String,
    val generalConsentAccepted: Boolean, // Wajib true sesuai pedoman SATUSEHAT & UU PDP
    val syncStatus: String,             // "SYNCED_200_OK" | "QUEUED_OUTBOX" | "BLOCKED_NO_CONSENT" | "RETRY_BACKOFF"
    val retryCount: Int = 0,
    val fhirJsonPreview: String,
    val lastSyncResponse: String,
    val updatedAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 4 Entity 2: Bridging PCare BPJS Kesehatan (FKTP) & Rujukan Berjenjang (FKTL)
 */
@Entity(
    tableName = "saas_phase4_bpjs_pcare",
    indices = [Index(value = ["tenantId", "noKunjunganPcare"], unique = true)]
)
data class BpjsPcareClaimEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val noKunjunganPcare: String, // e.g., "0112U0451026P000101"
    val tenantId: String,
    val branchCode: String,
    val patientName: String,
    val bpjsCardNumber: String,   // 13 digit nomor kartu BPJS
    val membershipStatus: String, // "AKTIF (FKTP Terdaftar)" | "AKTIF (Luar Wilayah)" | "TUNGGAKAN_IURAN"
    val poliCodePcare: String,    // "001 - Poli Umum", "002 - Poli Gigi"
    val icd10Diagnosis: String,
    val serviceStatus: String,    // "TUNTAS_FKTP" | "RUJUK_FKTL_RS"
    val referralHospitalName: String, // Kosong jika TUNTAS_FKTP, atau nama RS rujukan
    val claimTariffType: String,  // "Kapitasi FKTP" | "Klaim Non-Kapitasi"
    val bridgingSyncState: String, // "BRIDGED_PCARE_200" | "OFFLINE_QUEUE_PENDING"
    val createdAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 4 Entity 3: Inventori Apotek Klinik Berstandar KFA (Kamus Farmasi & Alat Kesehatan) + FEFO
 */
@Entity(
    tableName = "saas_phase4_pharmacy_inventory",
    indices = [Index(value = ["tenantId", "branchCode", "kfaCode", "batchNumber"], unique = true)]
)
data class PharmacyInventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: String,
    val branchCode: String,
    val kfaCode: String,          // Kode KFA Kemenkes (e.g., "93001019")
    val medicationName: String,
    val dosageForm: String,       // "Kapsul 30mg", "Kaplet 500mg", "Suspensi 100ml"
    val batchNumber: String,
    val expiryDateIso: String,    // Format YYYY-MM-DD untuk pengurutan FEFO (First Expired First Out)
    val stockQuantity: Int,       // Dijaga agar tidak pernah < 0 lewat transaksi atomik
    val minReorderThreshold: Int,
    val unitPriceIdr: Long,
    val updatedAtUtcMillis: Long = System.currentTimeMillis()
)

/**
 * Fase 4 Entity 4: Manajemen Multi-Cabang Klinik & Metrik Performa Eksekutif
 */
@Entity(
    tableName = "saas_phase4_branches",
    indices = [Index(value = ["tenantId", "branchCode"], unique = true)]
)
data class ClinicBranchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantId: String,
    val branchCode: String,
    val branchName: String,
    val cityAndProvince: String,
    val timeZoneCode: String, // "WIB" | "WITA" | "WIT"
    val satusehatOrgId: String,
    val bpjsPcareCode: String,
    val activeDoctorsCount: Int,
    val monthlyVisitsCount: Int,
    val avgWaitTimeMinutes: Int,
    val noShowRatePercent: Double,
    val satusehatSyncPercent: Int,
    val monthlyRevenueIdr: Long,
    val isCentralBranch: Boolean = false
)

object Phase4IntegrationEngine {
    /**
     * Menghasilkan contoh Payload JSON HL7 FHIR R4 Bundle standar SATUSEHAT Kemenkes
     * yang menggabungkan Encounter, Condition (ICD-10), Observation (LOINC), & MedicationRequest (KFA).
     */
    fun buildSatusehatFhirBundleJson(
        bundleId: String,
        orgId: String,
        patientIhs: String,
        practitionerIhs: String,
        icd10Code: String,
        icd10Display: String,
        loincCode: String,
        kfaCode: String
    ): String {
        return """
{
  "resourceType": "Bundle",
  "id": "$bundleId",
  "type": "transaction",
  "entry": [
    {
      "resource": {
        "resourceType": "Encounter",
        "status": "finished",
        "class": { "system": "http://terminology.hl7.org/CodeSystem/v3-ActCode", "code": "AMB" },
        "subject": { "reference": "Patient/$patientIhs" },
        "participant": [{ "individual": { "reference": "Practitioner/$practitionerIhs" } }],
        "serviceProvider": { "reference": "Organization/$orgId" }
      }
    },
    {
      "resource": {
        "resourceType": "Condition",
        "code": {
          "coding": [{ "system": "http://hl7.org/fhir/sid/icd-10", "code": "$icd10Code", "display": "$icd10Display" }]
        },
        "subject": { "reference": "Patient/$patientIhs" }
      }
    },
    {
      "resource": {
        "resourceType": "Observation",
        "code": { "coding": [{ "system": "http://loinc.org", "code": "$loincCode" }] },
        "subject": { "reference": "Patient/$patientIhs" }
      }
    },
    {
      "resource": {
        "resourceType": "MedicationRequest",
        "status": "active",
        "intent": "order",
        "medicationCodeableConcept": {
          "coding": [{ "system": "http://sys-ids.kemkes.go.id/kfa", "code": "$kfaCode" }]
        },
        "subject": { "reference": "Patient/$patientIhs" }
      }
    }
  ]
}
        """.trimIndent()
    }
}
