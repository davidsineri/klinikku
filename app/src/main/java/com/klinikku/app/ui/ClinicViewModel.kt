package com.klinikku.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.klinikku.app.data.AuditLogEntity
import com.klinikku.app.data.BookingEntity
import com.klinikku.app.data.BpjsPcareClaimEntity
import com.klinikku.app.data.ClinicBranchEntity
import com.klinikku.app.data.ClinicChatMessageEntity
import com.klinikku.app.data.ClinicDatabase
import com.klinikku.app.data.ClinicFormatters
import com.klinikku.app.data.ClinicRepository
import com.klinikku.app.data.ClinicReviewEntity
import com.klinikku.app.data.DateOption
import com.klinikku.app.data.DoctorEntity
import com.klinikku.app.data.ElectronicPrescriptionEntity
import com.klinikku.app.data.EmrSoapRecordEntity
import com.klinikku.app.data.IndonesianZone
import com.klinikku.app.data.LabResultEntity
import com.klinikku.app.data.McuPackageEntity
import com.klinikku.app.data.MedicalCertificateEntity
import com.klinikku.app.data.PatientEntity
import com.klinikku.app.data.PaymentInvoiceEntity
import com.klinikku.app.data.PharmacyInventoryEntity
import com.klinikku.app.data.Phase1ReservationEntity
import com.klinikku.app.data.PolyclinicQueueEntity
import com.klinikku.app.data.SatusehatFhirOutboxEntity
import com.klinikku.app.data.ScheduleRuleEntity
import com.klinikku.app.data.TeleconsultationSessionEntity
import com.klinikku.app.data.TenantClinicEntity
import com.klinikku.app.data.UserRole
import com.klinikku.app.ui.screens.CrisisScenarioId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ClinicTab(val route: String, val label: String) {
    HOME("home", "Beranda"),
    SAAS_PHASE1("saas_phase1", "Fase 1"),
    SAAS_PHASE2("saas_phase2", "Fase 2"),
    SAAS_PHASE3("saas_phase3", "Fase 3"),
    SAAS_PHASE4("saas_phase4", "Fase 4"),
    DOCTORS("doctors", "Dokter"),
    BOOKINGS("bookings", "Jadwal"),
    QUEUE_MCU("queue_mcu", "Antrean & Uji")
}

data class BookingWizardState(
    val isVisible: Boolean = false,
    val step: Int = 1, // 1: Pasien & Pembayaran, 2: Tanggal & Jam, 3: Keluhan & Konfirmasi
    val doctor: DoctorEntity? = null,
    val mcuPackage: McuPackageEntity? = null,
    val selectedPatientId: Int = 1,
    val paymentType: String = "BPJS Kesehatan",
    val selectedDateIso: String = ClinicFormatters.getTodayIso(),
    val selectedTimeSlot: String = "",
    val occupiedSlots: Set<String> = emptySet(),
    val symptomsOrNotes: String = ""
)

class ClinicViewModel(application: Application) : AndroidViewModel(application) {
    private val database = ClinicDatabase.getDatabase(application)
    private val repository = ClinicRepository(database.clinicDao())

    val doctors: StateFlow<List<DoctorEntity>> = repository.allDoctors.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val patients: StateFlow<List<PatientEntity>> = repository.allPatients.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookings: StateFlow<List<BookingEntity>> = repository.allBookings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val mcuPackages: StateFlow<List<McuPackageEntity>> = repository.allMcuPackages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val polyclinicQueues: StateFlow<List<PolyclinicQueueEntity>> =
        repository.allPolyclinicQueues.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentTab = MutableStateFlow(ClinicTab.HOME)
    val currentTab: StateFlow<ClinicTab> = _currentTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPolyclinic = MutableStateFlow("Semua Poli")
    val selectedPolyclinic: StateFlow<String> = _selectedPolyclinic.asStateFlow()

    private val _onlyBpjsDoctors = MutableStateFlow(false)
    val onlyBpjsDoctors: StateFlow<Boolean> = _onlyBpjsDoctors.asStateFlow()

    private val _bookingSubTab = MutableStateFlow(0) // 0: Aktif, 1: Selesai, 2: Dibatalkan
    val bookingSubTab: StateFlow<Int> = _bookingSubTab.asStateFlow()

    private val _queueMcuSubTab = MutableStateFlow(0) // 0: Monitor Antrean Live, 1: Paket MCU & Lab
    val queueMcuSubTab: StateFlow<Int> = _queueMcuSubTab.asStateFlow()

    private val _wizardState = MutableStateFlow(BookingWizardState())
    val wizardState: StateFlow<BookingWizardState> = _wizardState.asStateFlow()

    private val _selectedDoctorDetail = MutableStateFlow<DoctorEntity?>(null)
    val selectedDoctorDetail: StateFlow<DoctorEntity?> = _selectedDoctorDetail.asStateFlow()

    private val _activeTicketModal = MutableStateFlow<BookingEntity?>(null)
    val activeTicketModal: StateFlow<BookingEntity?> = _activeTicketModal.asStateFlow()

    private val _rescheduleTarget = MutableStateFlow<BookingEntity?>(null)
    val rescheduleTarget: StateFlow<BookingEntity?> = _rescheduleTarget.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _activeCrisisScenarios = MutableStateFlow<Set<CrisisScenarioId>>(emptySet())
    val activeCrisisScenarios: StateFlow<Set<CrisisScenarioId>> = _activeCrisisScenarios.asStateFlow()

    private val _lastMitigationLog = MutableStateFlow<String?>(null)
    val lastMitigationLog: StateFlow<String?> = _lastMitigationLog.asStateFlow()

