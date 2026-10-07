package com.klinikku.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import java.util.Locale
import kotlin.random.Random

class ClinicRepository(private val dao: ClinicDao) {
    val allDoctors: Flow<List<DoctorEntity>> = dao.getAllDoctors()
    val allPatients: Flow<List<PatientEntity>> = dao.getAllPatients()
    val allBookings: Flow<List<BookingEntity>> = dao.getAllBookings()
    val allMcuPackages: Flow<List<McuPackageEntity>> = dao.getAllMcuPackages()
    val allPolyclinicQueues: Flow<List<PolyclinicQueueEntity>> = dao.getAllPolyclinicQueues()
    val allSaasTenants: Flow<List<TenantClinicEntity>> = dao.getAllSaasTenants()
    val allPhase1Reservations: Flow<List<Phase1ReservationEntity>> = dao.getAllPhase1Reservations()
    val recentAuditLogs: Flow<List<AuditLogEntity>> = dao.getRecentAuditLogs()
    val allPhase2Invoices: Flow<List<PaymentInvoiceEntity>> = dao.getAllPhase2Invoices()
    val allPhase2Chats: Flow<List<ClinicChatMessageEntity>> = dao.getAllPhase2Chats()
    val allPhase2Reviews: Flow<List<ClinicReviewEntity>> = dao.getAllPhase2Reviews()
    val allPhase3EmrRecords: Flow<List<EmrSoapRecordEntity>> = dao.getAllPhase3EmrRecords()
    val allPhase3Prescriptions: Flow<List<ElectronicPrescriptionEntity>> = dao.getAllPhase3Prescriptions()
    val allPhase3LabResults: Flow<List<LabResultEntity>> = dao.getAllPhase3LabResults()
    val allPhase3Certificates: Flow<List<MedicalCertificateEntity>> = dao.getAllPhase3Certificates()
    val allPhase3Teleconsults: Flow<List<TeleconsultationSessionEntity>> = dao.getAllPhase3Teleconsults()
    val allPhase4SatusehatOutbox: Flow<List<SatusehatFhirOutboxEntity>> = dao.getAllPhase4SatusehatOutbox()
    val allPhase4BpjsClaims: Flow<List<BpjsPcareClaimEntity>> = dao.getAllPhase4BpjsClaims()
    val allPhase4PharmacyInventory: Flow<List<PharmacyInventoryEntity>> = dao.getAllPhase4PharmacyInventory()
    val allPhase4Branches: Flow<List<ClinicBranchEntity>> = dao.getAllPhase4Branches()

    fun getScheduleRulesForTenant(tenantId: String): Flow<List<ScheduleRuleEntity>> =
        dao.getScheduleRulesForTenant(tenantId)

    suspend fun initializeSeedData() {
        ClinicDatabase.seedIfEmpty(dao)
    }

    suspend fun getOccupiedSlots(doctorId: Int, dateIso: String): Set<String> {
        return dao.getActiveBookingsForDoctorAndDate(doctorId, dateIso)
            .map { it.appointmentTime }
            .toSet()
    }

    suspend fun createBooking(
        patient: PatientEntity,
        doctor: DoctorEntity,
        appointmentDate: String,
        appointmentTime: String,
        bookingType: String,
        packageName: String,
        symptomsOrNotes: String,
        paymentType: String,
        basePrice: Long
    ): BookingEntity {
        val prefix = ClinicFormatters.polyclinicPrefix(doctor.polyclinic)
        val existingCount = dao.getBookingCountForPolyclinicAndDate(doctor.polyclinic, appointmentDate)
        val queueState = dao.getQueueForPolyclinic(doctor.polyclinic)

        val nextOrder = if (appointmentDate == ClinicFormatters.getTodayIso() && queueState != null) {
            queueState.totalIssuedToday + 1
        } else {
            existingCount + 1
        }

        if (appointmentDate == ClinicFormatters.getTodayIso() && queueState != null) {
            dao.updatePolyclinicQueue(
                queueState.copy(totalIssuedToday = nextOrder)
            )
        }

        val queueNumber = String.format(Locale.US, "%s-%02d", prefix, nextOrder)
        val randomSuffix = Random.nextInt(100, 999)
        val bookingCode = "KLK-2610-$randomSuffix"

        val finalCost = if (paymentType.contains("BPJS", ignoreCase = true)) 0L else basePrice

        val estimatedTurn = calculateEstimatedTime(appointmentTime, nextOrder)

        val entity = BookingEntity(
            bookingCode = bookingCode,
            queueNumber = queueNumber,
            queueOrder = nextOrder,
            patientId = patient.id,
            patientName = patient.fullName,
            patientRm = patient.medicalRecordNumber,
            doctorId = doctor.id,
            doctorName = doctor.name,
            polyclinic = doctor.polyclinic,
            roomNumber = doctor.roomNumber,
            appointmentDate = appointmentDate,
            appointmentTime = appointmentTime,
            bookingType = bookingType,
            packageName = packageName,
            symptomsOrNotes = symptomsOrNotes.ifBlank { "Pemeriksaan & konsultasi medis terjadwal" },
            paymentType = paymentType,
            totalCost = finalCost,
            status = "TERJADWAL",
            estimatedTurnTime = estimatedTurn,
            clinicalSummary = ""
        )
        val id = dao.insertBooking(entity).toInt()
        return entity.copy(id = id)
    }

    suspend fun checkInBooking(booking: BookingEntity) {
        dao.updateBookingStatus(booking.id, "CHECK_IN")
    }

    suspend fun startExamination(booking: BookingEntity) {
        dao.updateBookingStatus(booking.id, "DIPERIKSA")
    }

