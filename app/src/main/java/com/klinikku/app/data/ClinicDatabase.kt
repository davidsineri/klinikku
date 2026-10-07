package com.klinikku.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.klinikku.app.BuildConfig

@Database(
    entities = [
        DoctorEntity::class,
        PatientEntity::class,
        BookingEntity::class,
        McuPackageEntity::class,
        PolyclinicQueueEntity::class,
        TenantClinicEntity::class,
        ScheduleRuleEntity::class,
        Phase1ReservationEntity::class,
        AuditLogEntity::class,
        PaymentInvoiceEntity::class,
        ClinicChatMessageEntity::class,
        ClinicReviewEntity::class,
        EmrSoapRecordEntity::class,
        ElectronicPrescriptionEntity::class,
        LabResultEntity::class,
        MedicalCertificateEntity::class,
        TeleconsultationSessionEntity::class,
        SatusehatFhirOutboxEntity::class,
        BpjsPcareClaimEntity::class,
        PharmacyInventoryEntity::class,
        ClinicBranchEntity::class
    ],
    version = 5,
    exportSchema = true
)
abstract class ClinicDatabase : RoomDatabase() {
    abstract fun clinicDao(): ClinicDao

    companion object {
        @Volatile
        private var INSTANCE: ClinicDatabase? = null

        fun getDatabase(context: Context): ClinicDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    ClinicDatabase::class.java,
                    "klinikku_database"
                )
                if (BuildConfig.DEBUG) {
                    builder.fallbackToDestructiveMigration()
                }
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedIfEmpty(dao: ClinicDao) {
            if (dao.getSaasTenantCount() == 0) {
                val tenants = listOf(
                    TenantClinicEntity(
                        tenantId = "klinik-jakarta-01",
                        clinicName = "Klinik Pratama Sehat Sentosa",
                        clinicType = "Klinik Umum & Gigi",
                        city = "Jakarta Selatan",
                        defaultZoneCode = "WIB",
                        subscriptionPlan = "Pro SaaS",
                        subscriptionActive = true,
                        monthlyFeeIdr = 499000L,
                        noShowGraceMinutes = 15,
                        isHolidayClosed = false
                    ),
                    TenantClinicEntity(
                        tenantId = "klinik-bali-02",
                        clinicName = "Klinik Estetika & Dental Cemerlang",
                        clinicType = "Gigi & Kecantikan",
                        city = "Denpasar, Bali",
                        defaultZoneCode = "WITA",
                        subscriptionPlan = "Starter SaaS",
                        subscriptionActive = true,
                        monthlyFeeIdr = 299000L,
                        noShowGraceMinutes = 15,
                        isHolidayClosed = false
                    ),
                    TenantClinicEntity(
                        tenantId = "klinik-papua-03",
                        clinicName = "Praktik Mandiri dr. Kasih & Rekan",
                        clinicType = "Praktik Mandiri",
                        city = "Jayapura",
                        defaultZoneCode = "WIT",
                        subscriptionPlan = "Enterprise Multi-Cabang",
                        subscriptionActive = true,
                        monthlyFeeIdr = 899000L,
                        noShowGraceMinutes = 20,
                        isHolidayClosed = false
                    )
                )
                dao.insertSaasTenants(tenants)

                val rules = listOf(
                    ScheduleRuleEntity(
                        id = 1,
                        tenantId = "klinik-jakarta-01",
                        doctorId = 101,
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        serviceUnit = "Poli Penyakit Dalam",
                        workStartHour = 8,
                        workEndHour = 15,
                        breakStartHour = 12,
                        breakEndHour = 13,
                        slotDurationMinutes = 30
                    ),
                    ScheduleRuleEntity(
                        id = 2,
                        tenantId = "klinik-jakarta-01",
                        doctorId = 102,
                        doctorName = "drg. Hendra Wijaya, Sp.KG",
                        serviceUnit = "Poli Gigi",
                        workStartHour = 9,
                        workEndHour = 16,
                        breakStartHour = 12,
                        breakEndHour = 13,
                        slotDurationMinutes = 30
                    ),
                    ScheduleRuleEntity(
                        id = 3,
                        tenantId = "klinik-bali-02",
                        doctorId = 201,
                        doctorName = "dr. Citra Lestari, Sp.DVE",
                        serviceUnit = "Klinik Estetika Medis",
                        workStartHour = 10,
                        workEndHour = 17,
                        breakStartHour = 13,
                        breakEndHour = 14,
                        slotDurationMinutes = 30
                    ),
                    ScheduleRuleEntity(
                        id = 4,
                        tenantId = "klinik-papua-03",
                        doctorId = 301,
                        doctorName = "dr. Bagus Santoso",
                        serviceUnit = "Praktik Umum Mandiri",
                        workStartHour = 8,
                        workEndHour = 14,
                        breakStartHour = 11,
                        breakEndHour = 12,
                        slotDurationMinutes = 20
                    )
                )
                dao.insertScheduleRules(rules)

                dao.insertAuditLog(
                    AuditLogEntity(
                        tenantId = "klinik-jakarta-01",
                        actorRole = "SUPER_ADMIN",
                        actorName = "System Provisioner",
                        actionType = "INIT_MULTI_TENANT_RLS",
                        targetResource = "3 Klinik Tenant + Isolasi Tenant Aktif",
                        ipOrDeviceNote = "Galaxy Tab A11+ Cloud Node"
                    )
                )
            }

            if (dao.getPhase2InvoiceCount() == 0) {
                val now = System.currentTimeMillis()
                val seedInvoices = listOf(
                    PaymentInvoiceEntity(
                        id = 1,
                        invoiceNumber = "INV-2610-801",
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2610-401",
                        patientName = "Budi Raharjo",
                        familyRelation = "Diri Sendiri",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        serviceUnit = "Poli Penyakit Dalam",
                        paymentTypeLabel = "DP Komitmen Antrean (Rp 50.000)",
                        paymentChannel = "QRIS Dinamis",
                        totalConsultationFee = 220000L,
                        billedAmount = 50000L,
                        status = "PAID",
                        idempotencyKey = "IDEMP-JKT-801",
                        webhookCallbackCount = 1,
                        expiresAtUtcMillis = now + 900_000L,
                        paidAtUtcMillis = now - 3600_000L
                    ),
                    PaymentInvoiceEntity(
                        id = 2,
                        invoiceNumber = "INV-2610-802",
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2610-419",
                        patientName = "Siti Larasati",
                        familyRelation = "Pasangan",
                        doctorName = "drg. Hendra Wijaya, Sp.KG",
                        serviceUnit = "Poli Gigi & Mulut",
                        paymentTypeLabel = "DP Tindakan Scaling (Rp 75.000)",
                        paymentChannel = "QRIS Dinamis",
                        totalConsultationFee = 275000L,
                        billedAmount = 75000L,
                        status = "PENDING",
                        idempotencyKey = "IDEMP-JKT-802",
                        webhookCallbackCount = 0,
                        expiresAtUtcMillis = now + 900_000L,
                        paidAtUtcMillis = null
                    ),
                    PaymentInvoiceEntity(
                        id = 3,
                        invoiceNumber = "INV-2610-803",
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2609-188",
                        patientName = "Arka Raharjo",
                        familyRelation = "Anak",
                        doctorName = "dr. Arief Budiman, Sp.A",
                        serviceUnit = "Poli Anak",
                        paymentTypeLabel = "Pelunasan Konsultasi Penuh",
                        paymentChannel = "BCA Virtual Account",
                        totalConsultationFee = 210000L,
                        billedAmount = 210000L,
                        status = "PAID",
                        idempotencyKey = "IDEMP-JKT-803",
                        webhookCallbackCount = 1,
                        expiresAtUtcMillis = now - 86400_000L,
                        paidAtUtcMillis = now - 86400_000L
                    ),
                    PaymentInvoiceEntity(
                        id = 4,
                        invoiceNumber = "INV-2610-901",
                        tenantId = "klinik-bali-02",
                        bookingRef = "BLI-2610-104",
                        patientName = "Ni Luh Putu Ayu",
                        familyRelation = "Diri Sendiri",
                        doctorName = "dr. Citra Lestari, Sp.DVE",
                        serviceUnit = "Klinik Estetika Medis",
                        paymentTypeLabel = "DP Reservasi Estetika",
                        paymentChannel = "GoPay / OVO",
                        totalConsultationFee = 350000L,
                        billedAmount = 100000L,
                        status = "PAID",
                        idempotencyKey = "IDEMP-BLI-901",
                        webhookCallbackCount = 1,
                        expiresAtUtcMillis = now + 900_000L,
                        paidAtUtcMillis = now - 1800_000L
                    )
                )
                dao.insertPhase2InvoicesSeed(seedInvoices)

                val seedChats = listOf(
                    ClinicChatMessageEntity(
                        id = 1,
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2610-419",
                        senderRole = "PATIENT",
                        senderName = "Siti Larasati (Pasien)",
                        messageText = "Halo Admin Klinik, untuk jadwal drg. Hendra besok jam 10:00 WIB apakah saya perlu datang 15 menit lebih awal?",
                        hasPrivacyWarning = false,
                        isReadByRecipient = true,
                        createdAtUtcMillis = now - 1200_000L
                    ),
                    ClinicChatMessageEntity(
                        id = 2,
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2610-419",
                        senderRole = "CLINIC_STAFF",
                        senderName = "Resepsionis Nadia (Staf Klinik)",
                        messageText = "Selamat pagi Ibu Siti! Betul, silakan melakukan Check-In 15 menit sebelum jadwal dan menyelesaikan DP Rp 75.000 via QRIS agar nomor antrean B-02 langsung terkunci.",
                        hasPrivacyWarning = false,
                        isReadByRecipient = true,
                        createdAtUtcMillis = now - 900_000L
                    ),
                    ClinicChatMessageEntity(
                        id = 3,
                        tenantId = "klinik-bali-02",
                        bookingRef = "BLI-2610-104",
                        senderRole = "CLINIC_STAFF",
                        senderName = "Admin Klinik Bali",
                        messageText = "Om Swastiastu Kak Putu, pembayaran DP Rp 100.000 sudah kami terima otomatis. Sampai jumpa pukul 11:00 WITA.",
                        hasPrivacyWarning = false,
                        isReadByRecipient = true,
                        createdAtUtcMillis = now - 600_000L
                    )
                )
                dao.insertPhase2ChatsSeed(seedChats)

                val seedReviews = listOf(
                    ClinicReviewEntity(
                        id = 1,
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2609-188",
                        doctorName = "dr. Arief Budiman, Sp.A",
                        serviceUnit = "Poli Anak",
                        patientDisplayName = "Budi Raharjo (Orang Tua Arka)",
                        maskPatientName = true,
                        ratingStars = 5,
                        reviewComment = "Sistem antrean sangat akurat! Anak saya tidak rewel karena kami baru berangkat dari rumah saat antrean C-01 dipanggil. Dokter Arief sangat sabar menjelaskan tumbuh kembang anak.",
                        clinicOfficialReply = "Terima kasih banyak Bapak/Ibu atas kepercayaannya pada Poli Anak kami. Semoga Ananda Arka sehat selalu!",
                        createdAtUtcMillis = now - 80000_000L
                    ),
                    ClinicReviewEntity(
                        id = 2,
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2609-142",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        serviceUnit = "Poli Penyakit Dalam",
                        patientDisplayName = "Hendrik Wijaya",
                        maskPatientName = true,
                        ratingStars = 5,
                        reviewComment = "Sejak ada fitur bayar DP via QRIS, tidak ada lagi antrean diserobot atau jadwal fiktif. Ruang tunggu jadi sangat nyaman dan tenang.",
                        clinicOfficialReply = "Terima kasih atas ulasan positifnya! Kami terus berkomitmen menjaga ketepatan waktu layanan.",
                        createdAtUtcMillis = now - 160000_000L
                    ),
                    ClinicReviewEntity(
                        id = 3,
                        tenantId = "klinik-bali-02",
                        bookingRef = "BLI-2610-104",
                        doctorName = "dr. Citra Lestari, Sp.DVE",
                        serviceUnit = "Klinik Estetika Medis",
                        patientDisplayName = "Ni Luh Putu Ayu",
                        maskPatientName = true,
                        ratingStars = 5,
                        reviewComment = "Pelayanan ramah dan modern, notifikasi pengingatnya sopan tanpa mengumbar privasi di layar HP.",
                        clinicOfficialReply = "Matur suksma Kak! Privasi dan kenyamanan pasien adalah prioritas utama kami.",
                        createdAtUtcMillis = now - 40000_000L
                    )
                )
                dao.insertPhase2ReviewsSeed(seedReviews)
            }

            if (dao.getPhase3EmrCount() == 0) {
                val now = System.currentTimeMillis()
                val seedEmr = listOf(
                    EmrSoapRecordEntity(
                        id = 1,
                        encounterCode = "ENC-2610-401",
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2610-401",
                        patientName = "Budi Raharjo",
                        patientRm = "RM-2026-0841",
                        patientAllergies = "Tidak ada alergi obat",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        polyclinic = "Poli Penyakit Dalam",
                        bloodPressureMmHg = "124/80 mmHg",
                        heartRateBpm = 78,
                        temperatureCelsius = 36.7,
                        weightKg = 68.5,
                        subjectiveText = "Pasien mengeluhkan rasa panas di ulu hati (heartburn) menjalar ke dada tengah terutama setelah makan malam pedas sejak 4 hari lalu, disertai mual ringan.",
                        objectiveText = "Keadaan umum kompos mentis. TD 124/80 mmHg, Nadi 78x/mnt, Suhu 36.7°C. Palpasi abdomen: nyeri tekan epigastrium (+), bising usus normal, hepar/lien tidak teraba.",
                        icd10Code = "K21.9",
                        assessmentDiagnosis = "Gastro-esophageal reflux disease (GERD) tanpa esofagitis + Dispepsia Fungsional",
                        planTherapy = "1. Lansoprazole 30mg kapsul (1x1 pagi 30 menit sebelum makan)\n2. Sukralfat suspensi 500mg/5ml (3x10ml 1 jam sebelum makan)\n3. Edukasi: Hindari tidur <2 jam setelah makan, kurangi kopi, cokelat, dan makanan asam/pedas.",
                        addendumNotes = "Addendum [dr. Nadia Sp.PD]: Pasien juga disarankan cek Profil Lipid & Glukosa Puasa karena riwayat kolesterol borderline bulan lalu.",
                        isSignedLocked = true,
                        encryptedCipherPreview = Phase3ClinicalSafetyEngine.generateEncryptedCipherPreview("ENC-2610-401:K21.9:BudiRaharjo"),
                        retentionUntilYear = 2051,
                        satusehatEncounterRef = "FHIR-ENC-JKT-88401",
                        createdAtUtcMillis = now - 7200_000L
                    ),
                    EmrSoapRecordEntity(
                        id = 2,
                        encounterCode = "ENC-2610-419",
                        tenantId = "klinik-jakarta-01",
                        bookingRef = "KLK-2610-419",
                        patientName = "Siti Larasati",
                        patientRm = "RM-2026-0842",
                        patientAllergies = "Antibiotik Golongan Penisilin",
                        doctorName = "drg. Hendra Wijaya, Sp.KG",
                        polyclinic = "Poli Gigi & Mulut",
                        bloodPressureMmHg = "116/74 mmHg",
                        heartRateBpm = 74,
                        temperatureCelsius = 36.6,
                        weightKg = 54.0,
                        subjectiveText = "Pasien datang untuk scaling rutin dan mengeluh ngilu tajam pada gigi geraham kanan bawah saat minum dingin.",
                        objectiveText = "Kalkulus subgingiva regio 31-42 (+). Kavitas media pada oklusal gigi 46 mencapai dentin, tes termal dingin (+), perkusi (-). CATATAN ALERGI: Pasien alergi Penisilin/Amoxicillin!",
                        icd10Code = "K02.1",
                        assessmentDiagnosis = "Karies Dentin Gigi 46 + Gingivitis Marginalis Kronis",
                        planTherapy = "1. Scaling ultrasonik rahang atas & bawah + tumpatan resin komposit sinar gigi 46.\n2. Analgesik non-penisilin: Paracetamol 500mg bila ngilu.\n3. DILARANG meresepkan Amoxicillin/Ampicillin karena riwayat alergi Penisilin.",
                        addendumNotes = "",
                        isSignedLocked = true,
                        encryptedCipherPreview = Phase3ClinicalSafetyEngine.generateEncryptedCipherPreview("ENC-2610-419:K02.1:SitiLarasati"),
                        retentionUntilYear = 2051,
                        satusehatEncounterRef = "FHIR-ENC-JKT-88419",
                        createdAtUtcMillis = now - 3600_000L
                    ),
                    EmrSoapRecordEntity(
                        id = 3,
                        encounterCode = "ENC-2610-901",
                        tenantId = "klinik-bali-02",
                        bookingRef = "BLI-2610-104",
                        patientName = "Ni Luh Putu Ayu",
                        patientRm = "RM-2026-0988",
                        patientAllergies = "Tidak ada alergi obat",
                        doctorName = "dr. Citra Lestari, Sp.DVE",
                        polyclinic = "Klinik Estetika Medis",
                        bloodPressureMmHg = "112/72 mmHg",
                        heartRateBpm = 72,
                        temperatureCelsius = 36.5,
                        weightKg = 52.0,
                        subjectiveText = "Kemerahan dan gatal ringan pada pipi setelah paparan sinar matahari pantai 2 hari berturut-turut.",
                        objectiveText = "Makula eritematosa batas tidak tegas pada regio malar bilateral, skuama halus (+), tanda infeksi sekunder (-).",
                        icd10Code = "L20.9",
                        assessmentDiagnosis = "Dermatitis Kontak Iritan Surya Ringan / Sunburn",
                        planTherapy = "1. Krim Ceramide + Panthenol Barrier Repair (2x sehari)\n2. Cetirizine 10mg tablet (1x malam bila gatal)\n3. Sunscreen SPF 50+ PA++++ wajib ulang tiap 3 jam.",
                        addendumNotes = "",
                        isSignedLocked = true,
                        encryptedCipherPreview = Phase3ClinicalSafetyEngine.generateEncryptedCipherPreview("ENC-2610-901:L20.9:NiLuhPutuAyu"),
                        retentionUntilYear = 2051,
                        satusehatEncounterRef = "FHIR-ENC-BLI-99104",
                        createdAtUtcMillis = now - 1800_000L
                    )
                )
                dao.insertPhase3EmrSeed(seedEmr)

                val seedPrescriptions = listOf(
                    ElectronicPrescriptionEntity(
                        id = 1,
                        rxNumber = "RX-2610-301",
                        tenantId = "klinik-jakarta-01",
                        encounterCode = "ENC-2610-401",
                        patientName = "Budi Raharjo",
                        patientAllergies = "Tidak ada alergi obat",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        medicationListPipe = "Lansoprazole 30mg Kapsul (1x1 sebelum makan pagi, No. X)|Sukralfat Suspensi 500mg/5ml (3x10ml sebelum makan, 1 Botol)|Domperidone 10mg Tablet (bila mual, No. VI)",
                        hasAllergyConflict = false,
                        allergyWarningMessage = "Aman: Tidak ditemukan konflik alergi obat pada profil pasien.",
                        pharmacyStatus = "DISERAHKAN",
                        createdAtUtcMillis = now - 7000_000L
                    ),
                    ElectronicPrescriptionEntity(
                        id = 2,
                        rxNumber = "RX-2610-302",
                        tenantId = "klinik-jakarta-01",
                        encounterCode = "ENC-2610-419",
                        patientName = "Siti Larasati",
                        patientAllergies = "Antibiotik Golongan Penisilin",
                        doctorName = "drg. Hendra Wijaya, Sp.KG",
                        medicationListPipe = "Paracetamol 500mg Kaplet (3x1 bila nyeri pasca tambal, No. VI)|Obat Kumur Chlorhexidine Gluconate 0.2% (2x sehari, 1 Botol)",
                        hasAllergyConflict = false,
                        allergyWarningMessage = "CDS Verified: Bebas antibiotik golongan Penisilin/Amoxicillin.",
                        pharmacyStatus = "DISIAPKAN",
                        createdAtUtcMillis = now - 3400_000L
                    ),
                    ElectronicPrescriptionEntity(
                        id = 3,
                        rxNumber = "RX-2610-905",
                        tenantId = "klinik-bali-02",
                        encounterCode = "ENC-2610-901",
                        patientName = "Ni Luh Putu Ayu",
                        patientAllergies = "Tidak ada alergi obat",
                        doctorName = "dr. Citra Lestari, Sp.DVE",
                        medicationListPipe = "Moisturizer Ceramide + Panthenol 30g (2x sehari)|Cetirizine 10mg Tablet (1x1 malam, No. V)",
                        hasAllergyConflict = false,
                        allergyWarningMessage = "Aman: Tidak ditemukan konflik alergi obat pada profil pasien.",
                        pharmacyStatus = "MENUNGGU_APOTEK",
                        createdAtUtcMillis = now - 1600_000L
                    )
                )
                dao.insertPhase3PrescriptionsSeed(seedPrescriptions)

                val seedLabs = listOf(
                    LabResultEntity(
                        id = 1,
                        labOrderNumber = "LAB-2610-501",
                        tenantId = "klinik-jakarta-01",
                        encounterCode = "ENC-2610-401",
                        patientName = "Budi Raharjo",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        panelCategory = "Metabolik & Kimia Darah",
                        loincCode = "2093-3",
                        parameterName = "Kolesterol Total Serum",
                        resultValue = "228",
                        unit = "mg/dL",
                        referenceRange = "< 200 mg/dL",
                        flagStatus = "HIGH",
                        createdAtUtcMillis = now - 6800_000L
                    ),
                    LabResultEntity(
                        id = 2,
                        labOrderNumber = "LAB-2610-502",
                        tenantId = "klinik-jakarta-01",
                        encounterCode = "ENC-2610-401",
                        patientName = "Budi Raharjo",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        panelCategory = "Metabolik & Kimia Darah",
                        loincCode = "1558-6",
                        parameterName = "Glukosa Darah Puasa (GDP)",
                        resultValue = "94",
                        unit = "mg/dL",
                        referenceRange = "70 – 100 mg/dL",
                        flagStatus = "NORMAL",
                        createdAtUtcMillis = now - 6700_000L
                    ),
                    LabResultEntity(
                        id = 3,
                        labOrderNumber = "LAB-2610-503",
                        tenantId = "klinik-jakarta-01",
                        encounterCode = "ENC-2610-401",
                        patientName = "Budi Raharjo",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        panelCategory = "Hematologi Lengkap",
                        loincCode = "718-7",
                        parameterName = "Hemoglobin (Hb)",
                        resultValue = "14.6",
                        unit = "g/dL",
                        referenceRange = "13.2 – 16.6 g/dL",
                        flagStatus = "NORMAL",
                        createdAtUtcMillis = now - 6600_000L
                    )
                )
                dao.insertPhase3LabResultsSeed(seedLabs)

                val seedCertificates = listOf(
                    MedicalCertificateEntity(
                        id = 1,
                        certificateNumber = "SKD-2610-701",
                        tenantId = "klinik-jakarta-01",
                        encounterCode = "ENC-2610-401",
                        certificateType = "SURAT_SAKIT",
                        patientName = "Budi Raharjo",
                        patientRm = "RM-2026-0841",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        restDaysCount = 2,
                        validPeriodText = "2 Hari (4 Okt 2026 s/d 5 Okt 2026)",
                        medicalConclusion = "Berdasarkan pemeriksaan fisik medis, pasien memerlukan istirahat medis selama 2 (dua) hari karena kondisi kesehatan. (Sesuai etika privasi medis, detail kode diagnosis ICD-10 tidak dicantumkan untuk pihak HRD kecuali atas izin tertulis pasien).",
                        digitalSignatureHash = "TTE-SHA256:9f84b2c10e77a3d9",
                        createdAtUtcMillis = now - 6500_000L
                    ),
                    MedicalCertificateEntity(
                        id = 2,
                        certificateNumber = "SKS-2610-702",
                        tenantId = "klinik-jakarta-01",
                        encounterCode = "ENC-2610-419",
                        certificateType = "SURAT_SEHAT",
                        patientName = "Siti Larasati",
                        patientRm = "RM-2026-0842",
                        doctorName = "dr. Bagus Santoso",
                        restDaysCount = 0,
                        validPeriodText = "Berlaku 30 Hari sejak diterbitkan",
                        medicalConclusion = "Dinyatakan SEHAT JASMANI untuk keperluan administrasi pekerjaan/perjalanan. TD: 116/74 mmHg, Nadi: 74x/mnt, Suhu: 36.6°C, BB: 54 kg, Buta Warna: Negatif (Normal).",
                        digitalSignatureHash = "TTE-SHA256:3a12e88b9104f6c2",
                        createdAtUtcMillis = now - 3200_000L
                    )
                )
                dao.insertPhase3CertificatesSeed(seedCertificates)

                val seedTeleconsults = listOf(
                    TeleconsultationSessionEntity(
                        id = 1,
                        sessionCode = "TELE-2610-901",
                        tenantId = "klinik-jakarta-01",
                        patientName = "Budi Raharjo",
                        doctorName = "dr. Nadia Prameswari, Sp.PD",
                        polyclinic = "Poli Penyakit Dalam",
                        status = "LIVE_VIDEO",
                        clinicalChatTranscript = "[Informed Consent Telemedisin Disetujui Pasien]\n" +
                            "Budi Raharjo: Dok, setelah minum Lansoprazole nyeri ulu hati sudah jauh berkurang, apakah hasil lab kolesterol saya sudah keluar?\n" +
                            "dr. Nadia Sp.PD: Halo Pak Budi, betul hasil lab Kolesterol Total Bapak 228 mg/dL (sedikit di atas batas 200). Kita mulai modifikasi diet rendah lemak jenuh selama 4 minggu dulu ya.",
                        informedConsentAccepted = true,
                        createdAtUtcMillis = now - 1200_000L
                    ),
                    TeleconsultationSessionEntity(
                        id = 2,
                        sessionCode = "TELE-2610-902",
                        tenantId = "klinik-jakarta-01",
                        patientName = "Siti Larasati",
                        doctorName = "drg. Hendra Wijaya, Sp.KG",
                        polyclinic = "Poli Gigi & Mulut",
                        status = "WAITING_ROOM",
                        clinicalChatTranscript = "[Informed Consent Telemedisin Disetujui Pasien]\n" +
                            "Siti Larasati: Halo Dokter Hendra, saya ingin konsultasi kontrol pasca tambal gigi kemarin.",
                        informedConsentAccepted = true,
                        createdAtUtcMillis = now - 600_000L
                    )
                )
                dao.insertPhase3TeleconsultsSeed(seedTeleconsults)
            }

            if (dao.getPhase4OutboxCount() == 0) {
                val now = System.currentTimeMillis()
                val seedOutbox = listOf(
                    SatusehatFhirOutboxEntity(
                        id = 1,
                        fhirBundleId = "FHIR-BND-2610-401",
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        encounterCode = "ENC-2610-401",
                        patientName = "Budi Raharjo",
                        patientIhsNumber = "P10293847501",
                        practitionerIhsNumber = "N10002938411",
                        resourceTypesSummary = "Encounter + Condition(K21.9) + Observation(2093-3) + MedicationRequest(93001019)",
                        icd10Code = "K21.9",
                        loincCode = "2093-3",
                        kfaCode = "93001019",
                        generalConsentAccepted = true,
                        syncStatus = "SYNCED_200_OK",
                        retryCount = 1,
                        fhirJsonPreview = Phase4IntegrationEngine.buildSatusehatFhirBundleJson(
                            bundleId = "FHIR-BND-2610-401",
                            orgId = "100028491",
                            patientIhs = "P10293847501",
                            practitionerIhs = "N10002938411",
                            icd10Code = "K21.9",
                            icd10Display = "Gastro-esophageal reflux disease without oesophagitis",
                            loincCode = "2093-3",
                            kfaCode = "93001019"
                        ),
                        lastSyncResponse = "200 OK • SATUSEHAT FHIR Server: Bundle Transaction Committed (Encounter ID: 84920-a91b)",
                        updatedAtUtcMillis = now - 5400_000L
                    ),
                    SatusehatFhirOutboxEntity(
                        id = 2,
                        fhirBundleId = "FHIR-BND-2610-419",
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        encounterCode = "ENC-2610-419",
                        patientName = "Siti Larasati",
                        patientIhsNumber = "P10293847502",
                        practitionerIhsNumber = "N10002938422",
                        resourceTypesSummary = "Encounter + Condition(K02.1) + MedicationRequest(93004122)",
                        icd10Code = "K02.1",
                        loincCode = "718-7",
                        kfaCode = "93004122",
                        generalConsentAccepted = true,
                        syncStatus = "QUEUED_OUTBOX",
                        retryCount = 0,
                        fhirJsonPreview = Phase4IntegrationEngine.buildSatusehatFhirBundleJson(
                            bundleId = "FHIR-BND-2610-419",
                            orgId = "100028491",
                            patientIhs = "P10293847502",
                            practitionerIhs = "N10002938422",
                            icd10Code = "K02.1",
                            icd10Display = "Caries of dentine",
                            loincCode = "718-7",
                            kfaCode = "93004122"
                        ),
                        lastSyncResponse = "MENUNGGU WORKER OUTBOX: Siap dikirim ke endpoint FHIR Kemenkes (Consent Pasien Aktif)",
                        updatedAtUtcMillis = now - 1800_000L
                    ),
                    SatusehatFhirOutboxEntity(
                        id = 3,
                        fhirBundleId = "FHIR-BND-2610-901",
                        tenantId = "klinik-bali-02",
                        branchCode = "CAB-DPS-01",
                        encounterCode = "ENC-2610-901",
                        patientName = "Ni Luh Putu Ayu",
                        patientIhsNumber = "P10293847991",
                        practitionerIhsNumber = "N10002938881",
                        resourceTypesSummary = "Encounter + Condition(L20.9) + MedicationRequest(93005510)",
                        icd10Code = "L20.9",
                        loincCode = "1558-6",
                        kfaCode = "93005510",
                        generalConsentAccepted = true,
                        syncStatus = "SYNCED_200_OK",
                        retryCount = 1,
                        fhirJsonPreview = Phase4IntegrationEngine.buildSatusehatFhirBundleJson(
                            bundleId = "FHIR-BND-2610-901",
                            orgId = "100099120",
                            patientIhs = "P10293847991",
                            practitionerIhs = "N10002938881",
                            icd10Code = "L20.9",
                            icd10Display = "Atopic dermatitis, unspecified",
                            loincCode = "1558-6",
                            kfaCode = "93005510"
                        ),
                        lastSyncResponse = "200 OK • SATUSEHAT FHIR Server Bali Node",
                        updatedAtUtcMillis = now - 900_000L
                    )
                )
                dao.insertPhase4OutboxSeed(seedOutbox)

                val seedBpjs = listOf(
                    BpjsPcareClaimEntity(
                        id = 1,
                        noKunjunganPcare = "0112U0451026P000101",
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        patientName = "Budi Raharjo",
                        bpjsCardNumber = "0001849203194",
                        membershipStatus = "AKTIF (FKTP Terdaftar)",
                        poliCodePcare = "001 - Poli Umum / Penyakit Dalam FKTP",
                        icd10Diagnosis = "K21.9 - Gastro-esophageal reflux disease",
                        serviceStatus = "TUNTAS_FKTP",
                        referralHospitalName = "-",
                        claimTariffType = "Kapitasi FKTP",
                        bridgingSyncState = "BRIDGED_PCARE_200",
                        createdAtUtcMillis = now - 5000_000L
                    ),
                    BpjsPcareClaimEntity(
                        id = 2,
                        noKunjunganPcare = "0112U0451026P000108",
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        patientName = "Hendrik Wijaya",
                        bpjsCardNumber = "0001849203881",
                        membershipStatus = "AKTIF (FKTP Terdaftar)",
                        poliCodePcare = "001 - Poli Umum",
                        icd10Diagnosis = "I20.0 - Unstable Angina (Indikasi Rujukan Spesialis Jantung)",
                        serviceStatus = "RUJUK_FKTL_RS",
                        referralHospitalName = "RSUP Fatmawati Jakarta (Poli Jantung & Pembuluh Darah)",
                        claimTariffType = "Kapitasi FKTP (Surat Rujukan VClaim Aktif)",
                        bridgingSyncState = "BRIDGED_PCARE_200",
                        createdAtUtcMillis = now - 2400_000L
                    )
                )
                dao.insertPhase4BpjsClaimsSeed(seedBpjs)

                val seedPharmacy = listOf(
                    PharmacyInventoryEntity(
                        id = 1,
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        kfaCode = "93001019",
                        medicationName = "Lansoprazole 30 mg Kapsul Lepas Tunda",
                        dosageForm = "Kapsul 30 mg",
                        batchNumber = "BATCH-LNS-26A",
                        expiryDateIso = "2027-02-15",
                        stockQuantity = 140,
                        minReorderThreshold = 40,
                        unitPriceIdr = 3500L
                    ),
                    PharmacyInventoryEntity(
                        id = 2,
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        kfaCode = "93004122",
                        medicationName = "Paracetamol 500 mg Kaplet Generik",
                        dosageForm = "Kaplet 500 mg",
                        batchNumber = "BATCH-PCT-26B",
                        expiryDateIso = "2026-12-10",
                        stockQuantity = 24,
                        minReorderThreshold = 30,
                        unitPriceIdr = 1200L
                    ),
                    PharmacyInventoryEntity(
                        id = 3,
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        kfaCode = "93008812",
                        medicationName = "Clindamycin 300 mg Kapsul (Alternatif Non-Penisilin)",
                        dosageForm = "Kapsul 300 mg",
                        batchNumber = "BATCH-CLN-26C",
                        expiryDateIso = "2027-06-30",
                        stockQuantity = 85,
                        minReorderThreshold = 25,
                        unitPriceIdr = 4800L
                    ),
                    PharmacyInventoryEntity(
                        id = 4,
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        kfaCode = "93005510",
                        medicationName = "Cetirizine HCl 10 mg Tablet Salut",
                        dosageForm = "Tablet 10 mg",
                        batchNumber = "BATCH-CTZ-26D",
                        expiryDateIso = "2027-09-20",
                        stockQuantity = 18,
                        minReorderThreshold = 25,
                        unitPriceIdr = 1800L
                    ),
                    PharmacyInventoryEntity(
                        id = 5,
                        tenantId = "klinik-bali-02",
                        branchCode = "CAB-DPS-01",
                        kfaCode = "93009104",
                        medicationName = "Ceramide + Panthenol Barrier Cream 30g",
                        dosageForm = "Tube 30g",
                        batchNumber = "BATCH-CRM-26E",
                        expiryDateIso = "2027-04-01",
                        stockQuantity = 42,
                        minReorderThreshold = 15,
                        unitPriceIdr = 85000L
                    )
                )
                dao.insertPhase4PharmacySeed(seedPharmacy)

                val seedBranches = listOf(
                    ClinicBranchEntity(
                        id = 1,
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-JKT-SEL",
                        branchName = "Cabang Utama Kebayoran Baru (Pusat)",
                        cityAndProvince = "Jakarta Selatan, DKI Jakarta",
                        timeZoneCode = "WIB",
                        satusehatOrgId = "ORG-100028491",
                        bpjsPcareCode = "0112U045",
                        activeDoctorsCount = 8,
                        monthlyVisitsCount = 1420,
                        avgWaitTimeMinutes = 11,
                        noShowRatePercent = 4.2,
                        satusehatSyncPercent = 98,
                        monthlyRevenueIdr = 184500000L,
                        isCentralBranch = true
                    ),
                    ClinicBranchEntity(
                        id = 2,
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-BDG-UTR",
                        branchName = "Cabang Dago Pakar Bandung",
                        cityAndProvince = "Kota Bandung, Jawa Barat",
                        timeZoneCode = "WIB",
                        satusehatOrgId = "ORG-100028492",
                        bpjsPcareCode = "0114U088",
                        activeDoctorsCount = 5,
                        monthlyVisitsCount = 890,
                        avgWaitTimeMinutes = 13,
                        noShowRatePercent = 5.1,
                        satusehatSyncPercent = 96,
                        monthlyRevenueIdr = 112000000L,
                        isCentralBranch = false
                    ),
                    ClinicBranchEntity(
                        id = 3,
                        tenantId = "klinik-jakarta-01",
                        branchCode = "CAB-DPS-BALI",
                        branchName = "Cabang Renon Denpasar (Zona WITA)",
                        cityAndProvince = "Denpasar, Bali",
                        timeZoneCode = "WITA",
                        satusehatOrgId = "ORG-100028499",
                        bpjsPcareCode = "0220U019",
                        activeDoctorsCount = 4,
                        monthlyVisitsCount = 640,
                        avgWaitTimeMinutes = 10,
                        noShowRatePercent = 3.8,
                        satusehatSyncPercent = 99,
                        monthlyRevenueIdr = 96500000L,
                        isCentralBranch = false
                    ),
                    ClinicBranchEntity(
                        id = 4,
                        tenantId = "klinik-bali-02",
                        branchCode = "CAB-DPS-01",
                        branchName = "Cabang Utama Seminyak Estetika",
                        cityAndProvince = "Badung, Bali",
                        timeZoneCode = "WITA",
                        satusehatOrgId = "ORG-100099120",
                        bpjsPcareCode = "NON-BPJS",
                        activeDoctorsCount = 4,
                        monthlyVisitsCount = 510,
                        avgWaitTimeMinutes = 9,
                        noShowRatePercent = 3.5,
                        satusehatSyncPercent = 97,
                        monthlyRevenueIdr = 158000000L,
                        isCentralBranch = true
                    )
                )
                dao.insertPhase4BranchesSeed(seedBranches)
            }

            if (dao.getDoctorCount() > 0) return

            val doctors = listOf(
                DoctorEntity(
                    id = 1,
                    name = "dr. Nadia Prameswari, Sp.PD",
                    polyclinic = "Poli Penyakit Dalam",
                    specialtyTitle = "Spesialis Penyakit Dalam & Metabolik",
                    strNumber = "STR-31.1.4.401.22.104892",
                    experienceYears = 11,
                    consultationFee = 220000L,
                    rating = 4.9,
                    reviewCount = 342,
                    roomNumber = "Ruang 104 • Lantai 1",
                    availableDays = "Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu",
                    timeSlots = "08:30,09:30,10:30,13:00,14:30,16:00",
                    dailyQuota = 18,
                    acceptsBpjs = true,
                    bio = "Berpengalaman menangani diabetes melitus, hipertensi, kolesterol, gangguan pencernaan (GERD/maag kronis), serta skrining kesehatan dewasa terpadu.",
                    avatarColorHex = 0xFF0D9488
                ),
                DoctorEntity(
                    id = 2,
                    name = "drg. Hendra Wijaya, Sp.KG",
                    polyclinic = "Poli Gigi & Mulut",
                    specialtyTitle = "Spesialis Konservasi Gigi & Endodontik",
                    strNumber = "STR-31.2.1.102.21.098311",
                    experienceYears = 9,
                    consultationFee = 185000L,
                    rating = 4.9,
                    reviewCount = 289,
                    roomNumber = "Ruang 201 • Lantai 2",
                    availableDays = "Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu",
                    timeSlots = "09:00,10:00,11:00,14:00,15:30,17:00",
                    dailyQuota = 12,
                    acceptsBpjs = true,
                    bio = "Ahli perawatan saluran akar tanpa nyeri, tambal estetik sinar laser, scaling karang gigi ultrasonik, dan rekonstruksi mahkota gigi.",
                    avatarColorHex = 0xFF0284C7
                ),
                DoctorEntity(
                    id = 3,
                    name = "dr. Arief Budiman, Sp.A",
                    polyclinic = "Poli Anak",
                    specialtyTitle = "Spesialis Kesehatan Anak & Tumbuh Kembang",
                    strNumber = "STR-32.1.2.205.20.112540",
                    experienceYears = 14,
                    consultationFee = 210000L,
                    rating = 5.0,
                    reviewCount = 418,
                    roomNumber = "Ruang 102 • Lantai 1 (Ramah Anak)",
                    availableDays = "Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu",
                    timeSlots = "08:00,09:00,10:00,11:00,15:00,16:30",
                    dailyQuota = 20,
                    acceptsBpjs = true,
                    bio = "Fokus pada imunisasi lengkap IDAI, pemantauan gizi & tumbuh kembang balita, alergi anak, serta penanganan infeksi saluran napas akut.",
                    avatarColorHex = 0xFFF59E0B
                ),
                DoctorEntity(
                    id = 4,
                    name = "dr. Bagus Santoso",
                    polyclinic = "Poli Umum",
                    specialtyTitle = "Dokter Umum & Layanan Primer Faskes 1",
                    strNumber = "STR-31.1.1.100.23.154008",
                    experienceYears = 7,
                    consultationFee = 95000L,
                    rating = 4.8,
                    reviewCount = 512,
                    roomNumber = "Ruang 101 • Lantai 1",
                    availableDays = "Senin,Selasa,Rabu,Kamis,Jumat,Sabtu,Minggu",
                    timeSlots = "07:30,08:30,09:30,10:30,13:00,15:00,17:00,19:00",
                    dailyQuota = 30,
                    acceptsBpjs = true,
                    bio = "Melayani pemeriksaan kesehatan umum, demam, batuk pilek, surat keterangan sehat, rawat luka ringan, dan rujukan berjenjang BPJS Kesehatan.",
                    avatarColorHex = 0xFF059669
                ),
                DoctorEntity(
                    id = 5,
                    name = "dr. Citra Lestari, Sp.DVE",
                    polyclinic = "Poli Kulit & Estetika",
                    specialtyTitle = "Spesialis Dermatologi, Venereologi & Estetika",
                    strNumber = "STR-31.2.5.502.22.087419",
                    experienceYears = 8,
                    consultationFee = 250000L,
                    rating = 4.9,
                    reviewCount = 264,
                    roomNumber = "Ruang 204 • Lantai 2",
                    availableDays = "Senin,Rabu,Kamis,Jumat,Sabtu,Minggu",
                    timeSlots = "10:00,11:00,13:30,15:00,16:30,18:00",
                    dailyQuota = 15,
                    acceptsBpjs = false,
                    bio = "Menangani dermatitis atopik, eksim, terapi jerawat klinis, alergi kulit, jamur, serta peremajaan kulit medis berbasis bukti.",
                    avatarColorHex = 0xFFE11D48
                ),
                DoctorEntity(
                    id = 6,
                    name = "dr. Rina Kusuma, Sp.M",
                    polyclinic = "Poli Mata",
                    specialtyTitle = "Spesialis Mata & Refraksi Klinis",
                    strNumber = "STR-31.2.3.304.21.076210",
                    experienceYears = 10,
                    consultationFee = 230000L,
                    rating = 4.8,
                    reviewCount = 195,
                    roomNumber = "Ruang 202 • Lantai 2",
                    availableDays = "Senin,Selasa,Kamis,Jumat,Sabtu,Minggu",
                    timeSlots = "09:00,10:30,13:00,14:30,16:00",
                    dailyQuota = 14,
                    acceptsBpjs = true,
                    bio = "Pemeriksaan visus & koreksi kacamata presisi, sindrom mata kering digital, skrining katarak dini, dan glaukoma.",
                    avatarColorHex = 0xFF7C3AED
                ),
                DoctorEntity(
                    id = 7,
                    name = "dr. Dimas Pratama, Sp.THT-BKL",
                    polyclinic = "Poli THT",
                    specialtyTitle = "Spesialis Telinga Hidung Tenggorok",
                    strNumber = "STR-31.1.6.601.20.065332",
                    experienceYears = 12,
                    consultationFee = 225000L,
                    rating = 4.9,
                    reviewCount = 231,
                    roomNumber = "Ruang 203 • Lantai 2",
                    availableDays = "Selasa,Rabu,Kamis,Sabtu,Minggu",
                    timeSlots = "08:30,10:00,13:30,15:00,16:30",
                    dailyQuota = 15,
                    acceptsBpjs = true,
                    bio = "Endoskopi THT tanpa nyeri, pembersihan serumen telinga mikroskopik, penanganan sinusitis alergi, radang amandel, dan vertigo perifer.",
                    avatarColorHex = 0xFF0369A1
                ),
                DoctorEntity(
                    id = 8,
                    name = "dr. Maya Sari, Sp.OG",
                    polyclinic = "Poli Kandungan",
                    specialtyTitle = "Spesialis Obstetri & Ginekologi (USG 4D)",
                    strNumber = "STR-31.2.4.409.19.054821",
                    experienceYears = 13,
                    consultationFee = 260000L,
                    rating = 5.0,
                    reviewCount = 376,
                    roomNumber = "Ruang 105 • Lantai 1",
                    availableDays = "Senin,Selasa,Rabu,Jumat,Sabtu,Minggu",
                    timeSlots = "09:00,10:00,11:00,14:00,15:30,17:00",
                    dailyQuota = 16,
                    acceptsBpjs = true,
                    bio = "Kontrol kehamilan terpadu dengan USG 4D HD-Live, konsultasi program hamil, kesehatan reproduksi wanita, dan KB modern.",
                    avatarColorHex = 0xFFDB2777
                )
            )
            dao.insertDoctors(doctors)

            val patients = listOf(
                PatientEntity(
                    id = 1,
                    fullName = "Budi Raharjo",
                    nik = "3174051405940001",
                    medicalRecordNumber = "RM-2026-0841",
                    birthDate = "14 Mei 1994",
                    gender = "Laki-laki",
                    bloodType = "O+",
                    phone = "0812-8490-3321",
                    paymentMethodDefault = "BPJS Kesehatan",
                    bpjsOrInsuranceNumber = "0001849203194",
                    allergies = "Tidak ada alergi obat",
                    relationship = "Diri Sendiri"
                ),
                PatientEntity(
                    id = 2,
                    fullName = "Siti Larasati",
                    nik = "3174052208960002",
                    medicalRecordNumber = "RM-2026-0842",
                    birthDate = "22 Agustus 1996",
                    gender = "Perempuan",
                    bloodType = "A+",
                    phone = "0813-9920-1145",
                    paymentMethodDefault = "Umum / Mandiri",
                    bpjsOrInsuranceNumber = "0001849203195",
                    allergies = "Antibiotik Golongan Penisilin",
                    relationship = "Pasangan"
                ),
                PatientEntity(
                    id = 3,
                    fullName = "Arka Raharjo",
                    nik = "3174051002220003",
                    medicalRecordNumber = "RM-2026-0915",
                    birthDate = "10 Februari 2022",
                    gender = "Laki-laki",
                    bloodType = "O+",
                    phone = "0812-8490-3321",
                    paymentMethodDefault = "Asuransi Swasta",
                    bpjsOrInsuranceNumber = "PRU-99482104",
                    allergies = "Debu & Dingin",
                    relationship = "Anak"
                )
            )
            patients.forEach { dao.insertPatient(it) }

            val mcuPackages = listOf(
                McuPackageEntity(
                    id = 1,
                    title = "Paket MCU Dasar Sehat & Metabolik",
                    category = "Umum & Lab",
                    price = 385000L,
                    originalPrice = 520000L,
                    durationMinutes = 45,
                    preparationNote = "Puasa makan 8–10 jam sebelum pengambilan darah (diperbolehkan minum air putih).",
                    includedTests = "Konsultasi & Pemeriksaan Fisik Dokter|Darah Lengkap (Hematologi 18 Parameter)|Gula Darah Puasa & HbA1c|Profil Lipid (Kolesterol Total, LDL, HDL, Trigliserida)|Asam Urat & Fungsi Ginjal (Ureum/Kreatinin)|Urine Lengkap",
                    assignedDoctorId = 1
                ),
                McuPackageEntity(
                    id = 2,
                    title = "Paket Skrining Jantung & Ekokardiografi Dasar",
                    category = "Jantung",
                    price = 640000L,
                    originalPrice = 850000L,
                    durationMinutes = 60,
                    preparationNote = "Puasa 8 jam, hindari konsumsi kopi/kafein dan olahraga berat 12 jam sebelum tindakan.",
                    includedTests = "Konsultasi Spesialis Penyakit Dalam|Rekam Jantung EKG 12-Lead|Rontgen Thorax Digital|Profil Kolesterol Lengkap & Gula Darah|Pemeriksaan Tekanan Darah & Indeks Massa Tubuh",
                    assignedDoctorId = 1
                ),
                McuPackageEntity(
                    id = 3,
                    title = "Paket Dental Scaling Ultrasonik & Polishing",
                    category = "Gigi",
                    price = 275000L,
                    originalPrice = 380000L,
                    durationMinutes = 40,
                    preparationNote = "Tidak perlu puasa. Sikat gigi seperti biasa sebelum datang ke klinik.",
                    includedTests = "Pemeriksaan Kamera Intraoral HD|Scaling Karang Gigi Rahang Atas & Bawah|Polishing Stain Kopi/Teh|Aplikasi Fluoride Pelindung Email Gigi|Edukasi Perawatan Gusi",
                    assignedDoctorId = 2
                ),
                McuPackageEntity(
                    id = 4,
                    title = "Paket Tumbuh Kembang & Imunisasi Anak",
                    category = "Anak",
                    price = 310000L,
                    originalPrice = 420000L,
                    durationMinutes = 40,
                    preparationNote = "Bawa Buku KIA / Catatan Imunisasi anak.",
                    includedTests = "Konsultasi Spesialis Anak (Sp.A)|Skrining Kurva Pertumbuhan WHO & Denver II|Pemeriksaan THT & Gigi Anak Dasar|Suplementasi Vitamin A & Konsultasi Gizi MPASI/Anak",
                    assignedDoctorId = 3
                ),
                McuPackageEntity(
                    id = 5,
                    title = "Paket Cek Lab Demam, Dengue & Tifoid Cepat",
                    category = "Imunitas",
                    price = 245000L,
                    originalPrice = 330000L,
                    durationMinutes = 30,
                    preparationNote = "Tidak memerlukan persiapan puasa. Hasil laboratorium keluar dalam 45 menit.",
                    includedTests = "Konsultasi Dokter Umum Siaga|Hematologi Lengkap + Trombosit & Hematokrit|Tes NS1 / IgG IgM Dengue (DBD)|Tes Tubex / Widal Tifoid|Resep Obat Penurun Panas & Elektrolit",
                    assignedDoctorId = 4
                )
            )
            dao.insertMcuPackages(mcuPackages)

            val queues = listOf(
                PolyclinicQueueEntity(
                    polyclinic = "Poli Umum",
                    prefixCode = "A",
                    currentNumber = 3,
                    totalIssuedToday = 6,
                    roomNumber = "Ruang 101 • Lantai 1",
                    activeDoctorName = "dr. Bagus Santoso",
                    avgMinutesPerPatient = 12
                ),
                PolyclinicQueueEntity(
                    polyclinic = "Poli Gigi & Mulut",
                    prefixCode = "B",
                    currentNumber = 1,
                    totalIssuedToday = 3,
                    roomNumber = "Ruang 201 • Lantai 2",
                    activeDoctorName = "drg. Hendra Wijaya, Sp.KG",
                    avgMinutesPerPatient = 25
                ),
                PolyclinicQueueEntity(
                    polyclinic = "Poli Anak",
                    prefixCode = "C",
                    currentNumber = 2,
                    totalIssuedToday = 4,
                    roomNumber = "Ruang 102 • Lantai 1",
                    activeDoctorName = "dr. Arief Budiman, Sp.A",
                    avgMinutesPerPatient = 15
                ),
                PolyclinicQueueEntity(
                    polyclinic = "Poli Penyakit Dalam",
                    prefixCode = "D",
                    currentNumber = 2,
                    totalIssuedToday = 4,
                    roomNumber = "Ruang 104 • Lantai 1",
                    activeDoctorName = "dr. Nadia Prameswari, Sp.PD",
                    avgMinutesPerPatient = 18
                ),
                PolyclinicQueueEntity(
                    polyclinic = "Poli Kulit & Estetika",
                    prefixCode = "E",
                    currentNumber = 1,
                    totalIssuedToday = 2,
                    roomNumber = "Ruang 204 • Lantai 2",
                    activeDoctorName = "dr. Citra Lestari, Sp.DVE",
                    avgMinutesPerPatient = 20
                ),
                PolyclinicQueueEntity(
                    polyclinic = "Poli Mata",
                    prefixCode = "F",
                    currentNumber = 1,
                    totalIssuedToday = 2,
                    roomNumber = "Ruang 202 • Lantai 2",
                    activeDoctorName = "dr. Rina Kusuma, Sp.M",
                    avgMinutesPerPatient = 15
                ),
                PolyclinicQueueEntity(
                    polyclinic = "Poli THT",
                    prefixCode = "G",
                    currentNumber = 1,
                    totalIssuedToday = 2,
                    roomNumber = "Ruang 203 • Lantai 2",
                    activeDoctorName = "dr. Dimas Pratama, Sp.THT-BKL",
                    avgMinutesPerPatient = 18
                ),
                PolyclinicQueueEntity(
                    polyclinic = "Poli Kandungan",
                    prefixCode = "H",
                    currentNumber = 1,
                    totalIssuedToday = 3,
                    roomNumber = "Ruang 105 • Lantai 1",
                    activeDoctorName = "dr. Maya Sari, Sp.OG",
                    avgMinutesPerPatient = 20
                )
            )
            dao.insertPolyclinicQueues(queues)

            val todayIso = ClinicFormatters.getTodayIso()
            val tomorrowIso = ClinicFormatters.getOffsetDateIso(1)
            val pastIso = ClinicFormatters.getOffsetDateIso(-5)

            val sampleBookings = listOf(
                BookingEntity(
                    id = 1,
                    bookingCode = "KLK-2610-401",
                    queueNumber = "D-04",
                    queueOrder = 4,
                    patientId = 1,
                    patientName = "Budi Raharjo",
                    patientRm = "RM-2026-0841",
                    doctorId = 1,
                    doctorName = "dr. Nadia Prameswari, Sp.PD",
                    polyclinic = "Poli Penyakit Dalam",
                    roomNumber = "Ruang 104 • Lantai 1",
                    appointmentDate = todayIso,
                    appointmentTime = "09:30",
                    bookingType = "Konsultasi Dokter",
                    packageName = "",
                    symptomsOrNotes = "Kontrol rutin asam lambung (GERD) & cek hasil kolesterol bulanan.",
                    paymentType = "BPJS Kesehatan",
                    totalCost = 0L,
                    status = "CHECK_IN",
                    estimatedTurnTime = "09:48 WIB",
                    clinicalSummary = ""
                ),
                BookingEntity(
                    id = 2,
                    bookingCode = "KLK-2610-419",
                    queueNumber = "B-02",
                    queueOrder = 2,
                    patientId = 2,
                    patientName = "Siti Larasati",
                    patientRm = "RM-2026-0842",
                    doctorId = 2,
                    doctorName = "drg. Hendra Wijaya, Sp.KG",
                    polyclinic = "Poli Gigi & Mulut",
                    roomNumber = "Ruang 201 • Lantai 2",
                    appointmentDate = tomorrowIso,
                    appointmentTime = "10:00",
                    bookingType = "Medical Check-Up",
                    packageName = "Paket Dental Scaling Ultrasonik & Polishing",
                    symptomsOrNotes = "Pembersihan karang gigi rutin 6 bulanan & gusi kadang berdarah saat menyikat gigi.",
                    paymentType = "Umum / Mandiri",
                    totalCost = 275000L,
                    status = "TERJADWAL",
                    estimatedTurnTime = "10:15 WIB",
                    clinicalSummary = ""
                ),
                BookingEntity(
                    id = 3,
                    bookingCode = "KLK-2609-188",
                    queueNumber = "C-03",
                    queueOrder = 3,
                    patientId = 3,
                    patientName = "Arka Raharjo",
                    patientRm = "RM-2026-0915",
                    doctorId = 3,
                    doctorName = "dr. Arief Budiman, Sp.A",
                    polyclinic = "Poli Anak",
                    roomNumber = "Ruang 102 • Lantai 1 (Ramah Anak)",
                    appointmentDate = pastIso,
                    appointmentTime = "09:00",
                    bookingType = "Konsultasi Dokter",
                    packageName = "",
                    symptomsOrNotes = "Batuk berdahak ringan 2 hari dan konsultasi nafsu makan.",
                    paymentType = "Asuransi Swasta",
                    totalCost = 210000L,
                    status = "SELESAI",
                    estimatedTurnTime = "09:10 WIB",
                    clinicalSummary = "Diagnosis: Rhinofaringitis Akut Ringan (Common Cold).\nTD/Suhu: 36.8°C • BB: 16.4 kg (Status Gizi Baik).\nResep & Terapi:\n1. Ambroxol Sirup 15mg/5ml (3x sehari 2.5ml sesudah makan)\n2. Cetirizine Drops (1x malam bila hidung tersumbat)\n3. Multivitamin Anak Zinc + Lysine (1x sehari).\nAnjuran: Perbanyak minum air hangat, hindari makanan berminyak."
                )
            )
            dao.insertBookings(sampleBookings)
        }
    }
}