    // --- SaaS Fase 1 Multi-Tenant & Role State ---
    val saasTenants: StateFlow<List<TenantClinicEntity>> = repository.allSaasTenants.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedTenantId = MutableStateFlow("klinik-jakarta-01")
    val selectedTenantId: StateFlow<String> = _selectedTenantId.asStateFlow()

    private val _selectedUserRole = MutableStateFlow(UserRole.PATIENT)
    val selectedUserRole: StateFlow<UserRole> = _selectedUserRole.asStateFlow()

    private val _selectedDisplayZone = MutableStateFlow(IndonesianZone.WIB)
    val selectedDisplayZone: StateFlow<IndonesianZone> = _selectedDisplayZone.asStateFlow()

    private val _concurrencyTestReport = MutableStateFlow<String?>(null)
    val concurrencyTestReport: StateFlow<String?> = _concurrencyTestReport.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val tenantScheduleRules: StateFlow<List<ScheduleRuleEntity>> = _selectedTenantId
        .flatMapLatest { tId -> repository.getScheduleRulesForTenant(tId) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase1Reservations: StateFlow<List<Phase1ReservationEntity>> =
        repository.allPhase1Reservations.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentAuditLogs: StateFlow<List<AuditLogEntity>> = repository.recentAuditLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPhase2Invoices: StateFlow<List<PaymentInvoiceEntity>> =
        repository.allPhase2Invoices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase2Chats: StateFlow<List<ClinicChatMessageEntity>> =
        repository.allPhase2Chats.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase2Reviews: StateFlow<List<ClinicReviewEntity>> =
        repository.allPhase2Reviews.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _phase2SubTab = MutableStateFlow(0) // 0: Pembayaran/DP, 1: Chat, 2: Ulasan, 3: Keluarga, 4: Laporan
    val phase2SubTab: StateFlow<Int> = _phase2SubTab.asStateFlow()

    private val _webhookTestBanner = MutableStateFlow<String?>(null)
    val webhookTestBanner: StateFlow<String?> = _webhookTestBanner.asStateFlow()

    // --- SaaS Fase 3 StateFlows ---
    val allPhase3EmrRecords: StateFlow<List<EmrSoapRecordEntity>> =
        repository.allPhase3EmrRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase3Prescriptions: StateFlow<List<ElectronicPrescriptionEntity>> =
        repository.allPhase3Prescriptions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase3LabResults: StateFlow<List<LabResultEntity>> =
        repository.allPhase3LabResults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase3Certificates: StateFlow<List<MedicalCertificateEntity>> =
        repository.allPhase3Certificates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase3Teleconsults: StateFlow<List<TeleconsultationSessionEntity>> =
        repository.allPhase3Teleconsults.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _phase3SubTab = MutableStateFlow(0) // 0: RME SOAP, 1: E-Resep, 2: Hasil Lab, 3: Surat Medis, 4: Telemedisin
    val phase3SubTab: StateFlow<Int> = _phase3SubTab.asStateFlow()

    private val _phase3ComplianceBanner = MutableStateFlow<String?>(null)
    val phase3ComplianceBanner: StateFlow<String?> = _phase3ComplianceBanner.asStateFlow()

    // --- SaaS Fase 4 StateFlows ---
    val allPhase4SatusehatOutbox: StateFlow<List<SatusehatFhirOutboxEntity>> =
        repository.allPhase4SatusehatOutbox.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase4BpjsClaims: StateFlow<List<BpjsPcareClaimEntity>> =
        repository.allPhase4BpjsClaims.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase4PharmacyInventory: StateFlow<List<PharmacyInventoryEntity>> =
        repository.allPhase4PharmacyInventory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allPhase4Branches: StateFlow<List<ClinicBranchEntity>> =
        repository.allPhase4Branches.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _phase4SubTab = MutableStateFlow(0) // 0: SATUSEHAT, 1: BPJS PCare, 2: Apotek KFA, 3: Multi-Cabang, 4: Analitik
    val phase4SubTab: StateFlow<Int> = _phase4SubTab.asStateFlow()

    private val _phase4StatusBanner = MutableStateFlow<String?>(null)
    val phase4StatusBanner: StateFlow<String?> = _phase4StatusBanner.asStateFlow()

    val upcomingDateOptions: List<DateOption> = ClinicFormatters.getUpcomingDateOptions(7)

    val filteredDoctors: StateFlow<List<DoctorEntity>> = combine(
        doctors,
        _searchQuery,
        _selectedPolyclinic,
        _onlyBpjsDoctors
    ) { allDocs, query, poly, bpjsOnly ->
        allDocs.filter { doc ->
            val matchesPoly = poly == "Semua Poli" || doc.polyclinic.equals(poly, ignoreCase = true)
            val matchesBpjs = !bpjsOnly || doc.acceptsBpjs
            val q = query.trim()
            val matchesQuery = q.isEmpty() ||
                doc.name.contains(q, ignoreCase = true) ||
                doc.polyclinic.contains(q, ignoreCase = true) ||
                doc.specialtyTitle.contains(q, ignoreCase = true) ||
                doc.bio.contains(q, ignoreCase = true)
            matchesPoly && matchesBpjs && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeSeedData()
        }
    }

    fun selectTab(tab: ClinicTab) {
        _currentTab.value = tab
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectPolyclinicFilter(polyclinic: String, navigateToDoctors: Boolean = false) {
        _selectedPolyclinic.value = polyclinic
        if (navigateToDoctors) {
            _currentTab.value = ClinicTab.DOCTORS
        }
    }

    fun toggleBpjsOnlyFilter() {
        _onlyBpjsDoctors.value = !_onlyBpjsDoctors.value
    }

    fun setBookingSubTab(index: Int) {
        _bookingSubTab.value = index
    }

    fun setQueueMcuSubTab(index: Int) {
        _queueMcuSubTab.value = index
    }

    fun openDoctorDetail(doctor: DoctorEntity?) {
        _selectedDoctorDetail.value = doctor
    }

    fun openTicketModal(booking: BookingEntity?) {
        _activeTicketModal.value = booking
    }

    fun openRescheduleModal(booking: BookingEntity?) {
        _rescheduleTarget.value = booking
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // Booking Wizard Operations
    fun startBookingForDoctor(doctor: DoctorEntity, mcuPackage: McuPackageEntity? = null) {
        val defaultPatient = patients.value.firstOrNull()
        val defaultDate = ClinicFormatters.getTodayIso()
        val slots = doctor.timeSlots.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val defaultPayment = if (mcuPackage != null) {
            "Umum / Mandiri"
        } else if (doctor.acceptsBpjs && defaultPatient?.paymentMethodDefault == "BPJS Kesehatan") {
            "BPJS Kesehatan"
        } else {
            defaultPatient?.paymentMethodDefault ?: "Umum / Mandiri"
        }

        viewModelScope.launch {
            val occupied = repository.getOccupiedSlots(doctor.id, defaultDate)
            val firstAvailableSlot = slots.firstOrNull { !occupied.contains(it) } ?: slots.firstOrNull().orEmpty()
            _wizardState.value = BookingWizardState(
                isVisible = true,
                step = 1,
                doctor = doctor,
                mcuPackage = mcuPackage,
                selectedPatientId = defaultPatient?.id ?: 1,
                paymentType = defaultPayment,
                selectedDateIso = defaultDate,
                selectedTimeSlot = firstAvailableSlot,
                occupiedSlots = occupied,
                symptomsOrNotes = if (mcuPackage != null) {
                    "Pemeriksaan ${mcuPackage.title}"
                } else {
                    ""
                }
            )
        }
    }

    fun startBookingForMcu(mcuPackage: McuPackageEntity) {
        val assignedDoc = doctors.value.find { it.id == mcuPackage.assignedDoctorId }
            ?: doctors.value.firstOrNull()
            ?: return
        startBookingForDoctor(assignedDoc, mcuPackage)
    }

    fun startQuickBooking() {
        val firstDoc = filteredDoctors.value.firstOrNull() ?: doctors.value.firstOrNull() ?: return
        startBookingForDoctor(firstDoc, null)
    }

    fun closeBookingWizard() {
        _wizardState.value = BookingWizardState(isVisible = false)
    }

    fun setWizardStep(step: Int) {
        _wizardState.value = _wizardState.value.copy(step = step.coerceIn(1, 3))
    }

    fun selectWizardDoctor(doctor: DoctorEntity) {
        val current = _wizardState.value
        viewModelScope.launch {
            val occupied = repository.getOccupiedSlots(doctor.id, current.selectedDateIso)
            val slots = doctor.timeSlots.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val nextSlot = slots.firstOrNull { !occupied.contains(it) } ?: slots.firstOrNull().orEmpty()
            val adjustedPayment = if (!doctor.acceptsBpjs && current.paymentType == "BPJS Kesehatan") {
                "Umum / Mandiri"
            } else {
                current.paymentType
            }
            _wizardState.value = current.copy(
                doctor = doctor,
                occupiedSlots = occupied,
                selectedTimeSlot = nextSlot,
                paymentType = adjustedPayment
            )
        }
    }

    fun selectWizardPatient(patient: PatientEntity) {
        val current = _wizardState.value
        val docAcceptsBpjs = current.doctor?.acceptsBpjs ?: true
        val newPayment = if (!docAcceptsBpjs && patient.paymentMethodDefault == "BPJS Kesehatan") {
            "Umum / Mandiri"
        } else if (current.mcuPackage != null && patient.paymentMethodDefault == "BPJS Kesehatan") {
            "Umum / Mandiri"
        } else {
            patient.paymentMethodDefault
        }
        _wizardState.value = current.copy(
            selectedPatientId = patient.id,
            paymentType = newPayment
        )
    }

    fun selectWizardPaymentType(paymentType: String) {
        _wizardState.value = _wizardState.value.copy(paymentType = paymentType)
    }

    fun selectWizardDate(dateIso: String) {
        val current = _wizardState.value
        val doc = current.doctor ?: return
        viewModelScope.launch {
            val occupied = repository.getOccupiedSlots(doc.id, dateIso)
            val slots = doc.timeSlots.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val validSlot = if (current.selectedTimeSlot in slots && !occupied.contains(current.selectedTimeSlot)) {
                current.selectedTimeSlot
            } else {
                slots.firstOrNull { !occupied.contains(it) } ?: slots.firstOrNull().orEmpty()
            }
            _wizardState.value = current.copy(
                selectedDateIso = dateIso,
                occupiedSlots = occupied,
                selectedTimeSlot = validSlot
            )
        }
    }

    fun selectWizardTimeSlot(slot: String) {
        _wizardState.value = _wizardState.value.copy(selectedTimeSlot = slot)
    }

    fun updateWizardSymptoms(notes: String) {
        _wizardState.value = _wizardState.value.copy(symptomsOrNotes = notes)
    }

    fun appendWizardSymptomTag(tag: String) {
        val currentText = _wizardState.value.symptomsOrNotes.trim()
        val updated = if (currentText.isEmpty()) {
            tag
        } else if (currentText.contains(tag, ignoreCase = true)) {
            currentText
        } else {
            "$currentText, $tag"
        }
        _wizardState.value = _wizardState.value.copy(symptomsOrNotes = updated)
    }

    fun submitBooking() {
        val wiz = _wizardState.value
        val doctor = wiz.doctor ?: return
        val patient = patients.value.find { it.id == wiz.selectedPatientId }
            ?: patients.value.firstOrNull()
            ?: return
        val mcu = wiz.mcuPackage
        val baseCost = mcu?.price ?: doctor.consultationFee
        val bookingType = if (mcu != null) "Medical Check-Up" else "Konsultasi Dokter"

        viewModelScope.launch {
            val created = repository.createBooking(
                patient = patient,
                doctor = doctor,
                appointmentDate = wiz.selectedDateIso,
                appointmentTime = wiz.selectedTimeSlot.ifBlank { "09:00" },
                bookingType = bookingType,
                packageName = mcu?.title.orEmpty(),
                symptomsOrNotes = wiz.symptomsOrNotes,
                paymentType = wiz.paymentType,
                basePrice = baseCost
            )
            _wizardState.value = BookingWizardState(isVisible = false)
            _bookingSubTab.value = 0
            _currentTab.value = ClinicTab.BOOKINGS
            _activeTicketModal.value = created
            _snackbarMessage.value =
                "Reservasi ${created.bookingCode} berhasil! Nomor Antrean: ${created.queueNumber}"
        }
    }

    // Booking lifecycle actions
    fun checkInBooking(booking: BookingEntity) {
        viewModelScope.launch {
            repository.checkInBooking(booking)
            if (_activeTicketModal.value?.id == booking.id) {
                _activeTicketModal.value = booking.copy(status = "CHECK_IN")
            }
            _snackbarMessage.value =
                "Check-in berhasil untuk antrean ${booking.queueNumber} (${booking.polyclinic})."
        }
    }

    fun advanceBookingState(booking: BookingEntity) {
        viewModelScope.launch {
            when (booking.status) {
                "TERJADWAL" -> {
                    repository.checkInBooking(booking)
                    _snackbarMessage.value = "Status diubah ke CHECK-IN. Silakan tunggu di depan ${booking.roomNumber}."
                }
                "CHECK_IN" -> {
                    repository.startExamination(booking)
                    _snackbarMessage.value = "Pasien ${booking.patientName} (${booking.queueNumber}) sedang diperiksa oleh ${booking.doctorName}."
                }
                "DIPERIKSA" -> {
                    repository.completeBooking(booking)
                    _snackbarMessage.value = "Pemeriksaan selesai! Ringkasan medis & resep telah disimpan di Riwayat."
                }
            }
        }
    }

    fun cancelBooking(booking: BookingEntity) {
        viewModelScope.launch {
            repository.cancelBooking(booking.id)
            if (_activeTicketModal.value?.id == booking.id) {
                _activeTicketModal.value = null
            }
            _snackbarMessage.value = "Reservasi ${booking.bookingCode} (${booking.queueNumber}) telah dibatalkan."
        }
    }

    fun confirmReschedule(booking: BookingEntity, newDateIso: String, newTimeSlot: String) {
        viewModelScope.launch {
            repository.rescheduleBooking(booking.id, newDateIso, newTimeSlot)
            _rescheduleTarget.value = null
            _snackbarMessage.value =
                "Jadwal ${booking.bookingCode} berhasil diubah ke ${ClinicFormatters.formatIsoDateReadable(newDateIso)} pukul $newTimeSlot WIB."
        }
    }

    // Live Polyclinic Queue actions
    fun advancePolyclinicQueue(queue: PolyclinicQueueEntity) {
        viewModelScope.launch {
            repository.advancePolyclinicQueue(queue)
            val nextCall = String.format("%s-%02d", queue.prefixCode, queue.currentNumber + 1)
            // Also check if any checked-in booking matches this queue number today
            val matchingBooking = bookings.value.find {
                it.polyclinic == queue.polyclinic &&
                    it.queueNumber == nextCall &&
                    (it.status == "CHECK_IN" || it.status == "TERJADWAL")
            }
            if (matchingBooking != null) {
                repository.startExamination(matchingBooking)
                _snackbarMessage.value =
                    "Panggilan Antrean $nextCall (${matchingBooking.patientName}) silakan masuk ke ${queue.roomNumber}!"
            } else {
                _snackbarMessage.value =
                    "Antrean ${queue.polyclinic} maju ke nomor $nextCall."
            }
        }
    }

    // Patient CRUD
    fun savePatient(
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
        viewModelScope.launch {
            repository.savePatient(
                id = id,
                fullName = fullName,
                nik = nik,
                birthDate = birthDate,
                gender = gender,
                bloodType = bloodType,
                phone = phone,
                paymentMethodDefault = paymentMethodDefault,
                bpjsOrInsuranceNumber = bpjsOrInsuranceNumber,
                allergies = allergies,
                relationship = relationship,
                existingRm = existingRm
            )
            _snackbarMessage.value = if (id == 0) {
                "Profil pasien $fullName berhasil ditambahkan."
            } else {
                "Profil pasien $fullName berhasil diperbarui."
            }
        }
    }

    fun deletePatient(patient: PatientEntity) {
        if (patients.value.size <= 1) {
            _snackbarMessage.value = "Minimal harus terdapat 1 profil pasien terdaftar."
            return
        }
        viewModelScope.launch {
            repository.deletePatient(patient.id)
            _snackbarMessage.value = "Profil pasien ${patient.fullName} dihapus."
        }
    }

    fun navigateToCrisisSimulator() {
        _queueMcuSubTab.value = 2
        _currentTab.value = ClinicTab.QUEUE_MCU
    }

    fun toggleCrisisScenario(scenarioId: CrisisScenarioId) {
        val current = _activeCrisisScenarios.value.toMutableSet()
        val isActivating = !current.contains(scenarioId)
        if (isActivating) {
            current.add(scenarioId)
            _activeCrisisScenarios.value = current
            _lastMitigationLog.value = null
            viewModelScope.launch {
                when (scenarioId) {
                    CrisisScenarioId.OVERBOOKING_SURGE -> {
                        repository.triggerOverbookingSurge()
                        _snackbarMessage.value =
                            "SIMULASI AKTIF: Overbooking terjadi! +30 antrean menumpuk di Poli Dalam & Umum."
                    }
                    CrisisScenarioId.DOCTOR_EMERGENCY_ABSENCE -> {
                        repository.triggerDoctorEmergencyAbsence()
                        _snackbarMessage.value =
                            "SIMULASI AKTIF: dr. Nadia Sp.PD dipanggil darurat ke IGD! Antrean Poli Penyakit Dalam tertunda."
                    }
                    CrisisScenarioId.MASS_NO_SHOW_GHOST -> {
                        _snackbarMessage.value =
                            "SIMULASI AKTIF: 65% slot terkunci oleh Ghost Booking (No-Show). Kuota penuh semu!"
                    }
                    CrisisScenarioId.BPJS_GATEWAY_DOWNTIME -> {
                        _snackbarMessage.value =
                            "SIMULASI AKTIF: Server verifikasi BPJS P-Care mengalami Timeout! Antrean Check-In melambat."
                    }
                    CrisisScenarioId.CRITICAL_TRIAGE_MISROUTE -> {
                        repository.triggerCriticalTriageMisroute()
                        _snackbarMessage.value =
                            "SIMULASI AKTIF: Pasien gejala nyeri dada kritis masuk antrean reguler A-22!"
                    }
                }
            }
        } else {
            current.remove(scenarioId)
            _activeCrisisScenarios.value = current
            if (current.isEmpty()) {
                mitigateAllCrisisScenarios()
            } else {
                _snackbarMessage.value = "Skenario dimitigasi sebagian. Tekan 'Mitigasi & Pulihkan' untuk normalisasi penuh."
            }
        }
    }

    fun triggerAllCrisisScenarios() {
        _activeCrisisScenarios.value = CrisisScenarioId.entries.toSet()
        _lastMitigationLog.value = null
        viewModelScope.launch {
            repository.triggerOverbookingSurge()
            repository.triggerDoctorEmergencyAbsence()
            repository.triggerCriticalTriageMisroute()
            _snackbarMessage.value =
                "SIAGA MERAH: Seluruh 5 skenario dampak terburuk klinik diaktifkan bersamaan!"
        }
    }

    fun mitigateAllCrisisScenarios() {
        _activeCrisisScenarios.value = emptySet()
        viewModelScope.launch {
            repository.mitigateAndRestoreNormalOperations()
            _lastMitigationLog.value =
                "✓ Atomic Slot-Lock diaktifkan & konflik jadwal SIM-KRISIS dibersihkan.\n" +
                    "✓ Dokter Spesialis Pengganti (Backup On-Call) diaktifkan untuk Poli Penyakit Dalam.\n" +
                    "✓ Slot Ghost Booking / No-Show >15 menit otomatis dilepas ke pasien Waitlist.\n" +
                    "✓ Mode Offline-First Room Cache mengambil alih antrean BPJS tanpa hambatan.\n" +
                    "✓ Pasien Red-Flag (Nyeri Dada) dievakuasi langsung ke IGD 24 Jam."
            _snackbarMessage.value =
                "Protokol mitigasi berhasil dijalankan! Beban antrean & jadwal poliklinik kembali normal."
        }
    }

    // --- SaaS Fase 1 Actions ---
    fun selectTenant(tenant: TenantClinicEntity) {
        _selectedTenantId.value = tenant.tenantId
        val defaultZone = IndonesianZone.entries.find { it.code == tenant.defaultZoneCode } ?: IndonesianZone.WIB
        _selectedDisplayZone.value = defaultZone
    }

    fun selectUserRole(role: UserRole) {
        _selectedUserRole.value = role
    }

    fun selectDisplayZone(zone: IndonesianZone) {
        _selectedDisplayZone.value = zone
    }

    fun bookPhase1Slot(
        rule: ScheduleRuleEntity,
        patientName: String,
        visitCategory: String,
        slotStartUtcMillis: Long,
        slotEndUtcMillis: Long
    ) {
        val tId = _selectedTenantId.value
        val role = _selectedUserRole.value.code
        viewModelScope.launch {
            val result = repository.bookPhase1SlotAtomic(
                tenantId = tId,
                rule = rule,
                patientName = patientName.ifBlank { "Budi Raharjo" },
                visitCategory = visitCategory,
                slotStartUtcMillis = slotStartUtcMillis,
                slotEndUtcMillis = slotEndUtcMillis,
                actorRole = role
            )
            result.onSuccess { res ->
                _snackbarMessage.value =
                    "Reservasi Fase 1 Berhasil (${res.bookingRef} • Antrean ${res.queueCode}). Disimpan dalam UTC!"
            }.onFailure { err ->
                _snackbarMessage.value =
                    "DITOLAK (Anti-Double Booking): ${err.message}"
            }
        }
    }

    fun triggerConcurrencyDoubleBookingRace(
        rule: ScheduleRuleEntity,
        slotStartUtcMillis: Long,
        slotEndUtcMillis: Long
    ) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val report = repository.runConcurrentDoubleBookingTest(
                tenantId = tId,
                rule = rule,
                slotStartUtcMillis = slotStartUtcMillis,
                slotEndUtcMillis = slotEndUtcMillis
            )
            _concurrencyTestReport.value = report
            _snackbarMessage.value =
                "Uji 2 Request Bersamaan selesai! Cek hasil 201 Created vs 409 Conflict."
        }
    }

    fun updatePhase1ReservationStatus(reservation: Phase1ReservationEntity, newStatus: String) {
        val role = _selectedUserRole.value.code
        viewModelScope.launch {
            repository.updatePhase1Status(reservation, newStatus, role)
            _snackbarMessage.value =
                "Status ${reservation.bookingRef} diubah ke $newStatus."
        }
    }

    fun runNoShowSweeper() {
        val tId = _selectedTenantId.value
        val list = allPhase1Reservations.value
        viewModelScope.launch {
            val count = repository.sweepNoShowForTenant(tId, list)
            _snackbarMessage.value =
                "Sweeper No-Show selesai: $count reservasi lewat batas waktu diubah ke NO_SHOW & slot dibuka kembali."
        }
    }

    fun updateDoctorSlotDuration(rule: ScheduleRuleEntity, minutes: Int) {
        viewModelScope.launch {
            repository.updateScheduleSlotDuration(rule, minutes)
            _snackbarMessage.value =
                "Durasi slot ${rule.doctorName} diperbarui menjadi $minutes menit/pasien."
        }
    }

    fun toggleClinicHoliday(tenant: TenantClinicEntity) {
        viewModelScope.launch {
            repository.toggleTenantHolidayStatus(tenant)
            _snackbarMessage.value =
                "Status operasional ${tenant.clinicName} diperbarui."
        }
    }

    fun executePdpRightToErasure(patientName: String) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val erased = repository.erasePatientDataUnderPdp(tId, patientName)
            _snackbarMessage.value =
                "UU PDP Pasal 42: $erased rekam reservasi atas nama '$patientName' dihapus & tercatat di Audit Log."
        }
    }

    // --- SaaS Fase 2 Actions ---
    fun setPhase2SubTab(index: Int) {
        _phase2SubTab.value = index.coerceIn(0, 4)
    }

    fun navigateToPhase2Module(subTabIndex: Int = 0) {
        _phase2SubTab.value = subTabIndex.coerceIn(0, 4)
        _currentTab.value = ClinicTab.SAAS_PHASE2
    }

    fun createPhase2Invoice(
        patient: PatientEntity,
        doctorName: String,
        serviceUnit: String,
        paymentChannel: String,
        isFullPayment: Boolean,
        totalFee: Long
    ) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val inv = repository.createPhase2DpInvoice(
                tenantId = tId,
                patientName = patient.fullName,
                familyRelation = patient.relationship,
                doctorName = doctorName,
                serviceUnit = serviceUnit,
                paymentChannel = paymentChannel,
                isFullPayment = isFullPayment,
                totalFee = totalFee
            )
            _webhookTestBanner.value =
                "Tagihan ${inv.invoiceNumber} dibuat (${ClinicFormatters.formatRupiah(inv.billedAmount)} via ${inv.paymentChannel}) • IdempotencyKey: ${inv.idempotencyKey}"
            _snackbarMessage.value =
                "Tagihan DP/Reservasi ${inv.invoiceNumber} berhasil diterbitkan!"
        }
    }

    fun triggerWebhookCallback(invoice: PaymentInvoiceEntity) {
        viewModelScope.launch {
            val resultMsg = repository.handlePaymentWebhookIdempotent(invoice)
            _webhookTestBanner.value = resultMsg
            _snackbarMessage.value = resultMsg
        }
    }

    fun refundInvoice(invoice: PaymentInvoiceEntity) {
        viewModelScope.launch {
            repository.refundPhase2Invoice(invoice)
            _webhookTestBanner.value =
                "Tagihan ${invoice.invoiceNumber} telah di-refund (${ClinicFormatters.formatRupiah(invoice.billedAmount)}) ke saldo pasien."
            _snackbarMessage.value =
                "Refund DP ${invoice.invoiceNumber} berhasil diproses."
        }
    }

    fun sendClinicChatMessage(bookingRef: String, senderRole: String, senderName: String, message: String) {
        if (message.isBlank()) return
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val flagged = repository.sendPhase2Chat(
                tenantId = tId,
                bookingRef = bookingRef,
                senderRole = senderRole,
                senderName = senderName,
                rawText = message
            )
            if (flagged) {
                _snackbarMessage.value =
                    "Peringatan Privasi Fase 2: Pesan terdeteksi memuat NIK / istilah diagnosis klinis (sebaiknya gunakan modul RME Fase 3)."
            }
        }
    }

    fun submitVerifiedReview(
        bookingRef: String,
        doctorName: String,
        serviceUnit: String,
        patientName: String,
        maskName: Boolean,
        stars: Int,
        comment: String
    ) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val res = repository.submitPhase2VerifiedReview(
                tenantId = tId,
                bookingRef = bookingRef,
                doctorName = doctorName,
                serviceUnit = serviceUnit,
                patientName = patientName,
                maskName = maskName,
                stars = stars,
                comment = comment
            )
            res.onSuccess {
                _snackbarMessage.value =
                    "Ulasan terverifikasi ($stars★) berhasil dikirim! Terima kasih atas masukannya."
            }.onFailure {
                _snackbarMessage.value =
                    "DITOLAK: Kode kunjungan $bookingRef sudah pernah memberi ulasan (1 kunjungan = maks 1 ulasan)."
            }
        }
    }

    fun replyToReview(review: ClinicReviewEntity, officialReply: String) {
        if (officialReply.isBlank()) return
        viewModelScope.launch {
            repository.replyToPhase2Review(review, officialReply)
            _snackbarMessage.value =
                "Balasan resmi klinik pada ulasan ${review.bookingRef} berhasil disimpan."
        }
    }

    // --- SaaS Fase 3 Actions ---
    fun setPhase3SubTab(index: Int) {
        _phase3SubTab.value = index.coerceIn(0, 4)
    }

    fun navigateToPhase3Module(subTabIndex: Int = 0) {
        _phase3SubTab.value = subTabIndex.coerceIn(0, 4)
        _currentTab.value = ClinicTab.SAAS_PHASE3
    }

    fun logEmrReadAccess(record: EmrSoapRecordEntity) {
        val role = _selectedUserRole.value
        viewModelScope.launch {
            repository.recordEmrReadAccessAudit(
                tenantId = record.tenantId,
                actorRole = role.code,
                actorName = role.label,
                encounterCode = record.encounterCode,
                patientName = record.patientName
            )
            _phase3ComplianceBanner.value =
                "AUDIT LOG TERCATAT (UU PDP): Akses baca & dekripsi RME ${record.encounterCode} (${record.patientName}) oleh peran ${role.label} terekam permanen."
            _snackbarMessage.value =
                "Audit Log Akses Medis direkam untuk ${record.encounterCode}."
        }
    }

    fun createPhase3SoapRecord(
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
    ) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val created = repository.createPhase3EmrSoap(
                tenantId = tId,
                patient = patient,
                doctorName = doctorName,
                polyclinic = polyclinic,
                bpMmHg = bpMmHg,
                hrBpm = hrBpm,
                tempC = tempC,
                weightKg = weightKg,
                subjective = subjective,
                objective = objective,
                icd10Code = icd10Code,
                assessment = assessment,
                plan = plan
            )
            _phase3ComplianceBanner.value =
                "RME SOAP ${created.encounterCode} ditandatangani secara elektronik (TTE), dienkripsi (${created.encryptedCipherPreview}), & dikunci hingga tahun ${created.retentionUntilYear}."
            _snackbarMessage.value =
                "Rekam Medis ${created.encounterCode} (${created.icd10Code}) berhasil disimpan!"
        }
    }

    fun appendSoapAddendum(record: EmrSoapRecordEntity, addendumText: String) {
        if (addendumText.isBlank()) return
        viewModelScope.launch {
            repository.appendAddendumToLockedSoap(
                record = record,
                addendumText = addendumText,
                doctorName = record.doctorName
            )
            _phase3ComplianceBanner.value =
                "Addendum berhasil ditambahkan ke ${record.encounterCode} tanpa mengubah catatan SOAP asli (Immutability Permenkes 24/2022 terjaga)."
            _snackbarMessage.value =
                "Addendum ditambahkan pada ${record.encounterCode}."
        }
    }

    fun testAttemptDeleteEmr25YearGuard(record: EmrSoapRecordEntity) {
        viewModelScope.launch {
            val legalMsg = repository.attemptDeleteEmrUnderPdp(
                tenantId = record.tenantId,
                encounterCode = record.encounterCode,
                patientName = record.patientName
            )
            _phase3ComplianceBanner.value = legalMsg
            _snackbarMessage.value =
                "DITOLAK: RME ${record.encounterCode} dilindungi masa retensi wajib 25 tahun (Permenkes 24/2022)!"
        }
    }

    fun createPhase3Prescription(
        encounterCode: String,
        patient: PatientEntity,
        doctorName: String,
        medicationListPipe: String,
        overrideAllergy: Boolean
    ) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val res = repository.createPhase3Prescription(
                tenantId = tId,
                encounterCode = encounterCode,
                patientName = patient.fullName,
                patientAllergies = patient.allergies,
                doctorName = doctorName,
                medicationListPipe = medicationListPipe,
                overrideAllergyWithJustification = overrideAllergy
            )
            res.onSuccess { rx ->
                _phase3ComplianceBanner.value =
                    "E-Resep ${rx.rxNumber} diterbitkan untuk ${rx.patientName}. Status CDS: ${rx.allergyWarningMessage}"
                _snackbarMessage.value =
                    "E-Resep ${rx.rxNumber} berhasil dikirim ke Instalasi Farmasi!"
            }.onFailure { err ->
                _phase3ComplianceBanner.value = err.message
                _snackbarMessage.value = err.message ?: "Konflik alergi obat terdeteksi!"
            }
        }
    }

    fun advancePrescriptionStatus(rx: ElectronicPrescriptionEntity) {
        viewModelScope.launch {
            repository.advancePrescriptionPharmacyStatus(rx)
            _snackbarMessage.value =
                "Status farmasi ${rx.rxNumber} diperbarui."
        }
    }

    fun createPhase3LabResult(
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
    ) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val lab = repository.createPhase3LabResult(
                tenantId = tId,
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
            _phase3ComplianceBanner.value =
                "Hasil Lab ${lab.labOrderNumber} (${lab.parameterName} • LOINC ${lab.loincCode}) dipublikasikan dengan status [${lab.flagStatus}]."
            _snackbarMessage.value =
                "Hasil Lab ${lab.labOrderNumber} berhasil ditambahkan!"
        }
    }

    fun issuePhase3MedicalCertificate(
        encounterCode: String,
        certificateType: String,
        patient: PatientEntity,
        doctorName: String,
        restDaysCount: Int,
        conclusion: String
    ) {
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val cert = repository.issuePhase3Certificate(
                tenantId = tId,
                encounterCode = encounterCode,
                certificateType = certificateType,
                patientName = patient.fullName,
                patientRm = patient.medicalRecordNumber,
                doctorName = doctorName,
                restDaysCount = restDaysCount,
                conclusion = conclusion
            )
            _phase3ComplianceBanner.value =
                "Surat Medis ${cert.certificateNumber} (${cert.certificateType}) diterbitkan dengan Tanda Tangan Elektronik (${cert.digitalSignatureHash})."
            _snackbarMessage.value =
                "Surat Keterangan ${cert.certificateNumber} berhasil diterbitkan!"
        }
    }

    fun advanceTeleconsult(session: TeleconsultationSessionEntity, messageToAppend: String?) {
        viewModelScope.launch {
            repository.advanceTeleconsultSession(session, messageToAppend)
            _snackbarMessage.value =
                "Sesi Telemedisin ${session.sessionCode} diperbarui."
        }
    }

    // --- SaaS Fase 4 Actions: SATUSEHAT, BPJS PCare, Apotek KFA & Multi-Cabang ---
    fun setPhase4SubTab(index: Int) {
        _phase4SubTab.value = index.coerceIn(0, 4)
    }

    fun navigateToPhase4Module(subTabIndex: Int = 0) {
        _phase4SubTab.value = subTabIndex.coerceIn(0, 4)
        _currentTab.value = ClinicTab.SAAS_PHASE4
    }

    fun enqueueSatusehatFhirBundle(
        encounterCode: String,
        patientName: String,
        icd10Code: String,
        loincCode: String,
        kfaCode: String,
        generalConsentAccepted: Boolean
    ) {
        val tId = _selectedTenantId.value
        val defaultBranch = allPhase4Branches.value.firstOrNull { it.tenantId == tId }?.branchCode ?: "CAB-JKT-SEL"
        viewModelScope.launch {
            val outbox = repository.enqueueSatusehatFhirBundle(
                tenantId = tId,
                branchCode = defaultBranch,
                encounterCode = encounterCode,
                patientName = patientName,
                icd10Code = icd10Code,
                loincCode = loincCode,
                kfaCode = kfaCode,
                generalConsentAccepted = generalConsentAccepted
            )
            _phase4StatusBanner.value =
                "${outbox.fhirBundleId} (${outbox.syncStatus}): ${outbox.lastSyncResponse}"
            _snackbarMessage.value = if (generalConsentAccepted) {
                "FHIR R4 Bundle ${outbox.fhirBundleId} masuk antrean Outbox SATUSEHAT!"
            } else {
                "DIBLOKIR: Pengiriman ke SATUSEHAT ditolak karena General Consent belum disetujui pasien."
            }
        }
    }

    fun syncAllPendingSatusehatOutbox() {
        val tId = _selectedTenantId.value
        val currentList = allPhase4SatusehatOutbox.value
        viewModelScope.launch {
            val count = repository.syncPendingSatusehatOutbox(tId, currentList)
            _phase4StatusBanner.value =
                "SATUSEHAT Outbox Worker Selesai: $count FHIR R4 Bundle berhasil mendapatkan 200 OK dari server Kemenkes."
            _snackbarMessage.value =
                "$count Bundle FHIR R4 berhasil disinkronkan ke SATUSEHAT!"
        }
    }

    fun createBpjsPcareClaim(
        patientName: String,
        bpjsCardNumber: String,
        poliCodePcare: String,
        icd10Diagnosis: String,
        isReferralFktl: Boolean,
        referralHospitalName: String
    ) {
        val tId = _selectedTenantId.value
        val defaultBranch = allPhase4Branches.value.firstOrNull { it.tenantId == tId }?.branchCode ?: "CAB-JKT-SEL"
        viewModelScope.launch {
            val claim = repository.createBpjsPcareClaim(
                tenantId = tId,
                branchCode = defaultBranch,
                patientName = patientName,
                bpjsCardNumber = bpjsCardNumber,
                poliCodePcare = poliCodePcare,
                icd10Diagnosis = icd10Diagnosis,
                isReferralFktl = isReferralFktl,
                referralHospitalName = referralHospitalName
            )
            _phase4StatusBanner.value =
                "Bridging PCare BPJS Berhasil: No. Kunjungan ${claim.noKunjunganPcare} (${claim.serviceStatus} • ${claim.claimTariffType})"
            _snackbarMessage.value =
                "Kunjungan PCare ${claim.noKunjunganPcare} berhasil diterbitkan!"
        }
    }

    fun dispensePharmacyStock(item: PharmacyInventoryEntity, qty: Int) {
        viewModelScope.launch {
            val res = repository.dispensePharmacyStockAtomic(item, qty)
            res.onSuccess { updated ->
                val lowAlert = if (updated.stockQuantity <= updated.minReorderThreshold) {
                    " [PERINGATAN: Sisa stok (${updated.stockQuantity}) di bawah batas minimum (${updated.minReorderThreshold})!]"
                } else {
                    ""
                }
                _phase4StatusBanner.value =
                    "Transaksi FEFO Sukses: ${updated.medicationName} (KFA ${updated.kfaCode}) dipotong -$qty. Sisa stok: ${updated.stockQuantity}.$lowAlert"
                _snackbarMessage.value =
                    "Stok ${updated.medicationName} dipotong -$qty (Sisa: ${updated.stockQuantity})."
            }.onFailure { err ->
                _phase4StatusBanner.value = err.message
                _snackbarMessage.value = err.message ?: "Gagal memotong stok apotek!"
            }
        }
    }

    fun restockPharmacyBatch(item: PharmacyInventoryEntity, addedQty: Int) {
        viewModelScope.launch {
            val updated = repository.restockPharmacyBatch(item, addedQty)
            _phase4StatusBanner.value =
                "Restock Berhasil: ${updated.medicationName} (Batch ${updated.batchNumber}) bertambah +$addedQty -> Total stok: ${updated.stockQuantity}."
            _snackbarMessage.value =
                "Stok ${updated.medicationName} ditambah +$addedQty."
        }
    }

    fun createNewClinicBranch(
        branchName: String,
        cityAndProvince: String,
        timeZoneCode: String
    ) {
        if (branchName.isBlank()) return
        val tId = _selectedTenantId.value
        viewModelScope.launch {
            val branch = repository.createNewClinicBranch(
                tenantId = tId,
                branchName = branchName,
                cityAndProvince = cityAndProvince,
                timeZoneCode = timeZoneCode
            )
            _phase4StatusBanner.value =
                "Cabang Baru Aktif: ${branch.branchName} (${branch.branchCode} • Zona ${branch.timeZoneCode}) terhubung ke ${branch.satusehatOrgId}."
            _snackbarMessage.value =
                "Cabang ${branch.branchName} berhasil ditambahkan!"
        }
    }
}