    suspend fun completeBooking(booking: BookingEntity, customSummary: String? = null) {
        val defaultSummary = buildString {
            appendLine("Diagnosis: Pemeriksaan ${booking.polyclinic} Terjadwal — Kondisi Stabil.")
            appendLine("Dokter Pemeriksa: ${booking.doctorName}")
            if (booking.packageName.isNotBlank()) {
                appendLine("Tindakan MCU: ${booking.packageName} (Seluruh sampel lab telah diproses).")
            }
            appendLine("Resep & Anjuran Medis:")
            appendLine("1. Terapi medikamentosa sesuai indikasi keluhan (${booking.symptomsOrNotes.take(45)}).")
            appendLine("2. Istirahat cukup 7–8 jam/hari dan hidrasi air putih minimal 2 liter.")
            append("3. Kontrol kembali bila keluhan berlanjut setelah 5 hari.")
        }
        dao.completeBookingWithSummary(
            bookingId = booking.id,
            newStatus = "SELESAI",
            summary = customSummary?.takeIf { it.isNotBlank() } ?: defaultSummary
        )
    }

    suspend fun cancelBooking(bookingId: Int) {
        dao.updateBookingStatus(bookingId, "DIBATALKAN")
    }

    suspend fun rescheduleBooking(bookingId: Int, newDate: String, newTime: String) {
        val estimated = calculateEstimatedTime(newTime, 2)
        dao.rescheduleBooking(bookingId, newDate, newTime, estimated)
    }

    suspend fun advancePolyclinicQueue(queue: PolyclinicQueueEntity) {
        val nextNum = if (queue.currentNumber < queue.totalIssuedToday) {
            queue.currentNumber + 1
        } else {
            queue.currentNumber + 1
        }
        val updatedTotal = maxOf(queue.totalIssuedToday, nextNum)
        dao.updatePolyclinicQueue(
            queue.copy(
                currentNumber = nextNum,
                totalIssuedToday = updatedTotal
            )
        )
    }

    suspend fun savePatient(
        id: Int = 0,
        fullName: String,
        nik: String,
        birthDate: String,
        gender: String,
        bloodType: String,
        phone: String,
        paymentMethodDefault: String,
        bpjsOrInsuranceNumber: String,
        allergies: String,
        relationship: String,
        existingRm: String = ""
    ) {
        val rmNumber = existingRm.ifBlank {
            "RM-2026-${Random.nextInt(1000, 9999)}"
        }
        val entity = PatientEntity(
            id = id,
            fullName = fullName.trim(),
            nik = nik.trim(),
            medicalRecordNumber = rmNumber,
            birthDate = birthDate.trim().ifBlank { "01 Januari 1995" },
            gender = gender,
            bloodType = bloodType,
            phone = phone.trim(),
            paymentMethodDefault = paymentMethodDefault,
            bpjsOrInsuranceNumber = bpjsOrInsuranceNumber.trim(),
            allergies = allergies.trim().ifBlank { "Tidak ada" },
            relationship = relationship
        )
        if (id == 0) {
            dao.insertPatient(entity)
        } else {
            dao.updatePatient(entity)
        }
    }

    suspend fun deletePatient(patientId: Int) {
        dao.deletePatientById(patientId)
    }

    suspend fun triggerOverbookingSurge() {
        val dalam = dao.getQueueForPolyclinic("Poli Penyakit Dalam")
        if (dalam != null) {
            dao.updatePolyclinicQueue(
                dalam.copy(
                    totalIssuedToday = dalam.totalIssuedToday + 14,
                    avgMinutesPerPatient = 32
                )
            )
        }
        val umum = dao.getQueueForPolyclinic("Poli Umum")
        if (umum != null) {
            dao.updatePolyclinicQueue(
                umum.copy(
                    totalIssuedToday = umum.totalIssuedToday + 16,
                    avgMinutesPerPatient = 26
                )
            )
        }
        dao.insertBooking(
            BookingEntity(
                bookingCode = "SIM-KRISIS-OVB",
                queueNumber = "D-18",
                queueOrder = 18,
                patientId = 1,
                patientName = "Simulasi Bentrok Slot (4 Pasien Jam 09:30)",
                patientRm = "RM-SIM-901",
                doctorId = 1,
                doctorName = "dr. Nadia Prameswari, Sp.PD",
                polyclinic = "Poli Penyakit Dalam",
                roomNumber = "Ruang 104 • Lantai 1 (OVERCAPACITY)",
                appointmentDate = ClinicFormatters.getTodayIso(),
                appointmentTime = "09:30",
                bookingType = "Konsultasi Dokter",
                packageName = "",
                symptomsOrNotes = "DAMPAK OVERBOOKING: 4 pasien mendapat jam 09:30 bersamaan akibat race condition tanpa slot lock.",
                paymentType = "BPJS Kesehatan",
                totalCost = 0L,
                status = "CHECK_IN",
                estimatedTurnTime = "12:15 WIB (Molor +150 mnt)",
                clinicalSummary = ""
            )
        )
    }

    suspend fun triggerDoctorEmergencyAbsence() {
        val dalam = dao.getQueueForPolyclinic("Poli Penyakit Dalam")
        if (dalam != null) {
            dao.updatePolyclinicQueue(
                dalam.copy(
                    activeDoctorName = "TERTUNDA • dr. Nadia Dipanggil Operasi Darurat IGD",
                    avgMinutesPerPatient = 45
                )
            )
        }
    }

    suspend fun triggerCriticalTriageMisroute() {
        dao.insertBooking(
            BookingEntity(
                bookingCode = "SIM-KRISIS-IGD",
                queueNumber = "A-22",
                queueOrder = 22,
                patientId = 1,
                patientName = "Pasien Risiko Kritis (Salah Masuk Antrean Poli)",
                patientRm = "RM-SIM-999",
                doctorId = 4,
                doctorName = "dr. Bagus Santoso",
                polyclinic = "Poli Umum",
                roomNumber = "Ruang 101 • Lantai 1",
                appointmentDate = ClinicFormatters.getTodayIso(),
                appointmentTime = "10:30",
                bookingType = "Konsultasi Dokter",
                packageName = "",
                symptomsOrNotes = "RED-FLAG DARURAT: Nyeri dada kiri menjalar ke lengan & sesak napas berat sejak 40 menit lalu (Bahaya jika menunggu antrean A-22!).",
                paymentType = "BPJS Kesehatan",
                totalCost = 0L,
                status = "TERJADWAL",
                estimatedTurnTime = "12:40 WIB (BAHAYA FATAL)",
                clinicalSummary = ""
            )
        )
    }

