package com.klinikku.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClinicDao {
    // Doctors
    @Query("SELECT * FROM doctors ORDER BY rating DESC, name ASC")
    fun getAllDoctors(): Flow<List<DoctorEntity>>

    @Query("SELECT COUNT(*) FROM doctors")
    suspend fun getDoctorCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    // Patients
    @Query("SELECT * FROM patients ORDER BY id ASC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: PatientEntity): Long

    @Update
    suspend fun updatePatient(patient: PatientEntity)

    @Query("DELETE FROM patients WHERE id = :patientId")
    suspend fun deletePatientById(patientId: Int)

    // Bookings
    @Query("SELECT * FROM bookings ORDER BY appointmentDate ASC, appointmentTime ASC, createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query(
        "SELECT * FROM bookings WHERE doctorId = :doctorId AND appointmentDate = :date AND status != 'DIBATALKAN'"
    )
    suspend fun getActiveBookingsForDoctorAndDate(doctorId: Int, date: String): List<BookingEntity>

    @Query(
        "SELECT COUNT(*) FROM bookings WHERE polyclinic = :polyclinic AND appointmentDate = :date"
    )
    suspend fun getBookingCountForPolyclinicAndDate(polyclinic: String, date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookings(bookings: List<BookingEntity>)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET status = :newStatus WHERE id = :bookingId")
    suspend fun updateBookingStatus(bookingId: Int, newStatus: String)

    @Query("DELETE FROM bookings WHERE bookingCode LIKE 'SIM-KRISIS-%'")
    suspend fun deleteCrisisSimulatedBookings()

    @Query(
        "UPDATE bookings SET status = :newStatus, clinicalSummary = :summary WHERE id = :bookingId"
    )
    suspend fun completeBookingWithSummary(bookingId: Int, newStatus: String, summary: String)

    @Query(
        "UPDATE bookings SET appointmentDate = :newDate, appointmentTime = :newTime, estimatedTurnTime = :newEstimated WHERE id = :bookingId"
    )
    suspend fun rescheduleBooking(
        bookingId: Int,
        newDate: String,
        newTime: String,
        newEstimated: String
    )

    // MCU Packages
    @Query("SELECT * FROM mcu_packages ORDER BY price ASC")
    fun getAllMcuPackages(): Flow<List<McuPackageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMcuPackages(packages: List<McuPackageEntity>)

    // Polyclinic Queues
    @Query("SELECT * FROM polyclinic_queues ORDER BY prefixCode ASC")
    fun getAllPolyclinicQueues(): Flow<List<PolyclinicQueueEntity>>

    @Query("SELECT * FROM polyclinic_queues WHERE polyclinic = :polyclinic LIMIT 1")
    suspend fun getQueueForPolyclinic(polyclinic: String): PolyclinicQueueEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolyclinicQueues(queues: List<PolyclinicQueueEntity>)

    @Update
    suspend fun updatePolyclinicQueue(queue: PolyclinicQueueEntity)

    // --- SaaS Fase 1 Multi-Tenant, Dynamic Slots, Anti-Double-Booking & Audit Log ---
    @Query("SELECT * FROM saas_tenants ORDER BY clinicName ASC")
    fun getAllSaasTenants(): Flow<List<TenantClinicEntity>>

    @Query("SELECT COUNT(*) FROM saas_tenants")
    suspend fun getSaasTenantCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaasTenants(tenants: List<TenantClinicEntity>)

    @Update
    suspend fun updateSaasTenant(tenant: TenantClinicEntity)

    @Query("SELECT * FROM saas_schedule_rules WHERE tenantId = :tenantId ORDER BY id ASC")
    fun getScheduleRulesForTenant(tenantId: String): Flow<List<ScheduleRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleRules(rules: List<ScheduleRuleEntity>)

    @Update
    suspend fun updateScheduleRule(rule: ScheduleRuleEntity)

    @Query("SELECT * FROM saas_phase1_reservations WHERE tenantId = :tenantId ORDER BY slotStartUtcMillis ASC")
    fun getPhase1ReservationsForTenant(tenantId: String): Flow<List<Phase1ReservationEntity>>

    @Query("SELECT * FROM saas_phase1_reservations ORDER BY slotStartUtcMillis ASC")
    fun getAllPhase1Reservations(): Flow<List<Phase1ReservationEntity>>

    /**
     * OnConflictStrategy.ABORT memastikan jika ada 2 request bersamaan pada
     * (tenantId, doctorId, slotStartUtcMillis, activeLockKey=1), database melempar
     * SQLiteConstraintException secara atomik!
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase1ReservationStrict(reservation: Phase1ReservationEntity): Long

    @Query(
        "SELECT COUNT(*) FROM saas_phase1_reservations WHERE tenantId = :tenantId AND doctorId = :doctorId AND slotStartUtcMillis = :slotStartUtc AND activeLockKey = 1"
    )
    suspend fun countActiveSlotLock(tenantId: String, doctorId: Int, slotStartUtc: Long): Int

    @Transaction
    suspend fun bookSlotAtomicTransaction(reservation: Phase1ReservationEntity): Long {
        val existingCount = countActiveSlotLock(
            tenantId = reservation.tenantId,
            doctorId = reservation.doctorId,
            slotStartUtc = reservation.slotStartUtcMillis
        )
        if (existingCount > 0) {
            throw IllegalStateException("CONFLICT_409: Slot sudah dikunci oleh transaksi pasien lain.")
        }
        return insertPhase1ReservationStrict(reservation)
    }

    @Query(
        "UPDATE saas_phase1_reservations SET status = :newStatus, activeLockKey = CASE WHEN :releaseSlot = 1 THEN -id ELSE activeLockKey END WHERE id = :id"
    )
    suspend fun updatePhase1ReservationStatus(id: Int, newStatus: String, releaseSlot: Int)

    @Query(
        "DELETE FROM saas_phase1_reservations WHERE tenantId = :tenantId AND patientDisplayName = :patientName"
    )
    suspend fun erasePatientPhase1Data(tenantId: String, patientName: String): Int

    @Query("SELECT * FROM saas_audit_logs ORDER BY timestampUtcMillis DESC LIMIT 50")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- SaaS Fase 2: Pembayaran/DP, Chat Klinik-Pasien & Ulasan Terverifikasi ---
    @Query("SELECT * FROM saas_phase2_invoices ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase2Invoices(): Flow<List<PaymentInvoiceEntity>>

    @Query("SELECT COUNT(*) FROM saas_phase2_invoices")
    suspend fun getPhase2InvoiceCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase2InvoiceStrict(invoice: PaymentInvoiceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase2InvoicesSeed(invoices: List<PaymentInvoiceEntity>)

    @Update
    suspend fun updatePhase2Invoice(invoice: PaymentInvoiceEntity)

    @Query("SELECT * FROM saas_phase2_invoices WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getInvoiceByIdempotencyKey(key: String): PaymentInvoiceEntity?

    @Query("SELECT * FROM saas_phase2_chats ORDER BY createdAtUtcMillis ASC")
    fun getAllPhase2Chats(): Flow<List<ClinicChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase2Chat(message: ClinicChatMessageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase2ChatsSeed(messages: List<ClinicChatMessageEntity>)

    @Query("SELECT * FROM saas_phase2_reviews ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase2Reviews(): Flow<List<ClinicReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase2ReviewStrict(review: ClinicReviewEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase2ReviewsSeed(reviews: List<ClinicReviewEntity>)

    @Update
    suspend fun updatePhase2Review(review: ClinicReviewEntity)

    // --- SaaS Fase 3: RME SOAP, E-Resep, Hasil Lab, Surat Medis & Konsultasi Online ---
    @Query("SELECT * FROM saas_phase3_emr_soap ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase3EmrRecords(): Flow<List<EmrSoapRecordEntity>>

    @Query("SELECT COUNT(*) FROM saas_phase3_emr_soap")
    suspend fun getPhase3EmrCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase3EmrStrict(record: EmrSoapRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase3EmrSeed(records: List<EmrSoapRecordEntity>)

    @Update
    suspend fun updatePhase3Emr(record: EmrSoapRecordEntity)

    @Query("SELECT * FROM saas_phase3_prescriptions ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase3Prescriptions(): Flow<List<ElectronicPrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase3PrescriptionStrict(prescription: ElectronicPrescriptionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase3PrescriptionsSeed(prescriptions: List<ElectronicPrescriptionEntity>)

    @Update
    suspend fun updatePhase3Prescription(prescription: ElectronicPrescriptionEntity)

    @Query("SELECT * FROM saas_phase3_lab_results ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase3LabResults(): Flow<List<LabResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase3LabResult(labResult: LabResultEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase3LabResultsSeed(labResults: List<LabResultEntity>)

    @Query("SELECT * FROM saas_phase3_certificates ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase3Certificates(): Flow<List<MedicalCertificateEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase3CertificateStrict(certificate: MedicalCertificateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase3CertificatesSeed(certificates: List<MedicalCertificateEntity>)

    @Query("SELECT * FROM saas_phase3_teleconsults ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase3Teleconsults(): Flow<List<TeleconsultationSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase3Teleconsult(session: TeleconsultationSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase3TeleconsultsSeed(sessions: List<TeleconsultationSessionEntity>)

    @Update
    suspend fun updatePhase3Teleconsult(session: TeleconsultationSessionEntity)

    // --- SaaS Fase 4: SATUSEHAT FHIR Outbox, BPJS PCare, Apotek KFA (FEFO) & Multi-Cabang ---
    @Query("SELECT * FROM saas_phase4_satusehat_outbox ORDER BY updatedAtUtcMillis DESC")
    fun getAllPhase4SatusehatOutbox(): Flow<List<SatusehatFhirOutboxEntity>>

    @Query("SELECT COUNT(*) FROM saas_phase4_satusehat_outbox")
    suspend fun getPhase4OutboxCount(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase4OutboxStrict(outbox: SatusehatFhirOutboxEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase4OutboxSeed(items: List<SatusehatFhirOutboxEntity>)

    @Update
    suspend fun updatePhase4Outbox(outbox: SatusehatFhirOutboxEntity)

    @Query("SELECT * FROM saas_phase4_bpjs_pcare ORDER BY createdAtUtcMillis DESC")
    fun getAllPhase4BpjsClaims(): Flow<List<BpjsPcareClaimEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase4BpjsClaimStrict(claim: BpjsPcareClaimEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase4BpjsClaimsSeed(claims: List<BpjsPcareClaimEntity>)

    @Update
    suspend fun updatePhase4BpjsClaim(claim: BpjsPcareClaimEntity)

    @Query("SELECT * FROM saas_phase4_pharmacy_inventory ORDER BY expiryDateIso ASC, medicationName ASC")
    fun getAllPhase4PharmacyInventory(): Flow<List<PharmacyInventoryEntity>>

    @Query("SELECT * FROM saas_phase4_pharmacy_inventory WHERE id = :itemId LIMIT 1")
    suspend fun getPharmacyItemById(itemId: Int): PharmacyInventoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase4PharmacySeed(items: List<PharmacyInventoryEntity>)

    @Update
    suspend fun updatePhase4PharmacyItem(item: PharmacyInventoryEntity)

    /**
     * Transaksi Atomik Pemotongan Stok Apotek (FEFO & Anti-Negative Stock):
     * Mencegah 2 kasir/apoteker memotong stok obat yang sama hingga minus.
     */
    @Transaction
    suspend fun dispenseMedicationAtomicFefo(itemId: Int, quantityToDeduct: Int): PharmacyInventoryEntity {
        val current = getPharmacyItemById(itemId)
            ?: throw IllegalStateException("OBAT_TIDAK_DITEMUKAN: Item inventori tidak ditemukan.")
        if (current.stockQuantity < quantityToDeduct) {
            throw IllegalStateException(
                "STOK_KURANG_409: Sisa stok ${current.medicationName} (${current.stockQuantity}) kurang dari permintaan ($quantityToDeduct). Pemotongan dibatalkan agar stok tidak minus!"
            )
        }
        val updated = current.copy(
            stockQuantity = current.stockQuantity - quantityToDeduct,
            updatedAtUtcMillis = System.currentTimeMillis()
        )
        updatePhase4PharmacyItem(updated)
        return updated
    }

    @Query("SELECT * FROM saas_phase4_branches ORDER BY isCentralBranch DESC, branchName ASC")
    fun getAllPhase4Branches(): Flow<List<ClinicBranchEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPhase4BranchStrict(branch: ClinicBranchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhase4BranchesSeed(branches: List<ClinicBranchEntity>)

    @Update
    suspend fun updatePhase4Branch(branch: ClinicBranchEntity)
}
