package com.klinikku.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klinikku.app.data.AuditLogEntity
import com.klinikku.app.data.DoctorEntity
import com.klinikku.app.data.ElectronicPrescriptionEntity
import com.klinikku.app.data.EmrSoapRecordEntity
import com.klinikku.app.data.LabResultEntity
import com.klinikku.app.data.MedicalCertificateEntity
import com.klinikku.app.data.PatientEntity
import com.klinikku.app.data.Phase3ClinicalSafetyEngine
import com.klinikku.app.data.TeleconsultationSessionEntity
import com.klinikku.app.data.TenantClinicEntity
import com.klinikku.app.data.UserRole

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaasPhase3Screen(
    tenants: List<TenantClinicEntity>,
    selectedTenantId: String,
    selectedRole: UserRole,
    selectedSubTab: Int,
    emrRecords: List<EmrSoapRecordEntity>,
    prescriptions: List<ElectronicPrescriptionEntity>,
    labResults: List<LabResultEntity>,
    certificates: List<MedicalCertificateEntity>,
    teleconsults: List<TeleconsultationSessionEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    auditLogs: List<AuditLogEntity>,
    complianceBanner: String?,
    onSelectTenant: (TenantClinicEntity) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectSubTab: (Int) -> Unit,
    onLogEmrReadAccess: (EmrSoapRecordEntity) -> Unit,
    onCreateSoapRecord: (
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
    ) -> Unit,
    onAppendSoapAddendum: (EmrSoapRecordEntity, String) -> Unit,
    onTestAttemptDeleteEmr25YearGuard: (EmrSoapRecordEntity) -> Unit,
    onCreatePrescription: (
        encounterCode: String,
        patient: PatientEntity,
        doctorName: String,
        medicationListPipe: String,
        overrideAllergy: Boolean
    ) -> Unit,
    onAdvancePrescriptionStatus: (ElectronicPrescriptionEntity) -> Unit,
    onCreateLabResult: (
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
    ) -> Unit,
    onIssueMedicalCertificate: (
        encounterCode: String,
        certificateType: String,
        patient: PatientEntity,
        doctorName: String,
        restDaysCount: Int,
        conclusion: String
    ) -> Unit,
    onAdvanceTeleconsult: (TeleconsultationSessionEntity, String?) -> Unit,
    onNavigateBackHome: () -> Unit
) {
    BackHandler {
        onNavigateBackHome()
    }

    val tenantEmr = remember(emrRecords, selectedTenantId) {
        emrRecords.filter { it.tenantId == selectedTenantId }
    }
    val tenantPrescriptions = remember(prescriptions, selectedTenantId) {
        prescriptions.filter { it.tenantId == selectedTenantId }
    }
    val tenantLabs = remember(labResults, selectedTenantId) {
        labResults.filter { it.tenantId == selectedTenantId }
    }
    val tenantCertificates = remember(certificates, selectedTenantId) {
        certificates.filter { it.tenantId == selectedTenantId }
    }
    val tenantTeleconsults = remember(teleconsults, selectedTenantId) {
        teleconsults.filter { it.tenantId == selectedTenantId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saas_phase3_screen")
    ) {
        // Header Gradient Banner Fase 3 Clinical & EMR
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF065F46),
                            Color(0xFF0F766E),
                            Color(0xFF312E81)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF6EE7B7).copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "MODUL SAAS FASE 3 • PERMENKES 24/2022 & UU PDP READY",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFA7F3D0),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Retensi 25 Thn • AES-256",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Rekam Medis Elektronik (SOAP), E-Resep, Lab, Surat & Telemedisin",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tenant Switcher Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tenants.forEach { tenant ->
                        val selected = tenant.tenantId == selectedTenantId
                        Surface(
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { onSelectTenant(tenant) }
                        ) {
                            Text(
                                text = "${tenant.clinicName} (${tenant.defaultZoneCode})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color(0xFF065F46) else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Role Switcher Chips (Demonstrating RLS per Role)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Simulasi Peran RLS:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD1FAE5)
                    )
                    UserRole.entries.forEach { role ->
                        val isRoleSelected = role == selectedRole
                        Surface(
                            color = if (isRoleSelected) Color(0xFFFDE047) else Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { onSelectRole(role) }
                        ) {
                            Text(
                                text = role.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isRoleSelected) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Sub-Tab Navigation for Fase 3 Modules
        val subTabs = listOf(
            "1. RME (SOAP)" to Icons.AutoMirrored.Filled.Assignment,
            "2. E-Resep & CDS" to Icons.Default.Medication,
            "3. Hasil Lab" to Icons.Default.Biotech,
            "4. Surat Medis" to Icons.Default.Description,
            "5. Telemedisin" to Icons.Default.VideoCall
        )
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            subTabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { onSelectSubTab(index) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = title,
                                fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    },
                    modifier = Modifier.testTag("phase3_subtab_$index")
                )
            }
        }

        // Live Compliance & CDS Feedback Banner
        complianceBanner?.let { bannerText ->
            val isAlert = bannerText.contains("BAHAYA") || bannerText.contains("DITOLAK") || bannerText.contains("PERINGATAN")
            Surface(
                color = if (isAlert) Color(0xFFFEE2E2) else Color(0xFFECFDF5),
                border = BorderStroke(
                    1.dp,
                    if (isAlert) Color(0xFFEF4444) else Color(0xFF10B981)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAlert) Icons.Default.WarningAmber else Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (isAlert) Color(0xFFB91C1C) else Color(0xFF047857)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = bannerText,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isAlert) Color(0xFF7F1D1D) else Color(0xFF064E3B)
                    )
                }
            }
        }

        when (selectedSubTab) {
            0 -> Phase3EmrSoapSection(
                emrRecords = tenantEmr,
                patients = patients,
                doctors = doctors,
                auditLogs = auditLogs,
                selectedRole = selectedRole,
                onLogReadAccess = onLogEmrReadAccess,
                onCreateSoap = onCreateSoapRecord,
                onAppendAddendum = onAppendSoapAddendum,
                onTestDelete25YearGuard = onTestAttemptDeleteEmr25YearGuard
            )
            1 -> Phase3PrescriptionCdsSection(
                prescriptions = tenantPrescriptions,
                emrRecords = tenantEmr,
                patients = patients,
                doctors = doctors,
                onCreatePrescription = onCreatePrescription,
                onAdvanceStatus = onAdvancePrescriptionStatus
            )
            2 -> Phase3LabResultsSection(
                labResults = tenantLabs,
                emrRecords = tenantEmr,
                patients = patients,
                doctors = doctors,
                onCreateLabResult = onCreateLabResult
            )
            3 -> Phase3MedicalCertificateSection(
                certificates = tenantCertificates,
                emrRecords = tenantEmr,
                patients = patients,
                doctors = doctors,
                onIssueCertificate = onIssueMedicalCertificate
            )
            4 -> Phase3TeleconsultSection(
                teleconsults = tenantTeleconsults,
                onAdvanceTeleconsult = onAdvanceTeleconsult
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase3EmrSoapSection(
    emrRecords: List<EmrSoapRecordEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    auditLogs: List<AuditLogEntity>,
    selectedRole: UserRole,
    onLogReadAccess: (EmrSoapRecordEntity) -> Unit,
    onCreateSoap: (
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
    ) -> Unit,
    onAppendAddendum: (EmrSoapRecordEntity, String) -> Unit,
    onTestDelete25YearGuard: (EmrSoapRecordEntity) -> Unit
) {
    var selectedPatientIdx by remember { mutableIntStateOf(0) }
    var selectedDoctorIdx by remember { mutableIntStateOf(0) }
    var selectedIcdIdx by remember { mutableIntStateOf(0) }
    var bpText by remember { mutableStateOf("120/78 mmHg") }
    var hrText by remember { mutableStateOf("76") }
    var tempText by remember { mutableStateOf("36.7") }
    var weightText by remember { mutableStateOf("65.0") }
    var subjectiveText by remember { mutableStateOf("Nyeri ulu hati dan mual setelah makan terlambat sejak 3 hari.") }
    var objectiveText by remember { mutableStateOf("KU kompos mentis, nyeri tekan epigastrium ringan (+), bising usus normal.") }
    var planText by remember { mutableStateOf("Omeprazole 20mg 1x1 sebelum makan pagi, Antasida Doen 3x1, diet rendah asam/pedas.") }

    var activeAddendumTargetId by remember { mutableStateOf<Int?>(null) }
    var addendumInput by remember { mutableStateOf("") }

    val selectedPatient = patients.getOrElse(selectedPatientIdx) { patients.firstOrNull() }
    val selectedDoctor = doctors.getOrElse(selectedDoctorIdx) { doctors.firstOrNull() }
    val icdOptions = Phase3ClinicalSafetyEngine.commonIcd10Options
    val currentIcd = icdOptions.getOrElse(selectedIcdIdx) { icdOptions.first() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Legal & Regulatory Guardrail Card (Permenkes 24/2022 & UU PDP)
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kepatuhan Permenkes No. 24/2022 & UU PDP No. 27/2022",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Immutability RME: Catatan SOAP yang sudah ditandatangani (TTE) dikunci permanen. Koreksi hanya boleh melalui Addendum bertanda waktu.\n" +
                            "• Retensi 25 Tahun (Pasal 29 Permenkes 24/2022): Rekam medis wajib disimpan minimal 25 tahun (s/d 2051) dan tidak boleh dihapus oleh fitur Hapus Akun reguler.\n" +
                            "• Siap Fase 4 SATUSEHAT: Kode diagnosis sudah memakai standar ICD-10 WHO & memiliki kolom referensi FHIR Encounter.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Form Input SOAP Baru (Khusus Dokter / Simulasi Dokter)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Input Rekam Medis Elektronik (SOAP + ICD-10)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pilih pasien, tanda vital, dan kode ICD-10 untuk membuat RME terenkripsi AES-256:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pilih Pasien:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        patients.forEachIndexed { idx, p ->
                            FilterChip(
                                selected = selectedPatientIdx == idx,
                                onClick = { selectedPatientIdx = idx },
                                label = { Text("${p.fullName} (${p.medicalRecordNumber})") }
                            )
                        }
                    }

                    Text(
                        text = "Kode Diagnosis Standar ICD-10 (Siap SATUSEHAT Fase 4):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        icdOptions.forEachIndexed { idx, (code, desc) ->
                            FilterChip(
                                selected = selectedIcdIdx == idx,
                                onClick = { selectedIcdIdx = idx },
                                label = { Text("$code • ${desc.take(24)}") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bpText,
                            onValueChange = { bpText = it },
                            label = { Text("TD (mmHg)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = hrText,
                            onValueChange = { hrText = it },
                            label = { Text("Nadi (bpm)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = tempText,
                            onValueChange = { tempText = it },
                            label = { Text("Suhu (°C)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = weightText,
                            onValueChange = { weightText = it },
                            label = { Text("Berat (kg)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = subjectiveText,
                        onValueChange = { subjectiveText = it },
                        label = { Text("S — Subjective (Keluhan & Anamnesis)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = objectiveText,
                        onValueChange = { objectiveText = it },
                        label = { Text("O — Objective (Pemeriksaan Fisik)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = planText,
                        onValueChange = { planText = it },
                        label = { Text("P — Plan (Rencana Terapi & Edukasi)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (selectedPatient != null && selectedDoctor != null) {
                                onCreateSoap(
                                    selectedPatient,
                                    selectedDoctor.name,
                                    selectedDoctor.polyclinic,
                                    bpText,
                                    hrText.toIntOrNull() ?: 78,
                                    tempText.toDoubleOrNull() ?: 36.7,
                                    weightText.toDoubleOrNull() ?: 65.0,
                                    subjectiveText,
                                    objectiveText,
                                    currentIcd.first,
                                    currentIcd.second,
                                    planText
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_save_signed_soap")
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tanda Tangani (TTE) & Kunci RME SOAP",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Daftar Rekam Medis Elektronik (SOAP)
        item {
            Text(
                text = "Arsip Rekam Medis Elektronik (${emrRecords.size} Kunjungan)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(emrRecords, key = { it.id }) { rec ->
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${rec.encounterCode} • ${rec.patientName} (${rec.patientRm})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${rec.doctorName} • ${rec.polyclinic}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = Color(0xFF065F46),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "ICD-10: ${rec.icd10Code}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (rec.patientAllergies.contains("Penisilin", ignoreCase = true) ||
                        !rec.patientAllergies.contains("Tidak ada", ignoreCase = true)
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFFEE2E2),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ ALERGI PASIEN: ${rec.patientAllergies}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB91C1C),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Vital Signs Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        VitalChip("TD: ${rec.bloodPressureMmHg}")
                        VitalChip("Nadi: ${rec.heartRateBpm} bpm")
                        VitalChip("Suhu: ${rec.temperatureCelsius}°C")
                        VitalChip("BB: ${rec.weightKg} kg")
                        VitalChip("Retensi s/d ${rec.retentionUntilYear}")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    SoapFieldBlock("S (Subjective)", rec.subjectiveText)
                    SoapFieldBlock("O (Objective)", rec.objectiveText)
                    SoapFieldBlock("A (Assessment • ${rec.icd10Code})", rec.assessmentDiagnosis)
                    SoapFieldBlock("P (Plan & Terapi)", rec.planTherapy)

                    if (rec.addendumNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "ADDENDUM RESMI (Catatan Tambahan Terkunci):",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    text = rec.addendumNotes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EnhancedEncryption,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${rec.encryptedCipherPreview} • Ref: ${rec.satusehatEncounterRef}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Interactive Compliance Actions on SOAP Card
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onLogReadAccess(rec) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Audit Akses Baca (${selectedRole.label})")
                        }

                        OutlinedButton(
                            onClick = {
                                activeAddendumTargetId = if (activeAddendumTargetId == rec.id) null else rec.id
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.HistoryEdu,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Tambah Addendum")
                        }

                        OutlinedButton(
                            onClick = { onTestDelete25YearGuard(rec) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFB91C1C)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.GppBad,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uji Hapus (Proteksi 25 Thn)")
                        }
                    }

                    if (activeAddendumTargetId == rec.id) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = addendumInput,
                            onValueChange = { addendumInput = it },
                            label = { Text("Tulis Addendum Koreksi (Tanpa Mengubah SOAP Asli)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = {
                                onAppendAddendum(rec, addendumInput)
                                addendumInput = ""
                                activeAddendumTargetId = null
                            }
                        ) {
                            Text("Simpan Addendum Bertanda Waktu")
                        }
                    }
                }
            }
        }

        // Read-Access & Clinical Audit Log Preview
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Jejak Audit Akses Rekam Medis (Siapa Melihat Data Siapa)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    auditLogs.take(6).forEach { log ->
                        Text(
                            text = "• [${log.actorRole}] ${log.actionType}: ${log.targetResource}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase3PrescriptionCdsSection(
    prescriptions: List<ElectronicPrescriptionEntity>,
    emrRecords: List<EmrSoapRecordEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    onCreatePrescription: (
        encounterCode: String,
        patient: PatientEntity,
        doctorName: String,
        medicationListPipe: String,
        overrideAllergy: Boolean
    ) -> Unit,
    onAdvanceStatus: (ElectronicPrescriptionEntity) -> Unit
) {
    // Default to Siti Larasati (index 1) so user can immediately test Penicillin Allergy CDS!
    var selectedPatientIdx by remember { mutableIntStateOf(if (patients.size > 1) 1 else 0) }
    var medicationsText by remember {
        mutableStateOf("Amoxicillin 500mg Kaplet (3x1 sesudah makan, No. XV)|Paracetamol 500mg (3x1 bila nyeri)")
    }
    var overrideAllergyChecked by remember { mutableStateOf(false) }

    val selectedPatient = patients.getOrElse(selectedPatientIdx) { patients.firstOrNull() }
    val liveAllergyCheck = remember(selectedPatient, medicationsText) {
        Phase3ClinicalSafetyEngine.checkDrugAllergyConflict(
            patientAllergies = selectedPatient?.allergies.orEmpty(),
            medicationListText = medicationsText
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "E-Resep & Deteksi Alergi Obat Otomatis (CDS)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sistem otomatis memeriksa silang daftar obat dengan riwayat alergi pasien sebelum resep dikirim ke apotek.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Pilih Pasien:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        patients.forEachIndexed { idx, p ->
                            FilterChip(
                                selected = selectedPatientIdx == idx,
                                onClick = { selectedPatientIdx = idx },
                                label = { Text("${p.fullName} (Alergi: ${p.allergies})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                medicationsText =
                                    "Amoxicillin 500mg Kaplet (3x1 sesudah makan, No. XV)|Asam Mefenamat 500mg (3x1)"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Preset Uji Konflik Penisilin", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = {
                                medicationsText =
                                    "Clindamycin 300mg Kapsul (3x1, No. X)|Paracetamol 500mg (3x1 bila nyeri)"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Preset Resep Aman Non-Penisilin", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = medicationsText,
                        onValueChange = { medicationsText = it },
                        label = { Text("Daftar Obat & Dosis (Pisahkan dengan '|')") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-time CDS Allergy Banner
                    Surface(
                        color = if (liveAllergyCheck.first) Color(0xFFFEE2E2) else Color(0xFFECFDF5),
                        border = BorderStroke(
                            1.dp,
                            if (liveAllergyCheck.first) Color(0xFFEF4444) else Color(0xFF10B981)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (liveAllergyCheck.first) Icons.Default.AddAlert else Icons.Default.GppGood,
                                    contentDescription = null,
                                    tint = if (liveAllergyCheck.first) Color(0xFFB91C1C) else Color(0xFF047857)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = liveAllergyCheck.second,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (liveAllergyCheck.first) Color(0xFF7F1D1D) else Color(0xFF064E3B)
                                )
                            }
                            if (liveAllergyCheck.first) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = overrideAllergyChecked,
                                        onCheckedChange = { overrideAllergyChecked = it }
                                    )
                                    Text(
                                        text = "Override Darurat oleh Dokter (Wajib Justifikasi Medis di Audit Log)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF7F1D1D)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (selectedPatient != null) {
                                val encCode = emrRecords.firstOrNull()?.encounterCode ?: "ENC-2610-419"
                                val docName = doctors.firstOrNull()?.name ?: "drg. Hendra Wijaya, Sp.KG"
                                onCreatePrescription(
                                    encCode,
                                    selectedPatient,
                                    docName,
                                    medicationsText,
                                    overrideAllergyChecked
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_issue_eprescription")
                    ) {
                        Text(
                            text = "Terbitkan E-Resep ke Instalasi Farmasi",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        items(prescriptions, key = { it.id }) { rx ->
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${rx.rxNumber} • ${rx.patientName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Dokter: ${rx.doctorName} • Ref: ${rx.encounterCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = rx.pharmacyStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    rx.medicationListPipe.split("|").filter { it.isNotBlank() }.forEachIndexed { idx, med ->
                        Text(
                            text = "${idx + 1}. $med",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Status CDS: ${rx.allergyWarningMessage}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (rx.hasAllergyConflict) Color(0xFFB91C1C) else Color(0xFF047857),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (rx.pharmacyStatus != "DISERAHKAN") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { onAdvanceStatus(rx) }) {
                            Text("Majukan Status Farmasi (Siapkan / Serahkan Obat)")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase3LabResultsSection(
    labResults: List<LabResultEntity>,
    emrRecords: List<EmrSoapRecordEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    onCreateLabResult: (
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
    ) -> Unit
) {
    var paramName by remember { mutableStateOf("Asam Urat Darah (Uric Acid)") }
    var loincCode by remember { mutableStateOf("3084-1") }
    var resultVal by remember { mutableStateOf("7.8") }
    var unitText by remember { mutableStateOf("mg/dL") }
    var refRange by remember { mutableStateOf("3.4 – 7.0 mg/dL") }
    var selectedFlag by remember { mutableStateOf("HIGH") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Input Hasil Laboratorium Diagnostik (Standar LOINC)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Setiap parameter menggunakan kode LOINC agar siap dikirim sebagai FHIR Observation ke SATUSEHAT di Fase 4.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = paramName,
                            onValueChange = { paramName = it },
                            label = { Text("Parameter Lab") },
                            modifier = Modifier.weight(1.4f)
                        )
                        OutlinedTextField(
                            value = loincCode,
                            onValueChange = { loincCode = it },
                            label = { Text("Kode LOINC") },
                            modifier = Modifier.weight(0.8f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = resultVal,
                            onValueChange = { resultVal = it },
                            label = { Text("Nilai Hasil") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = unitText,
                            onValueChange = { unitText = it },
                            label = { Text("Satuan") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = refRange,
                            onValueChange = { refRange = it },
                            label = { Text("Nilai Rujukan") },
                            modifier = Modifier.weight(1.2f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("NORMAL", "HIGH", "CRITICAL").forEach { flag ->
                            FilterChip(
                                selected = selectedFlag == flag,
                                onClick = { selectedFlag = flag },
                                label = { Text("Flag: $flag") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val enc = emrRecords.firstOrNull()?.encounterCode ?: "ENC-2610-401"
                            val pName = patients.firstOrNull()?.fullName ?: "Budi Raharjo"
                            val dName = doctors.firstOrNull()?.name ?: "dr. Nadia Prameswari, Sp.PD"
                            onCreateLabResult(
                                enc,
                                pName,
                                dName,
                                "Metabolik & Kimia Darah",
                                loincCode,
                                paramName,
                                resultVal,
                                unitText,
                                refRange,
                                selectedFlag
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Publikasikan Hasil Lab Terverifikasi", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(labResults, key = { it.id }) { lab ->
            val isAbnormal = lab.flagStatus != "NORMAL"
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isAbnormal) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${lab.parameterName} (LOINC: ${lab.loincCode})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lab.labOrderNumber} • Pasien: ${lab.patientName} • ${lab.panelCategory}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Nilai Rujukan Normal: ${lab.referenceRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${lab.resultValue} ${lab.unit}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isAbnormal) Color(0xFFB45309) else Color(0xFF047857)
                        )
                        Surface(
                            color = if (isAbnormal) Color(0xFFFEF3C7) else Color(0xFFECFDF5),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = lab.flagStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAbnormal) Color(0xFF92400E) else Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase3MedicalCertificateSection(
    certificates: List<MedicalCertificateEntity>,
    emrRecords: List<EmrSoapRecordEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    onIssueCertificate: (
        encounterCode: String,
        certificateType: String,
        patient: PatientEntity,
        doctorName: String,
        restDaysCount: Int,
        conclusion: String
    ) -> Unit
) {
    var certType by remember { mutableStateOf("SURAT_SAKIT") }
    var restDaysText by remember { mutableStateOf("2") }
    var conclusionText by remember {
        mutableStateOf(
            "Berdasarkan pemeriksaan medis, pasien memerlukan istirahat selama 2 hari. (Detail diagnosis ICD-10 dirahasiakan sesuai etika medis & UU PDP)."
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Terbitkan Surat Keterangan Medis (TTE Hash)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prinsip Privasi HRD: Surat Sakit mencantumkan durasi istirahat tanpa membocorkan diagnosis spesifik kecuali pasien memberi izin tertulis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = certType == "SURAT_SAKIT",
                            onClick = {
                                certType = "SURAT_SAKIT"
                                conclusionText =
                                    "Berdasarkan pemeriksaan medis, pasien memerlukan istirahat selama 2 hari. (Detail diagnosis ICD-10 dirahasiakan sesuai etika medis & UU PDP)."
                            },
                            label = { Text("Surat Keterangan Sakit") }
                        )
                        FilterChip(
                            selected = certType == "SURAT_SEHAT",
                            onClick = {
                                certType = "SURAT_SEHAT"
                                conclusionText =
                                    "Dinyatakan SEHAT JASMANI berdasarkan pemeriksaan tanda vital fisik untuk keperluan administrasi."
                            },
                            label = { Text("Surat Keterangan Sehat") }
                        )
                    }
                    if (certType == "SURAT_SAKIT") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = restDaysText,
                            onValueChange = { restDaysText = it },
                            label = { Text("Jumlah Hari Istirahat") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = conclusionText,
                        onValueChange = { conclusionText = it },
                        label = { Text("Kesimpulan Medis Resmi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val p = patients.firstOrNull() ?: return@Button
                            val d = doctors.firstOrNull()?.name ?: "dr. Nadia Prameswari, Sp.PD"
                            val enc = emrRecords.firstOrNull()?.encounterCode ?: "ENC-2610-401"
                            onIssueCertificate(
                                enc,
                                certType,
                                p,
                                d,
                                restDaysText.toIntOrNull() ?: 2,
                                conclusionText
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Terbitkan Surat & Bubuhkan TTE SHA-256", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(certificates, key = { it.id }) { cert ->
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${cert.certificateNumber} • ${cert.certificateType}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = Color(0xFFECFDF5),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = cert.validPeriodText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pasien: ${cert.patientName} (${cert.patientRm}) • Pemeriksa: ${cert.doctorName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = cert.medicalConclusion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "✓ Terverifikasi Digital: ${cert.digitalSignatureHash}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun Phase3TeleconsultSection(
    teleconsults: List<TeleconsultationSessionEntity>,
    onAdvanceTeleconsult: (TeleconsultationSessionEntity, String?) -> Unit
) {
    var chatNoteInput by remember {
        mutableStateOf("Dokter: Silakan lanjutkan terapi obat 5 hari dan pantau tekanan darah di rumah.")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VideoCall,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ruang Konsultasi Online (Telemedisin Terjadwal)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dilengkapi persetujuan Informed Consent Telemedisin, transkrip klinis terenkripsi, dan tautan otomatis ke RME SOAP.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = chatNoteInput,
                        onValueChange = { chatNoteInput = it },
                        label = { Text("Catatan / Pesan Konsultasi Video") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        items(teleconsults, key = { it.id }) { session ->
            Card(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${session.sessionCode} • ${session.patientName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${session.doctorName} (${session.polyclinic})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = when (session.status) {
                                "LIVE_VIDEO" -> Color(0xFFDCFCE7)
                                "WAITING_ROOM" -> Color(0xFFFEF3C7)
                                else -> Color(0xFFE0E7FF)
                            },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = session.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = session.clinicalChatTranscript,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onAdvanceTeleconsult(session, chatNoteInput) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (session.status) {
                                "WAITING_ROOM" -> "Mulai Panggilan Video & Kirim Catatan"
                                "LIVE_VIDEO" -> "Selesaikan Telekonsultasi & Tautkan ke RME SOAP"
                                else -> "Tambahkan Catatan Tindak Lanjut Telemedisin"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SoapFieldBlock(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