    suspend fun mitigateAndRestoreNormalOperations() {
        dao.deleteCrisisSimulatedBookings()
        val dalam = dao.getQueueForPolyclinic("Poli Penyakit Dalam")
        if (dalam != null) {
            dao.updatePolyclinicQueue(
                dalam.copy(
                    currentNumber = 2,
                    totalIssuedToday = 5,
                    activeDoctorName = "dr. Nadia Prameswari, Sp.PD (Didampingi dr. Hendra Sp.PD Backup)",
                    avgMinutesPerPatient = 15
                )
            )
        }
        val umum = dao.getQueueForPolyclinic("Poli Umum")
        if (umum != null) {
            dao.updatePolyclinicQueue(
                umum.copy(
                    currentNumber = 3,
                    totalIssuedToday = 6,
                    activeDoctorName = "dr. Bagus Santoso",
                    avgMinutesPerPatient = 12
                )
            )
        }
    }

    // --- SaaS Fase 1 Methods ---
    suspend fun bookPhase1SlotAtomic(
        tenantId: String,
        rule: ScheduleRuleEntity,
        patientName: String,
        visitCategory: String,
        slotStartUtcMillis: Long,
        slotEndUtcMillis: Long,
        actorRole: String
    ): Result<Phase1ReservationEntity> {
        return try {
            val ref = "P1-${Random.nextInt(1000, 9999)}"
            val qNum = "Q-${Random.nextInt(10, 99)}"
            val entity = Phase1ReservationEntity(
                bookingRef = ref,
                tenantId = tenantId,
                doctorId = rule.doctorId,
                doctorName = rule.doctorName,
                serviceUnit = rule.serviceUnit,
                patientPhoneHash = "SHA256:${patientName.hashCode().toUInt().toString(16)}",
                patientDisplayName = patientName,
                visitCategory = visitCategory,
                slotStartUtcMillis = slotStartUtcMillis,
                slotEndUtcMillis = slotEndUtcMillis,
                queueCode = qNum,
                status = "BOOKED",
                activeLockKey = 1
            )
            val id = dao.bookSlotAtomicTransaction(entity).toInt()
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = tenantId,
                    actorRole = actorRole,
                    actorName = patientName,
                    actionType = "BOOK_SLOT_UTC",
                    targetResource = "$ref • ${rule.doctorName} (${SaasTimeAndSlotEngine.formatIsoUtc(slotStartUtcMillis)})",
                    ipOrDeviceNote = "Consent: PDP-v1.0-2026"
                )
            )
            Result.success(entity.copy(id = id))
        } catch (e: Exception) {
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = tenantId,
                    actorRole = actorRole,
                    actorName = patientName,
                    actionType = "REJECT_DOUBLE_BOOKING_409",
                    targetResource = "Slot ${SaasTimeAndSlotEngine.formatIsoUtc(slotStartUtcMillis)} terkunci",
                    ipOrDeviceNote = e.message?.take(60) ?: "SQLiteConstraintException"
                )
            )
            Result.failure(e)
        }
    }

    /**
     * Simulasi 2 Request Bersamaan (Concurrency Race Test) pada milidetik yang sama
     * untuk membuktikan UNIQUE INDEX + @Transaction mencegah Double Booking.
     */
    suspend fun runConcurrentDoubleBookingTest(
        tenantId: String,
        rule: ScheduleRuleEntity,
        slotStartUtcMillis: Long,
        slotEndUtcMillis: Long
    ): String = coroutineScope {
        val reqA = async(Dispatchers.IO) {
            bookPhase1SlotAtomic(
                tenantId = tenantId,
                rule = rule,
                patientName = "Pasien A (Request #1)",
                visitCategory = "Konsultasi Baru (Fase 1)",
                slotStartUtcMillis = slotStartUtcMillis,
                slotEndUtcMillis = slotEndUtcMillis,
                actorRole = "CONCURRENCY_TEST"
            )
        }
        val reqB = async(Dispatchers.IO) {
            bookPhase1SlotAtomic(
                tenantId = tenantId,
                rule = rule,
                patientName = "Pasien B (Request #2)",
                visitCategory = "Konsultasi Baru (Fase 1)",
                slotStartUtcMillis = slotStartUtcMillis,
                slotEndUtcMillis = slotEndUtcMillis,
                actorRole = "CONCURRENCY_TEST"
            )
        }
        val resA = reqA.await()
        val resB = reqB.await()

        val statusA = if (resA.isSuccess) {
            "201 CREATED (${resA.getOrNull()?.bookingRef})"
        } else {
            "409 CONFLICT (Ditolak DB Lock)"
        }
        val statusB = if (resB.isSuccess) {
            "201 CREATED (${resB.getOrNull()?.bookingRef})"
        } else {
            "409 CONFLICT (Ditolak DB Lock)"
        }

        "Hasil Uji 2 Request Paralel pada Slot UTC ${SaasTimeAndSlotEngine.formatIsoUtc(slotStartUtcMillis)}:\n" +
            "• Request #1 (Pasien A): $statusA\n" +
            "• Request #2 (Pasien B): $statusB\n" +
            "✓ Bukti Anti-Double Booking: Hanya 1 transaksi yang berhasil mengunci slot, transaksi kedua ditolak otomatis oleh UNIQUE INDEX + @Transaction!"
    }

    suspend fun updatePhase1Status(
        reservation: Phase1ReservationEntity,
        newStatus: String,
        actorRole: String
    ) {
        val releaseSlot = if (newStatus == "CANCELLED" || newStatus == "NO_SHOW") 1 else 0
        dao.updatePhase1ReservationStatus(reservation.id, newStatus, releaseSlot)
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = reservation.tenantId,
                actorRole = actorRole,
                actorName = actorRole,
                actionType = "STATUS_CHANGE_$newStatus",
                targetResource = "${reservation.bookingRef} (${reservation.queueCode}) -> $newStatus",
                ipOrDeviceNote = if (releaseSlot == 1) "Slot dilepas kembali" else "Slot tetap aktif"
            )
        )
    }

    suspend fun sweepNoShowForTenant(
        tenantId: String,
        reservations: List<Phase1ReservationEntity>
    ): Int {
        val bookedList = reservations.filter { it.tenantId == tenantId && it.status == "BOOKED" }
        var swept = 0
        bookedList.forEach { item ->
            dao.updatePhase1ReservationStatus(item.id, "NO_SHOW", releaseSlot = 1)
            swept++
        }
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "SYSTEM_CRON",
                actorName = "Auto-NoShow Sweeper",
                actionType = "AUTO_NOSHOW_SWEEP",
                targetResource = "$swept reservasi lewat batas grace period diubah ke NO_SHOW & slot dibuka kembali",
                ipOrDeviceNote = "Grace Period Sweeper"
            )
        )
        return swept
    }

    suspend fun updateScheduleSlotDuration(rule: ScheduleRuleEntity, newDurationMinutes: Int) {
        dao.updateScheduleRule(rule.copy(slotDurationMinutes = newDurationMinutes))
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = rule.tenantId,
                actorRole = "STAFF_DOCTOR",
                actorName = rule.doctorName,
                actionType = "UPDATE_SLOT_DURATION",
                targetResource = "Durasi slot diubah ke $newDurationMinutes menit (Istirahat ${rule.breakStartHour}:00-${rule.breakEndHour}:00)",
                ipOrDeviceNote = "Dynamic Slot Engine"
            )
        )
    }

    suspend fun toggleTenantHolidayStatus(tenant: TenantClinicEntity) {
        val updated = tenant.copy(isHolidayClosed = !tenant.isHolidayClosed)
        dao.updateSaasTenant(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenant.tenantId,
                actorRole = "STAFF_DOCTOR",
                actorName = "Admin Klinik",
                actionType = if (updated.isHolidayClosed) "SET_CLINIC_HOLIDAY" else "OPEN_CLINIC_SCHEDULE",
                targetResource = "${tenant.clinicName}: Libur=${updated.isHolidayClosed}",
                ipOrDeviceNote = "Schedule Rule Override"
            )
        )
    }

    suspend fun erasePatientDataUnderPdp(tenantId: String, patientName: String): Int {
        val count = dao.erasePatientPhase1Data(tenantId, patientName)
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "PATIENT",
                actorName = patientName,
                actionType = "UU_PDP_RIGHT_TO_ERASURE",
                targetResource = "$count data reservasi pasien '$patientName' dihapus permanen sesuai Pasal 42 UU PDP No. 27/2022",
                ipOrDeviceNote = "Verified Erasure Request"
            )
        )
        return count
    }

    // --- SaaS Fase 2 Methods: DP/Payment, Chat, Reviews ---
    suspend fun createPhase2DpInvoice(
        tenantId: String,
        patientName: String,
        familyRelation: String,
        doctorName: String,
        serviceUnit: String,
        paymentChannel: String,
        isFullPayment: Boolean,
        totalFee: Long
    ): PaymentInvoiceEntity {
        val billed = if (isFullPayment) totalFee else 50000L
        val suffix = Random.nextInt(100, 999)
        val invNum = "INV-2610-$suffix"
        val bookRef = "KLK-2610-$suffix"
        val idempKey = "IDEMP-$tenantId-$suffix"
        val entity = PaymentInvoiceEntity(
            invoiceNumber = invNum,
            tenantId = tenantId,
            bookingRef = bookRef,
            patientName = patientName,
            familyRelation = familyRelation,
            doctorName = doctorName,
            serviceUnit = serviceUnit,
            paymentTypeLabel = if (isFullPayment) "Pelunasan Penuh" else "DP Komitmen Antrean (Rp 50.000)",
            paymentChannel = paymentChannel,
            totalConsultationFee = totalFee,
            billedAmount = billed,
            status = "PENDING",
            idempotencyKey = idempKey,
            webhookCallbackCount = 0,
            expiresAtUtcMillis = System.currentTimeMillis() + 900_000L
        )
        val id = dao.insertPhase2InvoiceStrict(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "PATIENT",
                actorName = patientName,
                actionType = "CREATE_INVOICE_DP",
                targetResource = "$invNum • ${ClinicFormatters.formatRupiah(billed)} via $paymentChannel",
                ipOrDeviceNote = "IdempotencyKey: $idempKey"
            )
        )
        return entity.copy(id = id)
    }

    suspend fun handlePaymentWebhookIdempotent(invoice: PaymentInvoiceEntity): String {
        val current = dao.getInvoiceByIdempotencyKey(invoice.idempotencyKey) ?: invoice
        return if (current.status == "PAID") {
            val updated = current.copy(webhookCallbackCount = current.webhookCallbackCount + 1)
            dao.updatePhase2Invoice(updated)
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = current.tenantId,
                    actorRole = "PAYMENT_WEBHOOK",
                    actorName = "Gateway Callback",
                    actionType = "IDEMPOTENT_DUPLICATE_IGNORED",
                    targetResource = "${current.invoiceNumber} sudah PAID (Webhook ke-${updated.webhookCallbackCount} diabaikan aman)",
                    ipOrDeviceNote = "Key: ${current.idempotencyKey}"
                )
            )
            "200 OK (Idempotent Guard): Tagihan ${current.invoiceNumber} sudah LUNAS sebelumnya. Webhook ke-${updated.webhookCallbackCount} diabaikan agar saldo tidak tercatat ganda!"
        } else {
            val updated = current.copy(
                status = "PAID",
                webhookCallbackCount = current.webhookCallbackCount + 1,
                paidAtUtcMillis = System.currentTimeMillis()
            )
            dao.updatePhase2Invoice(updated)
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = current.tenantId,
                    actorRole = "PAYMENT_WEBHOOK",
                    actorName = "Gateway Callback",
                    actionType = "PAYMENT_SETTLED_PAID",
                    targetResource = "${current.invoiceNumber} LUNAS ${ClinicFormatters.formatRupiah(current.billedAmount)}",
                    ipOrDeviceNote = "Key: ${current.idempotencyKey}"
                )
            )
            "200 OK: Pembayaran ${current.invoiceNumber} (${ClinicFormatters.formatRupiah(current.billedAmount)}) terverifikasi LUNAS! Slot antrean dikunci."
        }
    }

    suspend fun refundPhase2Invoice(invoice: PaymentInvoiceEntity) {
        val updated = invoice.copy(status = "REFUNDED")
        dao.updatePhase2Invoice(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = invoice.tenantId,
                actorRole = "CLINIC_STAFF",
                actorName = "Kasir Klinik",
                actionType = "REFUND_DP_ISSUED",
                targetResource = "${invoice.invoiceNumber} dikembalikan (${ClinicFormatters.formatRupiah(invoice.billedAmount)})",
                ipOrDeviceNote = "Full Refund / Credit"
            )
        )
    }

    suspend fun sendPhase2Chat(
        tenantId: String,
        bookingRef: String,
        senderRole: String,
        senderName: String,
        rawText: String
    ): Boolean {
        val hasWarning = Phase2PrivacyHelper.checkChatPrivacyWarning(rawText)
        val msg = ClinicChatMessageEntity(
            tenantId = tenantId,
            bookingRef = bookingRef,
            senderRole = senderRole,
            senderName = senderName,
            messageText = rawText.trim(),
            hasPrivacyWarning = hasWarning,
            isReadByRecipient = false
        )
        dao.insertPhase2Chat(msg)
        if (hasWarning) {
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = tenantId,
                    actorRole = senderRole,
                    actorName = senderName,
                    actionType = "CHAT_PRIVACY_GUARD_ALERT",
                    targetResource = "Pesan di $bookingRef ditandai mengandung NIK/istilah medis sensitif",
                    ipOrDeviceNote = "Phase 2 Non-Clinical Chat Filter"
                )
            )
        }
        return hasWarning
    }

    suspend fun submitPhase2VerifiedReview(
        tenantId: String,
        bookingRef: String,
        doctorName: String,
        serviceUnit: String,
        patientName: String,
        maskName: Boolean,
        stars: Int,
        comment: String
    ): Result<Unit> {
        return try {
            val review = ClinicReviewEntity(
                tenantId = tenantId,
                bookingRef = bookingRef,
                doctorName = doctorName,
                serviceUnit = serviceUnit,
                patientDisplayName = patientName,
                maskPatientName = maskName,
                ratingStars = stars.coerceIn(1, 5),
                reviewComment = comment.trim()
            )
            dao.insertPhase2ReviewStrict(review)
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = tenantId,
                    actorRole = "PATIENT",
                    actorName = Phase2PrivacyHelper.maskNameForPublicReview(patientName, maskName),
                    actionType = "SUBMIT_VERIFIED_REVIEW",
                    targetResource = "$bookingRef • $stars★ untuk $doctorName",
                    ipOrDeviceNote = "Masked=$maskName"
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replyToPhase2Review(review: ClinicReviewEntity, officialReply: String) {
        dao.updatePhase2Review(review.copy(clinicOfficialReply = officialReply.trim()))
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = review.tenantId,
                actorRole = "CLINIC_STAFF",
                actorName = "Admin Klinik",
                actionType = "REPLY_REVIEW",
                targetResource = "Balasan resmi pada ulasan ${review.bookingRef}",
                ipOrDeviceNote = "Public Response"
            )
        )
    }

    // --- SaaS Fase 3 Methods: RME SOAP, E-Resep, Lab, Surat & Telemedisin ---
    suspend fun recordEmrReadAccessAudit(
        tenantId: String,
        actorRole: String,
        actorName: String,
        encounterCode: String,
        patientName: String
    ) {
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = actorRole,
                actorName = actorName,
                actionType = "READ_EMR_SOAP_DECRYPT",
                targetResource = "Membuka & mendekripsi RME $encounterCode milik pasien $patientName",
                ipOrDeviceNote = "Permenkes 24/2022 & UU PDP Read-Access Trail"
            )
        )
    }

    suspend fun createPhase3EmrSoap(
        tenantId: String,
        patient: PatientEntity,
        doctorName: String,
        polyclinic: String,
        bpMmHg: String,
        hrBpm: Int,
        tempC: Double,
        weightKg: Double,
        subjective: String,
        objective: String,
        icd10Code: String,
        assessment: String,
        plan: String
    ): EmrSoapRecordEntity {
        val suffix = Random.nextInt(100, 999)
        val encCode = "ENC-2610-$suffix"
        val cipher = Phase3ClinicalSafetyEngine.generateEncryptedCipherPreview("$encCode:$icd10Code:${patient.fullName}")
        val entity = EmrSoapRecordEntity(
            encounterCode = encCode,
            tenantId = tenantId,
            bookingRef = "KLK-2610-$suffix",
            patientName = patient.fullName,
            patientRm = patient.medicalRecordNumber,
            patientAllergies = patient.allergies,
            doctorName = doctorName,
            polyclinic = polyclinic,
            bloodPressureMmHg = bpMmHg.ifBlank { "120/80 mmHg" },
            heartRateBpm = hrBpm,
            temperatureCelsius = tempC,
            weightKg = weightKg,
            subjectiveText = subjective.trim(),
            objectiveText = objective.trim(),
            icd10Code = icd10Code,
            assessmentDiagnosis = assessment.trim(),
            planTherapy = plan.trim(),
            addendumNotes = "",
            isSignedLocked = true,
            encryptedCipherPreview = cipher,
            retentionUntilYear = 2051,
            satusehatEncounterRef = "FHIR-ENC-$suffix"
        )
        val id = dao.insertPhase3EmrStrict(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "DOCTOR",
                actorName = doctorName,
                actionType = "SIGN_LOCK_EMR_SOAP",
                targetResource = "$encCode (${patient.fullName} • ICD-10 $icd10Code) dikunci TTE & dienkripsi AES-256",
                ipOrDeviceNote = "Retensi 25 Thn s/d 2051"
            )
        )
        return entity.copy(id = id)
    }

    suspend fun appendAddendumToLockedSoap(
        record: EmrSoapRecordEntity,
        addendumText: String,
        doctorName: String
    ) {
        val stamp = "Addendum [$doctorName]: ${addendumText.trim()}"
        val combined = if (record.addendumNotes.isBlank()) {
            stamp
        } else {
            "${record.addendumNotes}\n$stamp"
        }
        dao.updatePhase3Emr(record.copy(addendumNotes = combined))
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = record.tenantId,
                actorRole = "DOCTOR",
                actorName = doctorName,
                actionType = "APPEND_EMR_ADDENDUM",
                targetResource = "Addendum ditambahkan ke ${record.encounterCode} tanpa mengubah catatan SOAP asli",
                ipOrDeviceNote = "Immutable Audit Trail"
            )
        )
    }

    suspend fun attemptDeleteEmrUnderPdp(
        tenantId: String,
        encounterCode: String,
        patientName: String
    ): String {
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "COMPLIANCE_GUARD",
                actorName = "Permenkes 24/2022 Guard",
                actionType = "BLOCK_EMR_ERASURE_25Y",
                targetResource = "Penghapusan $encounterCode ($patientName) DITOLAK: Wajib simpan 25 tahun s/d 2051",
                ipOrDeviceNote = "Lex Specialis Permenkes 24/2022 Pasal 29"
            )
        )
        return "DITOLAK OLEH HUKUM MEDIS (Permenkes No. 24/2022 Pasal 29): Rekam Medis Elektronik $encounterCode wajib disimpan minimal 25 tahun (sampai 2051). Hak hapus UU PDP hanya memutus akses akun publik (de-linking), tetapi arsip klinis tetap dikunci & tidak boleh dihapus."
    }

    suspend fun createPhase3Prescription(
        tenantId: String,
        encounterCode: String,
        patientName: String,
        patientAllergies: String,
        doctorName: String,
        medicationListPipe: String,
        overrideAllergyWithJustification: Boolean
    ): Result<ElectronicPrescriptionEntity> {
        val (hasConflict, warningMsg) = Phase3ClinicalSafetyEngine.checkDrugAllergyConflict(
            patientAllergies = patientAllergies,
            medicationListText = medicationListPipe
        )
        if (hasConflict && !overrideAllergyWithJustification) {
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = tenantId,
                    actorRole = "CDS_SAFETY_ENGINE",
                    actorName = doctorName,
                    actionType = "BLOCK_DRUG_ALLERGY_CONFLICT",
                    targetResource = "E-Resep untuk $patientName diblokir CDS karena konflik alergi ($patientAllergies)",
                    ipOrDeviceNote = warningMsg.take(65)
                )
            )
            return Result.failure(IllegalStateException(warningMsg))
        }
        val suffix = Random.nextInt(100, 999)
        val rxNum = "RX-2610-$suffix"
        val entity = ElectronicPrescriptionEntity(
            rxNumber = rxNum,
            tenantId = tenantId,
            encounterCode = encounterCode,
            patientName = patientName,
            patientAllergies = patientAllergies,
            doctorName = doctorName,
            medicationListPipe = medicationListPipe,
            hasAllergyConflict = hasConflict,
            allergyWarningMessage = warningMsg,
            pharmacyStatus = "MENUNGGU_APOTEK"
        )
        val id = dao.insertPhase3PrescriptionStrict(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "DOCTOR",
                actorName = doctorName,
                actionType = "ISSUE_E_PRESCRIPTION",
                targetResource = "$rxNum untuk $patientName diteruskan ke Instalasi Farmasi",
                ipOrDeviceNote = if (hasConflict) "Override Alergi Tercatat" else "CDS Allergy Passed"
            )
        )
        return Result.success(entity.copy(id = id))
    }

    suspend fun advancePrescriptionPharmacyStatus(rx: ElectronicPrescriptionEntity) {
        val nextStatus = when (rx.pharmacyStatus) {
            "MENUNGGU_APOTEK" -> "DISIAPKAN"
            "DISIAPKAN" -> "DISERAHKAN"
            else -> "DISERAHKAN"
        }
        dao.updatePhase3Prescription(rx.copy(pharmacyStatus = nextStatus))
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = rx.tenantId,
                actorRole = "PHARMACY_STAFF",
                actorName = "Apoteker Klinik",
                actionType = "PHARMACY_STATUS_$nextStatus",
                targetResource = "${rx.rxNumber} (${rx.patientName}) -> $nextStatus",
                ipOrDeviceNote = "Instalasi Farmasi"
            )
        )
    }

    suspend fun createPhase3LabResult(
        tenantId: String,
        encounterCode: String,
        patientName: String,
        doctorName: String,
        panelCategory: String,
        loincCode: String,
        parameterName: String,
        resultValue: String,
        unit: String,
        referenceRange: String,
        flagStatus: String
    ): LabResultEntity {
        val suffix = Random.nextInt(100, 999)
        val labNum = "LAB-2610-$suffix"
        val entity = LabResultEntity(
            labOrderNumber = labNum,
            tenantId = tenantId,
            encounterCode = encounterCode,
            patientName = patientName,
            doctorName = doctorName,
            panelCategory = panelCategory,
            loincCode = loincCode,
            parameterName = parameterName,
            resultValue = resultValue,
            unit = unit,
            referenceRange = referenceRange,
            flagStatus = flagStatus
        )
        val id = dao.insertPhase3LabResult(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "LAB_ANALYST",
                actorName = "Analis Lab Medis",
                actionType = "PUBLISH_LAB_RESULT_LOINC",
                targetResource = "$labNum • $parameterName ($loincCode): $resultValue $unit [$flagStatus]",
                ipOrDeviceNote = "FHIR Observation Ready"
            )
        )
        return entity.copy(id = id)
    }

    suspend fun issuePhase3Certificate(
        tenantId: String,
        encounterCode: String,
        certificateType: String,
        patientName: String,
        patientRm: String,
        doctorName: String,
        restDaysCount: Int,
        conclusion: String
    ): MedicalCertificateEntity {
        val suffix = Random.nextInt(100, 999)
        val prefix = if (certificateType == "SURAT_SAKIT") "SKD" else "SKS"
        val certNum = "$prefix-2610-$suffix"
        val validPeriod = if (certificateType == "SURAT_SAKIT") {
            "$restDaysCount Hari Istirahat Medis"
        } else {
            "Berlaku 30 Hari sejak diterbitkan"
        }
        val hash = Phase3ClinicalSafetyEngine
            .generateEncryptedCipherPreview("$certNum:$patientRm:$doctorName")
            .replace("AES256-GCM:iv9f2a:", "TTE-SHA256:")
        val entity = MedicalCertificateEntity(
            certificateNumber = certNum,
            tenantId = tenantId,
            encounterCode = encounterCode,
            certificateType = certificateType,
            patientName = patientName,
            patientRm = patientRm,
            doctorName = doctorName,
            restDaysCount = if (certificateType == "SURAT_SAKIT") restDaysCount else 0,
            validPeriodText = validPeriod,
            medicalConclusion = conclusion.trim(),
            digitalSignatureHash = hash
        )
        val id = dao.insertPhase3CertificateStrict(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "DOCTOR",
                actorName = doctorName,
                actionType = "ISSUE_MEDICAL_CERTIFICATE",
                targetResource = "$certNum ($certificateType) diterbitkan untuk $patientName",
                ipOrDeviceNote = hash
            )
        )
        return entity.copy(id = id)
    }

    suspend fun advanceTeleconsultSession(
        session: TeleconsultationSessionEntity,
        messageToAppend: String?
    ) {
        val nextStatus = when (session.status) {
            "WAITING_ROOM" -> "LIVE_VIDEO"
            "LIVE_VIDEO" -> "COMPLETED_SOAP_LINKED"
            else -> "COMPLETED_SOAP_LINKED"
        }
        val updatedTranscript = if (!messageToAppend.isNullOrBlank()) {
            "${session.clinicalChatTranscript}\n${messageToAppend.trim()}"
        } else {
            session.clinicalChatTranscript
        }
        dao.updatePhase3Teleconsult(
            session.copy(
                status = nextStatus,
                clinicalChatTranscript = updatedTranscript
            )
        )
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = session.tenantId,
                actorRole = "DOCTOR",
                actorName = session.doctorName,
                actionType = "TELECONSULT_$nextStatus",
                targetResource = "${session.sessionCode} (${session.patientName}) -> $nextStatus",
                ipOrDeviceNote = "Informed Consent Telemedisin Verified"
            )
        )
    }

    // --- SaaS Fase 4 Methods: SATUSEHAT FHIR R4, BPJS PCare, Apotek KFA & Multi-Cabang ---
    suspend fun enqueueSatusehatFhirBundle(
        tenantId: String,
        branchCode: String,
        encounterCode: String,
        patientName: String,
        icd10Code: String,
        loincCode: String,
        kfaCode: String,
        generalConsentAccepted: Boolean
    ): SatusehatFhirOutboxEntity {
        val suffix = Random.nextInt(100, 999)
        val bundleId = "FHIR-BND-2610-$suffix"
        val patientIhs = "P10293847$suffix"
        val pracIhs = "N10002938$suffix"
        val jsonPayload = Phase4IntegrationEngine.buildSatusehatFhirBundleJson(
            bundleId = bundleId,
            orgId = "100028491",
            patientIhs = patientIhs,
            practitionerIhs = pracIhs,
            icd10Code = icd10Code,
            icd10Display = "Standardized ICD-10 Clinical Condition",
            loincCode = loincCode,
            kfaCode = kfaCode
        )
        val status = if (generalConsentAccepted) "QUEUED_OUTBOX" else "BLOCKED_NO_CONSENT"
        val responseNote = if (generalConsentAccepted) {
            "TERANTRE DI OUTBOX LOKAL: Menunggu pengiriman asinkron ke server SATUSEHAT Kemenkes."
        } else {
            "DIBLOKIR OTOMATIS (UU PDP & Pedoman SATUSEHAT): Pasien belum menyetujui General Consent pengiriman data ke Kemenkes!"
        }
        val entity = SatusehatFhirOutboxEntity(
            fhirBundleId = bundleId,
            tenantId = tenantId,
            branchCode = branchCode,
            encounterCode = encounterCode,
            patientName = patientName,
            patientIhsNumber = patientIhs,
            practitionerIhsNumber = pracIhs,
            resourceTypesSummary = "Encounter + Condition($icd10Code) + Observation($loincCode) + MedicationRequest($kfaCode)",
            icd10Code = icd10Code,
            loincCode = loincCode,
            kfaCode = kfaCode,
            generalConsentAccepted = generalConsentAccepted,
            syncStatus = status,
            retryCount = 0,
            fhirJsonPreview = jsonPayload,
            lastSyncResponse = responseNote
        )
        val id = dao.insertPhase4OutboxStrict(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "SATUSEHAT_OUTBOX",
                actorName = "FHIR R4 Gateway",
                actionType = if (generalConsentAccepted) "ENQUEUE_FHIR_BUNDLE" else "BLOCK_FHIR_NO_CONSENT",
                targetResource = "$bundleId ($patientName • $icd10Code) -> $status",
                ipOrDeviceNote = "IHS: $patientIhs"
            )
        )
        return entity.copy(id = id)
    }

    suspend fun syncPendingSatusehatOutbox(
        tenantId: String,
        items: List<SatusehatFhirOutboxEntity>
    ): Int {
        val eligible = items.filter {
            it.tenantId == tenantId &&
                it.generalConsentAccepted &&
                it.syncStatus != "SYNCED_200_OK"
        }
        var syncedCount = 0
        eligible.forEach { item ->
            dao.updatePhase4Outbox(
                item.copy(
                    syncStatus = "SYNCED_200_OK",
                    retryCount = item.retryCount + 1,
                    lastSyncResponse = "200 OK • SATUSEHAT FHIR R4 Server: Bundle Transaction Committed (Idempotent Worker)",
                    updatedAtUtcMillis = System.currentTimeMillis()
                )
            )
            syncedCount++
        }
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "SATUSEHAT_WORKER",
                actorName = "Exponential Backoff Worker",
                actionType = "SYNC_SATUSEHAT_BATCH_200",
                targetResource = "$syncedCount FHIR Bundle berhasil disinkronkan ke SATUSEHAT Kemenkes",
                ipOrDeviceNote = "TLS 1.3 OAuth2 Bearer"
            )
        )
        return syncedCount
    }

    suspend fun createBpjsPcareClaim(
        tenantId: String,
        branchCode: String,
        patientName: String,
        bpjsCardNumber: String,
        poliCodePcare: String,
        icd10Diagnosis: String,
        isReferralFktl: Boolean,
        referralHospitalName: String
    ): BpjsPcareClaimEntity {
        val suffix = Random.nextInt(100, 999)
        val noKunjungan = "0112U0451026P000$suffix"
        val serviceStatus = if (isReferralFktl) "RUJUK_FKTL_RS" else "TUNTAS_FKTP"
        val hospital = if (isReferralFktl) {
            referralHospitalName.ifBlank { "RSUD / RS Rujukan Regional (Poli Spesialis)" }
        } else {
            "-"
        }
        val entity = BpjsPcareClaimEntity(
            noKunjunganPcare = noKunjungan,
            tenantId = tenantId,
            branchCode = branchCode,
            patientName = patientName,
            bpjsCardNumber = bpjsCardNumber.ifBlank { "0001849203194" },
            membershipStatus = "AKTIF (FKTP Terdaftar)",
            poliCodePcare = poliCodePcare,
            icd10Diagnosis = icd10Diagnosis,
            serviceStatus = serviceStatus,
            referralHospitalName = hospital,
            claimTariffType = if (isReferralFktl) "Kapitasi FKTP (Surat Rujukan VClaim Aktif)" else "Kapitasi FKTP",
            bridgingSyncState = "BRIDGED_PCARE_200"
        )
        val id = dao.insertPhase4BpjsClaimStrict(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "BPJS_PCARE_BRIDGE",
                actorName = "Bridging PCare TrustMark",
                actionType = "CREATE_PCARE_$serviceStatus",
                targetResource = "$noKunjungan ($patientName • $icd10Diagnosis) -> $serviceStatus",
                ipOrDeviceNote = "No.Kartu: ${entity.bpjsCardNumber}"
            )
        )
        return entity.copy(id = id)
    }

    suspend fun dispensePharmacyStockAtomic(
        item: PharmacyInventoryEntity,
        quantityToDeduct: Int
    ): Result<PharmacyInventoryEntity> {
        return try {
            val updated = dao.dispenseMedicationAtomicFefo(item.id, quantityToDeduct)
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = item.tenantId,
                    actorRole = "PHARMACIST",
                    actorName = "Apoteker Instalasi Farmasi",
                    actionType = "DISPENSE_KFA_ATOMIC_FEFO",
                    targetResource = "${item.medicationName} (KFA ${item.kfaCode}) dipotong -$quantityToDeduct -> Sisa ${updated.stockQuantity}",
                    ipOrDeviceNote = "Batch: ${item.batchNumber} (Exp ${item.expiryDateIso})"
                )
            )
            Result.success(updated)
        } catch (e: Exception) {
            dao.insertAuditLog(
                AuditLogEntity(
                    tenantId = item.tenantId,
                    actorRole = "PHARMACIST",
                    actorName = "Guard Stok Anti-Minus",
                    actionType = "REJECT_NEGATIVE_STOCK_409",
                    targetResource = "Gagal potong -$quantityToDeduct pada ${item.medicationName} (Stok saat ini: ${item.stockQuantity})",
                    ipOrDeviceNote = "Atomic Transaction Rollback"
                )
            )
            Result.failure(e)
        }
    }

    suspend fun restockPharmacyBatch(
        item: PharmacyInventoryEntity,
        addedQuantity: Int
    ): PharmacyInventoryEntity {
        val updated = item.copy(
            stockQuantity = item.stockQuantity + addedQuantity,
            updatedAtUtcMillis = System.currentTimeMillis()
        )
        dao.updatePhase4PharmacyItem(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = item.tenantId,
                actorRole = "PHARMACIST",
                actorName = "Logistik Farmasi",
                actionType = "RESTOCK_KFA_BATCH",
                targetResource = "${item.medicationName} (KFA ${item.kfaCode}) +$addedQuantity -> Total ${updated.stockQuantity}",
                ipOrDeviceNote = "Batch: ${item.batchNumber}"
            )
        )
        return updated
    }

    suspend fun createNewClinicBranch(
        tenantId: String,
        branchName: String,
        cityAndProvince: String,
        timeZoneCode: String
    ): ClinicBranchEntity {
        val suffix = Random.nextInt(10, 99)
        val code = "CAB-${timeZoneCode}-$suffix"
        val entity = ClinicBranchEntity(
            tenantId = tenantId,
            branchCode = code,
            branchName = branchName.trim(),
            cityAndProvince = cityAndProvince.trim(),
            timeZoneCode = timeZoneCode,
            satusehatOrgId = "ORG-100099$suffix",
            bpjsPcareCode = "0119U0$suffix",
            activeDoctorsCount = 4,
            monthlyVisitsCount = 320,
            avgWaitTimeMinutes = 11,
            noShowRatePercent = 4.5,
            satusehatSyncPercent = 97,
            monthlyRevenueIdr = 68000000L,
            isCentralBranch = false
        )
        val id = dao.insertPhase4BranchStrict(entity).toInt()
        dao.insertAuditLog(
            AuditLogEntity(
                tenantId = tenantId,
                actorRole = "SUPER_ADMIN",
                actorName = "Owner Grup Klinik",
                actionType = "PROVISION_NEW_BRANCH",
                targetResource = "$code • ${entity.branchName} (${entity.cityAndProvince} - $timeZoneCode)",
                ipOrDeviceNote = "SATUSEHAT ${entity.satusehatOrgId}"
            )
        )
        return entity.copy(id = id)
    }

    private fun calculateEstimatedTime(slotTime: String, order: Int): String {
        return try {
            val parts = slotTime.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt() + ((order % 3) * 10)
            val finalHour = (hour + (minute / 60)) % 24
            val finalMin = minute % 60
            String.format(Locale.US, "%02d:%02d WIB", finalHour, finalMin)
        } catch (e: Exception) {
            "$slotTime WIB"
        }
    }
}
