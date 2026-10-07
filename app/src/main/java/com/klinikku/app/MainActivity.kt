package com.klinikku.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MedicalServices
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.klinikku.app.ui.ClinicTab
import com.klinikku.app.ui.ClinicViewModel
import com.klinikku.app.ui.components.BookingWizardSheet
import com.klinikku.app.ui.components.DoctorDetailBottomSheet
import com.klinikku.app.ui.components.RescheduleBookingDialog
import com.klinikku.app.ui.components.TicketQrCodeDialog
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.outlined.CloudSync
import com.klinikku.app.ui.screens.BookingsScreen
import com.klinikku.app.ui.screens.DoctorsScreen
import com.klinikku.app.ui.screens.HomeScreen
import com.klinikku.app.ui.screens.PatientsScreen
import com.klinikku.app.ui.screens.QueueAndMcuScreen
import com.klinikku.app.ui.screens.SaasPhase1Screen
import com.klinikku.app.ui.screens.SaasPhase2Screen
import com.klinikku.app.ui.screens.SaasPhase3Screen
import com.klinikku.app.ui.screens.SaasPhase4Screen
import com.klinikku.app.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                KlinikKuApp()
            }
        }
    }
}

@Composable
fun KlinikKuApp(viewModel: ClinicViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val doctors by viewModel.doctors.collectAsStateWithLifecycle()
    val filteredDoctors by viewModel.filteredDoctors.collectAsStateWithLifecycle()
    val patients by viewModel.patients.collectAsStateWithLifecycle()
    val bookings by viewModel.bookings.collectAsStateWithLifecycle()
    val mcuPackages by viewModel.mcuPackages.collectAsStateWithLifecycle()
    val polyclinicQueues by viewModel.polyclinicQueues.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedPolyclinic by viewModel.selectedPolyclinic.collectAsStateWithLifecycle()
    val onlyBpjs by viewModel.onlyBpjsDoctors.collectAsStateWithLifecycle()
    val bookingSubTab by viewModel.bookingSubTab.collectAsStateWithLifecycle()
    val queueMcuSubTab by viewModel.queueMcuSubTab.collectAsStateWithLifecycle()

    val wizardState by viewModel.wizardState.collectAsStateWithLifecycle()
    val selectedDoctorDetail by viewModel.selectedDoctorDetail.collectAsStateWithLifecycle()
    val activeTicketModal by viewModel.activeTicketModal.collectAsStateWithLifecycle()
    val rescheduleTarget by viewModel.rescheduleTarget.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val activeCrisisScenarios by viewModel.activeCrisisScenarios.collectAsStateWithLifecycle()
    val lastMitigationLog by viewModel.lastMitigationLog.collectAsStateWithLifecycle()

    val saasTenants by viewModel.saasTenants.collectAsStateWithLifecycle()
    val selectedTenantId by viewModel.selectedTenantId.collectAsStateWithLifecycle()
    val selectedUserRole by viewModel.selectedUserRole.collectAsStateWithLifecycle()
    val selectedDisplayZone by viewModel.selectedDisplayZone.collectAsStateWithLifecycle()
    val tenantScheduleRules by viewModel.tenantScheduleRules.collectAsStateWithLifecycle()
    val allPhase1Reservations by viewModel.allPhase1Reservations.collectAsStateWithLifecycle()
    val recentAuditLogs by viewModel.recentAuditLogs.collectAsStateWithLifecycle()
    val concurrencyTestReport by viewModel.concurrencyTestReport.collectAsStateWithLifecycle()

    val allPhase2Invoices by viewModel.allPhase2Invoices.collectAsStateWithLifecycle()
    val allPhase2Chats by viewModel.allPhase2Chats.collectAsStateWithLifecycle()
    val allPhase2Reviews by viewModel.allPhase2Reviews.collectAsStateWithLifecycle()
    val phase2SubTab by viewModel.phase2SubTab.collectAsStateWithLifecycle()
    val webhookTestBanner by viewModel.webhookTestBanner.collectAsStateWithLifecycle()

    val allPhase3EmrRecords by viewModel.allPhase3EmrRecords.collectAsStateWithLifecycle()
    val allPhase3Prescriptions by viewModel.allPhase3Prescriptions.collectAsStateWithLifecycle()
    val allPhase3LabResults by viewModel.allPhase3LabResults.collectAsStateWithLifecycle()
    val allPhase3Certificates by viewModel.allPhase3Certificates.collectAsStateWithLifecycle()
    val allPhase3Teleconsults by viewModel.allPhase3Teleconsults.collectAsStateWithLifecycle()
    val phase3SubTab by viewModel.phase3SubTab.collectAsStateWithLifecycle()
    val phase3ComplianceBanner by viewModel.phase3ComplianceBanner.collectAsStateWithLifecycle()

    val allPhase4SatusehatOutbox by viewModel.allPhase4SatusehatOutbox.collectAsStateWithLifecycle()
    val allPhase4BpjsClaims by viewModel.allPhase4BpjsClaims.collectAsStateWithLifecycle()
    val allPhase4PharmacyInventory by viewModel.allPhase4PharmacyInventory.collectAsStateWithLifecycle()
    val allPhase4Branches by viewModel.allPhase4Branches.collectAsStateWithLifecycle()
    val phase4SubTab by viewModel.phase4SubTab.collectAsStateWithLifecycle()
    val phase4StatusBanner by viewModel.phase4StatusBanner.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val activeBookingsCount = remember(bookings) {
        bookings.count {
            it.status == "TERJADWAL" || it.status == "CHECK_IN" || it.status == "DIPERIKSA"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            if (currentTab == ClinicTab.HOME || currentTab == ClinicTab.DOCTORS || currentTab == ClinicTab.BOOKINGS) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.startQuickBooking() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Buat Reservasi Baru"
                        )
                    },
                    text = {
                        Text(
                            text = "Reservasi",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.testTag("fab_new_booking")
                )
            }
        },
        bottomBar = {
            NavigationBar {
                val tabs = listOf(
                    Triple(ClinicTab.HOME, Icons.Default.Home, Icons.Outlined.Home),
                    Triple(ClinicTab.SAAS_PHASE1, Icons.Default.Groups, Icons.Outlined.Groups),
                    Triple(ClinicTab.SAAS_PHASE2, Icons.Default.Payments, Icons.Outlined.Payments),
                    Triple(ClinicTab.SAAS_PHASE3, Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
                    Triple(ClinicTab.SAAS_PHASE4, Icons.Default.CloudSync, Icons.Outlined.CloudSync)
                )
                tabs.forEach { (tab, filledIcon, outlinedIcon) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(tab) },
                        icon = {
                            if (tab == ClinicTab.BOOKINGS && activeBookingsCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(activeBookingsCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) filledIcon else outlinedIcon,
                                        contentDescription = tab.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) filledIcon else outlinedIcon,
                                    contentDescription = tab.label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        },
                        modifier = Modifier.testTag("nav_tab_${tab.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ClinicTab.HOME -> {
                    HomeScreen(
                        doctors = doctors,
                        bookings = bookings,
                        queues = polyclinicQueues,
                        mcuPackages = mcuPackages,
                        onQuickBooking = { viewModel.startQuickBooking() },
                        onSelectPolyclinic = { poly ->
                            viewModel.selectPolyclinicFilter(poly, navigateToDoctors = true)
                        },
                        onOpenDoctorDetail = { doc -> viewModel.openDoctorDetail(doc) },
                        onBookDoctor = { doc -> viewModel.startBookingForDoctor(doc) },
                        onOpenTicket = { booking -> viewModel.openTicketModal(booking) },
                        onAdvanceBookingState = { booking -> viewModel.advanceBookingState(booking) },
                        onNavigateToBookings = { viewModel.selectTab(ClinicTab.BOOKINGS) },
                        onNavigateToMcu = {
                            viewModel.setQueueMcuSubTab(1)
                            viewModel.selectTab(ClinicTab.QUEUE_MCU)
                        },
                        onBookMcuPackage = { pkg -> viewModel.startBookingForMcu(pkg) },
                        activeCrisisCount = activeCrisisScenarios.size,
                        onNavigateToCrisisSimulator = { viewModel.navigateToCrisisSimulator() },
                        onNavigateToPhase2 = { subIdx -> viewModel.navigateToPhase2Module(subIdx) },
                        onNavigateToPhase3 = { subIdx -> viewModel.navigateToPhase3Module(subIdx) },
                        onNavigateToPhase4 = { subIdx -> viewModel.navigateToPhase4Module(subIdx) }
                    )
                }

                ClinicTab.SAAS_PHASE4 -> {
                    SaasPhase4Screen(
                        tenants = saasTenants,
                        selectedTenantId = selectedTenantId,
                        selectedRole = selectedUserRole,
                        selectedSubTab = phase4SubTab,
                        satusehatOutbox = allPhase4SatusehatOutbox,
                        bpjsClaims = allPhase4BpjsClaims,
                        pharmacyInventory = allPhase4PharmacyInventory,
                        branches = allPhase4Branches,
                        patients = patients,
                        statusBanner = phase4StatusBanner,
                        onSelectTenant = { viewModel.selectTenant(it) },
                        onSelectRole = { viewModel.selectUserRole(it) },
                        onSelectSubTab = { viewModel.setPhase4SubTab(it) },
                        onEnqueueSatusehatBundle = { encCode, pName, icd10, loinc, kfa, consent ->
                            viewModel.enqueueSatusehatFhirBundle(
                                encounterCode = encCode,
                                patientName = pName,
                                icd10Code = icd10,
                                loincCode = loinc,
                                kfaCode = kfa,
                                generalConsentAccepted = consent
                            )
                        },
                        onSyncAllSatusehatOutbox = {
                            viewModel.syncAllPendingSatusehatOutbox()
                        },
                        onCreateBpjsClaim = { pName, cardNum, poli, icd10, isRef, hospital ->
                            viewModel.createBpjsPcareClaim(
                                patientName = pName,
                                bpjsCardNumber = cardNum,
                                poliCodePcare = poli,
                                icd10Diagnosis = icd10,
                                isReferralFktl = isRef,
                                referralHospitalName = hospital
                            )
                        },
                        onDispensePharmacyStock = { item, qty ->
                            viewModel.dispensePharmacyStock(item, qty)
                        },
                        onRestockPharmacyBatch = { item, addedQty ->
                            viewModel.restockPharmacyBatch(item, addedQty)
                        },
                        onCreateNewBranch = { bName, cityProv, zone ->
                            viewModel.createNewClinicBranch(bName, cityProv, zone)
                        },
                        onNavigateBackHome = { viewModel.selectTab(ClinicTab.HOME) }
                    )
                }

                ClinicTab.SAAS_PHASE3 -> {
                    SaasPhase3Screen(
                        tenants = saasTenants,
                        selectedTenantId = selectedTenantId,
                        selectedRole = selectedUserRole,
                        selectedSubTab = phase3SubTab,
                        emrRecords = allPhase3EmrRecords,
                        prescriptions = allPhase3Prescriptions,
                        labResults = allPhase3LabResults,
                        certificates = allPhase3Certificates,
                        teleconsults = allPhase3Teleconsults,
                        patients = patients,
                        doctors = doctors,
                        auditLogs = recentAuditLogs,
                        complianceBanner = phase3ComplianceBanner,
                        onSelectTenant = { viewModel.selectTenant(it) },
                        onSelectRole = { viewModel.selectUserRole(it) },
                        onSelectSubTab = { viewModel.setPhase3SubTab(it) },
                        onLogEmrReadAccess = { viewModel.logEmrReadAccess(it) },
                        onCreateSoapRecord = { patient, docName, poly, bp, hr, temp, weight, subj, obj, icd10, assess, plan ->
                            viewModel.createPhase3SoapRecord(
                                patient = patient,
                                doctorName = docName,
                                polyclinic = poly,
                                bpMmHg = bp,
                                hrBpm = hr,
                                tempC = temp,
                                weightKg = weight,
                                subjective = subj,
                                objective = obj,
                                icd10Code = icd10,
                                assessment = assess,
                                plan = plan
                            )
                        },
                        onAppendSoapAddendum = { rec, addendum ->
                            viewModel.appendSoapAddendum(rec, addendum)
                        },
                        onTestAttemptDeleteEmr25YearGuard = { rec ->
                            viewModel.testAttemptDeleteEmr25YearGuard(rec)
                        },
                        onCreatePrescription = { encCode, patient, docName, medsPipe, overrideAllergy ->
                            viewModel.createPhase3Prescription(
                                encounterCode = encCode,
                                patient = patient,
                                doctorName = docName,
                                medicationListPipe = medsPipe,
                                overrideAllergy = overrideAllergy
                            )
                        },
                        onAdvancePrescriptionStatus = { rx ->
                            viewModel.advancePrescriptionStatus(rx)
                        },
                        onCreateLabResult = { encCode, pName, dName, panel, loinc, param, resVal, unit, refRange, flag ->
                            viewModel.createPhase3LabResult(
                                encounterCode = encCode,
                                patientName = pName,
                                doctorName = dName,
                                panelCategory = panel,
                                loincCode = loinc,
                                parameterName = param,
                                resultValue = resVal,
                                unit = unit,
                                referenceRange = refRange,
                                flagStatus = flag
                            )
                        },
                        onIssueMedicalCertificate = { encCode, certType, patient, docName, restDays, conclusion ->
                            viewModel.issuePhase3MedicalCertificate(
                                encounterCode = encCode,
                                certificateType = certType,
                                patient = patient,
                                doctorName = docName,
                                restDaysCount = restDays,
                                conclusion = conclusion
                            )
                        },
                        onAdvanceTeleconsult = { session, note ->
                            viewModel.advanceTeleconsult(session, note)
                        },
                        onNavigateBackHome = { viewModel.selectTab(ClinicTab.HOME) }
                    )
                }

                ClinicTab.SAAS_PHASE2 -> {
                    SaasPhase2Screen(
                        tenants = saasTenants,
                        selectedTenantId = selectedTenantId,
                        selectedRole = selectedUserRole,
                        selectedSubTab = phase2SubTab,
                        invoices = allPhase2Invoices,
                        chats = allPhase2Chats,
                        reviews = allPhase2Reviews,
                        patients = patients,
                        doctors = doctors,
                        webhookBanner = webhookTestBanner,
                        onSelectTenant = { viewModel.selectTenant(it) },
                        onSelectRole = { viewModel.selectUserRole(it) },
                        onSelectSubTab = { viewModel.setPhase2SubTab(it) },
                        onCreateInvoice = { patient, docName, unit, channel, isFull, fee ->
                            viewModel.createPhase2Invoice(patient, docName, unit, channel, isFull, fee)
                        },
                        onTriggerWebhook = { viewModel.triggerWebhookCallback(it) },
                        onRefundInvoice = { viewModel.refundInvoice(it) },
                        onSendChat = { ref, role, name, msg ->
                            viewModel.sendClinicChatMessage(ref, role, name, msg)
                        },
                        onSubmitReview = { ref, docName, unit, pName, mask, stars, comment ->
                            viewModel.submitVerifiedReview(ref, docName, unit, pName, mask, stars, comment)
                        },
                        onReplyReview = { rev, reply ->
                            viewModel.replyToReview(rev, reply)
                        },
                        onSavePatientDependent = { id, fullName, nik, birthDate, gender, bloodType, phone, payment, bpjsNum, allergies, relation, rm ->
                            viewModel.savePatient(
                                id = id,
                                fullName = fullName,
                                nik = nik,
                                birthDate = birthDate,
                                gender = gender,
                                bloodType = bloodType,
                                phone = phone,
                                paymentMethodDefault = payment,
                                bpjsOrInsuranceNumber = bpjsNum,
                                allergies = allergies,
                                relationship = relation,
                                existingRm = rm
                            )
                        },
                        onDeletePatientDependent = { viewModel.deletePatient(it) },
                        onNavigateBackHome = { viewModel.selectTab(ClinicTab.HOME) }
                    )
                }

                ClinicTab.DOCTORS -> {
                    DoctorsScreen(
                        doctors = filteredDoctors,
                        searchQuery = searchQuery,
                        selectedPolyclinic = selectedPolyclinic,
                        onlyBpjs = onlyBpjs,
                        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                        onSelectPolyclinic = { viewModel.selectPolyclinicFilter(it) },
                        onToggleBpjs = { viewModel.toggleBpjsOnlyFilter() },
                        onOpenDoctorDetail = { viewModel.openDoctorDetail(it) },
                        onBookDoctor = { viewModel.startBookingForDoctor(it) },
                        onNavigateBackHome = { viewModel.selectTab(ClinicTab.HOME) }
                    )
                }

                ClinicTab.BOOKINGS -> {
                    BookingsScreen(
                        bookings = bookings,
                        selectedSubTab = bookingSubTab,
                        onSelectSubTab = { viewModel.setBookingSubTab(it) },
                        onOpenTicket = { viewModel.openTicketModal(it) },
                        onAdvanceBookingState = { viewModel.advanceBookingState(it) },
                        onOpenReschedule = { viewModel.openRescheduleModal(it) },
                        onCancelBooking = { viewModel.cancelBooking(it) },
                        onCreateNewBooking = { viewModel.startQuickBooking() },
                        onNavigateBackHome = { viewModel.selectTab(ClinicTab.HOME) }
                    )
                }

                ClinicTab.QUEUE_MCU -> {
                    QueueAndMcuScreen(
                        queues = polyclinicQueues,
                        mcuPackages = mcuPackages,
                        activeBookings = bookings,
                        selectedSubTab = queueMcuSubTab,
                        activeCrisisScenarios = activeCrisisScenarios,
                        lastMitigationLog = lastMitigationLog,
                        onSelectSubTab = { viewModel.setQueueMcuSubTab(it) },
                        onAdvancePolyclinicQueue = { viewModel.advancePolyclinicQueue(it) },
                        onBookMcuPackage = { viewModel.startBookingForMcu(it) },
                        onToggleCrisisScenario = { viewModel.toggleCrisisScenario(it) },
                        onTriggerAllCrises = { viewModel.triggerAllCrisisScenarios() },
                        onMitigateAllCrises = { viewModel.mitigateAllCrisisScenarios() },
                        onNavigateBackHome = { viewModel.selectTab(ClinicTab.HOME) }
                    )
                }

                ClinicTab.SAAS_PHASE1 -> {
                    SaasPhase1Screen(
                        tenants = saasTenants,
                        selectedTenantId = selectedTenantId,
                        selectedRole = selectedUserRole,
                        selectedZone = selectedDisplayZone,
                        scheduleRules = tenantScheduleRules,
                        allPhase1Reservations = allPhase1Reservations,
                        auditLogs = recentAuditLogs,
                        concurrencyReport = concurrencyTestReport,
                        onSelectTenant = { viewModel.selectTenant(it) },
                        onSelectRole = { viewModel.selectUserRole(it) },
                        onSelectZone = { viewModel.selectDisplayZone(it) },
                        onBookSlot = { rule, pName, cat, startUtc, endUtc ->
                            viewModel.bookPhase1Slot(rule, pName, cat, startUtc, endUtc)
                        },
                        onRunConcurrencyTest = { rule, startUtc, endUtc ->
                            viewModel.triggerConcurrencyDoubleBookingRace(rule, startUtc, endUtc)
                        },
                        onUpdateReservationStatus = { res, st ->
                            viewModel.updatePhase1ReservationStatus(res, st)
                        },
                        onRunNoShowSweeper = { viewModel.runNoShowSweeper() },
                        onUpdateSlotDuration = { rule, mins ->
                            viewModel.updateDoctorSlotDuration(rule, mins)
                        },
                        onToggleHoliday = { viewModel.toggleClinicHoliday(it) },
                        onExecutePdpErasure = { pName ->
                            viewModel.executePdpRightToErasure(pName)
                        },
                        onNavigateBackHome = { viewModel.selectTab(ClinicTab.HOME) }
                    )
                }
            }
        }
    }

    // Modals & Bottom Sheets
    selectedDoctorDetail?.let { doctor ->
        DoctorDetailBottomSheet(
            doctor = doctor,
            onDismiss = { viewModel.openDoctorDetail(null) },
            onBookDoctor = { doc -> viewModel.startBookingForDoctor(doc) }
        )
    }

    if (wizardState.isVisible) {
        BookingWizardSheet(
            state = wizardState,
            doctors = doctors,
            patients = patients,
            dateOptions = viewModel.upcomingDateOptions,
            onDismiss = { viewModel.closeBookingWizard() },
            onStepChange = { viewModel.setWizardStep(it) },
            onSelectDoctor = { viewModel.selectWizardDoctor(it) },
            onSelectPatient = { viewModel.selectWizardPatient(it) },
            onSelectPayment = { viewModel.selectWizardPaymentType(it) },
            onSelectDate = { viewModel.selectWizardDate(it) },
            onSelectTimeSlot = { viewModel.selectWizardTimeSlot(it) },
            onUpdateSymptoms = { viewModel.updateWizardSymptoms(it) },
            onAppendSymptomTag = { viewModel.appendWizardSymptomTag(it) },
            onSubmitBooking = { viewModel.submitBooking() }
        )
    }

    activeTicketModal?.let { booking ->
        TicketQrCodeDialog(
            booking = booking,
            onDismiss = { viewModel.openTicketModal(null) },
            onCheckIn = { viewModel.checkInBooking(it) }
        )
    }

    rescheduleTarget?.let { booking ->
        val doc = doctors.find { it.id == booking.doctorId }
        val slots = doc?.timeSlots?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: listOf("08:30", "09:30", "10:30", "13:30", "15:00")
        RescheduleBookingDialog(
            booking = booking,
            dateOptions = viewModel.upcomingDateOptions,
            availableSlots = slots,
            onDismiss = { viewModel.openRescheduleModal(null) },
            onConfirm = { newDate, newSlot ->
                viewModel.confirmReschedule(booking, newDate, newSlot)
            }
        )
    }
}
