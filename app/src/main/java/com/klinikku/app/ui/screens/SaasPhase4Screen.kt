package com.klinikku.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klinikku.app.data.BpjsPcareClaimEntity
import com.klinikku.app.data.ClinicBranchEntity
import com.klinikku.app.data.ClinicFormatters
import com.klinikku.app.data.PatientEntity
import com.klinikku.app.data.PharmacyInventoryEntity
import com.klinikku.app.data.Phase3ClinicalSafetyEngine
import com.klinikku.app.data.SatusehatFhirOutboxEntity
import com.klinikku.app.data.TenantClinicEntity
import com.klinikku.app.data.UserRole

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaasPhase4Screen(
    tenants: List<TenantClinicEntity>,
    selectedTenantId: String,
    selectedRole: UserRole,
    selectedSubTab: Int,
    satusehatOutbox: List<SatusehatFhirOutboxEntity>,
    bpjsClaims: List<BpjsPcareClaimEntity>,
    pharmacyInventory: List<PharmacyInventoryEntity>,
    branches: List<ClinicBranchEntity>,
    patients: List<PatientEntity>,
    statusBanner: String?,
    onSelectTenant: (TenantClinicEntity) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectSubTab: (Int) -> Unit,
    onEnqueueSatusehatBundle: (
        encounterCode: String,
        patientName: String,
        icd10Code: String,
        loincCode: String,
        kfaCode: String,
        generalConsentAccepted: Boolean
    ) -> Unit,
    onSyncAllSatusehatOutbox: () -> Unit,
    onCreateBpjsClaim: (
        patientName: String,
        bpjsCardNumber: String,
        poliCodePcare: String,
        icd10Diagnosis: String,
        isReferralFktl: Boolean,
        referralHospitalName: String
    ) -> Unit,
    onDispensePharmacyStock: (PharmacyInventoryEntity, Int) -> Unit,
    onRestockPharmacyBatch: (PharmacyInventoryEntity, Int) -> Unit,
    onCreateNewBranch: (String, String, String) -> Unit,
    onNavigateBackHome: () -> Unit
) {
    BackHandler {
        onNavigateBackHome()
    }

    val tenantOutbox = remember(satusehatOutbox, selectedTenantId) {
        satusehatOutbox.filter { it.tenantId == selectedTenantId }
    }
    val tenantBpjs = remember(bpjsClaims, selectedTenantId) {
        bpjsClaims.filter { it.tenantId == selectedTenantId }
    }
    val tenantPharmacy = remember(pharmacyInventory, selectedTenantId) {
        pharmacyInventory.filter { it.tenantId == selectedTenantId }
    }
    val tenantBranches = remember(branches, selectedTenantId) {
        branches.filter { it.tenantId == selectedTenantId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saas_phase4_screen")
    ) {
        // Header Gradient Banner Fase 4 Enterprise Interoperability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF0F766E),
                            Color(0xFF047857)
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
                        color = Color(0xFFFDE047).copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "MODUL SAAS FASE 4 • SATUSEHAT FHIR R4 & BPJS PCARE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDE047),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "Multi-Cabang • KFA FEFO",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Integrasi SATUSEHAT, PCare BPJS, Apotek KFA, Analitik & Multi-Cabang",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tenant Switcher
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
                                color = if (selected) Color(0xFF1E1B4B) else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 5 Sub-Tabs for Fase 4
        val subTabs = listOf(
            "1. SATUSEHAT FHIR" to Icons.Default.CloudSync,
            "2. PCare BPJS" to Icons.Default.HealthAndSafety,
            "3. Apotek KFA" to Icons.Default.LocalPharmacy,
            "4. Multi-Cabang" to Icons.Default.AccountTree,
            "5. Analitik SaaS" to Icons.Default.Analytics
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
                    modifier = Modifier.testTag("phase4_subtab_$index")
                )
            }
        }

        // Status / Alert Banner
        statusBanner?.let { msg ->
            val isError = msg.contains("DIBLOKIR") || msg.contains("STOK_KURANG") || msg.contains("PERINGATAN")
            Surface(
                color = if (isError) Color(0xFFFEE2E2) else Color(0xFFECFDF5),
                border = BorderStroke(1.dp, if (isError) Color(0xFFEF4444) else Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.WarningAmber else Icons.Default.Verified,
                        contentDescription = null,
                        tint = if (isError) Color(0xFFB91C1C) else Color(0xFF047857)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isError) Color(0xFF7F1D1D) else Color(0xFF064E3B)
                    )
                }
            }
        }

        when (selectedSubTab) {
            0 -> Phase4SatusehatFhirSection(
                outboxItems = tenantOutbox,
                patients = patients,
                onEnqueueBundle = onEnqueueSatusehatBundle,
                onSyncAllOutbox = onSyncAllSatusehatOutbox
            )
            1 -> Phase4BpjsPcareSection(
                claims = tenantBpjs,
                patients = patients,
                onCreateClaim = onCreateBpjsClaim
            )
            2 -> Phase4PharmacyKfaSection(
                inventory = tenantPharmacy,
                onDispenseStock = onDispensePharmacyStock,
                onRestockBatch = onRestockPharmacyBatch
            )
            3 -> Phase4MultiBranchSection(
                branches = tenantBranches,
                onCreateBranch = onCreateNewBranch
            )
            4 -> Phase4AnalyticsSection(
                branches = tenantBranches,
                outboxItems = tenantOutbox,
                bpjsClaims = tenantBpjs
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase4SatusehatFhirSection(
    outboxItems: List<SatusehatFhirOutboxEntity>,
    patients: List<PatientEntity>,
    onEnqueueBundle: (
        encounterCode: String,
        patientName: String,
        icd10Code: String,
        loincCode: String,
        kfaCode: String,
        generalConsentAccepted: Boolean
    ) -> Unit,
    onSyncAllOutbox: () -> Unit
) {
    var selectedPatientIdx by remember { mutableIntStateOf(0) }
    var selectedIcdIdx by remember { mutableIntStateOf(0) }
    var generalConsentChecked by remember { mutableStateOf(true) }
    var expandedJsonBundleId by remember { mutableStateOf<String?>(null) }

    val selectedPatient = patients.getOrElse(selectedPatientIdx) { patients.firstOrNull() }
    val icdOptions = Phase3ClinicalSafetyEngine.commonIcd10Options
    val selectedIcd = icdOptions.getOrElse(selectedIcdIdx) { icdOptions.first() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Arsitektur Transactional Outbox SATUSEHAT (HL7 FHIR R4)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Dokter menyimpan SOAP di klinik dalam <50ms tanpa menunggu respons jaringan server Kemenkes.\n" +
                            "• Bundle FHIR R4 (Encounter + Condition ICD-10 + Observation LOINC + MedicationRequest KFA) masuk ke tabel Outbox dan dikirim secara asinkron dengan Exponential Backoff.\n" +
                            "• Wajib General Consent: Jika pasien belum mencentang persetujuan SATUSEHAT, pengiriman otomatis diblokir (BLOCKED_NO_CONSENT).",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Buat & Antrekan Bundle FHIR R4 ke Outbox SATUSEHAT",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        patients.forEachIndexed { idx, p ->
                            FilterChip(
                                selected = selectedPatientIdx == idx,
                                onClick = { selectedPatientIdx = idx },
                                label = { Text(p.fullName) }
                            )
                        }
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        icdOptions.take(4).forEachIndexed { idx, (code, desc) ->
                            FilterChip(
                                selected = selectedIcdIdx == idx,
                                onClick = { selectedIcdIdx = idx },
                                label = { Text("$code • ${desc.take(20)}") }
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = generalConsentChecked,
                            onCheckedChange = { generalConsentChecked = it }
                        )
                        Text(
                            text = "Pasien telah menyetujui General Consent pengiriman RME ke SATUSEHAT Kemenkes",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val pName = selectedPatient?.fullName ?: "Budi Raharjo"
                                onEnqueueBundle(
                                    "ENC-2610-NEW",
                                    pName,
                                    selectedIcd.first,
                                    "2093-3",
                                    "93001019",
                                    generalConsentChecked
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_enqueue_satusehat")
                        ) {
                            Text("Antrekan Bundle FHIR", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onSyncAllOutbox,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_sync_satusehat_worker")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Jalankan Worker Sync")
                        }
                    }
                }
            }
        }

        items(outboxItems, key = { it.id }) { item ->
            val statusColor = when (item.syncStatus) {
                "SYNCED_200_OK" -> Color(0xFF047857)
                "BLOCKED_NO_CONSENT" -> Color(0xFFB91C1C)
                else -> Color(0xFFB45309)
            }
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
                                text = "${item.fhirBundleId} • ${item.patientName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "IHS Pasien: ${item.patientIhsNumber} • IHS Nakes: ${item.practitionerIhsNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = item.syncStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.resourceTypesSummary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = item.lastSyncResponse,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            expandedJsonBundleId =
                                if (expandedJsonBundleId == item.fhirBundleId) null else item.fhirBundleId
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (expandedJsonBundleId == item.fhirBundleId) {
                                "Sembunyikan Payload FHIR R4 JSON"
                            } else {
                                "Lihat Payload FHIR R4 JSON"
                            }
                        )
                    }
                    if (expandedJsonBundleId == item.fhirBundleId) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFF0F172A),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.fhirJsonPreview,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase4BpjsPcareSection(
    claims: List<BpjsPcareClaimEntity>,
    patients: List<PatientEntity>,
    onCreateClaim: (
        patientName: String,
        bpjsCardNumber: String,
        poliCodePcare: String,
        icd10Diagnosis: String,
        isReferralFktl: Boolean,
        referralHospitalName: String
    ) -> Unit
) {
    var bpjsNumber by remember { mutableStateOf("0001849203194") }
    var icdDiagnosis by remember { mutableStateOf("J06.9 - ISPA Akut (Tuntas FKTP)") }
    var isReferralFktl by remember { mutableStateOf(false) }
    var referralHospital by remember { mutableStateOf("RSUP Fatmawati Jakarta (Poli Penyakit Dalam)") }

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
                        text = "Bridging PCare BPJS Kesehatan (FKTP & Rujukan FKTL)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Verifikasi kepesertaan 13 digit kartu BPJS, pencatatan pelayanan Kapitasi FKTP, dan penerbitan Surat Rujukan VClaim ke RS.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bpjsNumber,
                        onValueChange = { bpjsNumber = it },
                        label = { Text("Nomor Kartu BPJS (13 Digit)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = icdDiagnosis,
                        onValueChange = { icdDiagnosis = it },
                        label = { Text("Diagnosis ICD-10 PCare") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isReferralFktl,
                            onClick = {
                                isReferralFktl = false
                                icdDiagnosis = "J06.9 - ISPA Akut (Kompetensi 4A Tuntas FKTP)"
                            },
                            label = { Text("Tuntas di Klinik (FKTP)") }
                        )
                        FilterChip(
                            selected = isReferralFktl,
                            onClick = {
                                isReferralFktl = true
                                icdDiagnosis = "I20.0 - Angina Pektoris (Rujuk Spesialis Jantung RS)"
                            },
                            label = { Text("Rujuk Berjenjang ke RS (FKTL)") }
                        )
                    }
                    if (isReferralFktl) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = referralHospital,
                            onValueChange = { referralHospital = it },
                            label = { Text("Rumah Sakit Tujuan Rujukan (FKTL)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val pName = patients.firstOrNull()?.fullName ?: "Budi Raharjo"
                            onCreateClaim(
                                pName,
                                bpjsNumber,
                                "001 - Poli Umum FKTP",
                                icdDiagnosis,
                                isReferralFktl,
                                referralHospital
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Terbitkan Kunjungan / Rujukan PCare BPJS", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(claims, key = { it.id }) { claim ->
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
                                text = "No. Kunjungan: ${claim.noKunjunganPcare}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${claim.patientName} • Kartu BPJS: ${claim.bpjsCardNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = if (claim.serviceStatus == "RUJUK_FKTL_RS") Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = claim.serviceStatus,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Diagnosis: ${claim.icd10Diagnosis} (${claim.poliCodePcare})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (claim.serviceStatus == "RUJUK_FKTL_RS") {
                        Text(
                            text = "RS Tujuan Rujukan: ${claim.referralHospitalName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB45309),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Status Peserta: ${claim.membershipStatus} • Skema: ${claim.claimTariffType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase4PharmacyKfaSection(
    inventory: List<PharmacyInventoryEntity>,
    onDispenseStock: (PharmacyInventoryEntity, Int) -> Unit,
    onRestockBatch: (PharmacyInventoryEntity, Int) -> Unit
) {
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
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Inventori Apotek Standar KFA Kemenkes & FEFO Lock",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Diurutkan otomatis berdasarkan FEFO (First Expired First Out) agar obat yang mendekati masa kedaluwarsa dikeluarkan lebih dulu.\n" +
                            "• Dilengkapi transaksi atomik (@Transaction) sehingga stok tidak pernah bisa menjadi minus saat 2 apoteker memproses resep bersamaan.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        items(inventory, key = { it.id }) { item ->
            val isLowStock = item.stockQuantity <= item.minReorderThreshold
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(
                    1.dp,
                    if (isLowStock) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant
                ),
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
                                text = item.medicationName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kode KFA Kemenkes: ${item.kfaCode} • Batch: ${item.batchNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Kedaluwarsa (FEFO): ${item.expiryDateIso} • Harga: ${ClinicFormatters.formatRupiah(item.unitPriceIdr)}/unit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = if (isLowStock) Color(0xFFFEE2E2) else Color(0xFFECFDF5),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = "STOK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isLowStock) Color(0xFFB91C1C) else Color(0xFF047857)
                                )
                                Text(
                                    text = "${item.stockQuantity}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLowStock) Color(0xFFB91C1C) else Color(0xFF047857)
                                )
                            }
                        }
                    }

                    if (isLowStock) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ PERINGATAN REORDER: Stok (${item.stockQuantity}) di bawah batas minimum (${item.minReorderThreshold})!",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onDispenseStock(item, 10) }
                        ) {
                            Text("Potong Resep (-10)")
                        }
                        OutlinedButton(
                            onClick = { onDispenseStock(item, 500) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFB91C1C)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.GppBad,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uji Anti-Minus (-500)")
                        }
                        OutlinedButton(
                            onClick = { onRestockBatch(item, 50) }
                        ) {
                            Text("+ Restock Batch (+50)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase4MultiBranchSection(
    branches: List<ClinicBranchEntity>,
    onCreateBranch: (String, String, String) -> Unit
) {
    var branchName by remember { mutableStateOf("Cabang Surabaya Barat") }
    var cityProvince by remember { mutableStateOf("Surabaya, Jawa Timur") }
    var selectedZone by remember { mutableStateOf("WIB") }

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
                        text = "Tambah Cabang Klinik Baru (Multi-Cabang Lintas Zona Waktu)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { branchName = it },
                        label = { Text("Nama Cabang Klinik") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cityProvince,
                        onValueChange = { cityProvince = it },
                        label = { Text("Kota & Provinsi") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("WIB", "WITA", "WIT").forEach { z ->
                            FilterChip(
                                selected = selectedZone == z,
                                onClick = { selectedZone = z },
                                label = { Text("Zona $z") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onCreateBranch(branchName, cityProvince, selectedZone)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Aktifkan Cabang Baru & Generate SATUSEHAT Org ID", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(branches, key = { it.id }) { branch ->
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
                                text = "${branch.branchName} (${branch.branchCode})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${branch.cityAndProvince} • Zona Waktu: ${branch.timeZoneCode}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Sync ${branch.satusehatSyncPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SATUSEHAT Org: ${branch.satusehatOrgId} • Kode PCare: ${branch.bpjsPcareCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dokter: ${branch.activeDoctorsCount} • Kunjungan: ${branch.monthlyVisitsCount}/bln",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = ClinicFormatters.formatRupiah(branch.monthlyRevenueIdr),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase4AnalyticsSection(
    branches: List<ClinicBranchEntity>,
    outboxItems: List<SatusehatFhirOutboxEntity>,
    bpjsClaims: List<BpjsPcareClaimEntity>
) {
    val totalVisits = branches.sumOf { it.monthlyVisitsCount }
    val totalRevenue = branches.sumOf { it.monthlyRevenueIdr }
    val syncedCount = outboxItems.count { it.syncStatus == "SYNCED_200_OK" }

    val topDiseases = listOf(
        Triple("K21.9 - GERD & Dispepsia", 342, Color(0xFF0D9488)),
        Triple("J06.9 - ISPA Akut", 289, Color(0xFF0284C7)),
        Triple("K02.1 - Karies Dentin Gigi", 214, Color(0xFF6366F1)),
        Triple("I10 - Hipertensi Esensial", 176, Color(0xFFF59E0B)),
        Triple("L20.9 - Dermatitis Alergi", 128, Color(0xFFEC4899))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL KUNJUNGAN GRUP", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$totalVisits Pasien/Bln",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Dari ${branches.size} Cabang Aktif", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Surface(
                    color = Color(0xFFECFDF5),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("OMZET KONSOLIDASI", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46))
                        Text(
                            text = ClinicFormatters.formatRupiah(totalRevenue),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF047857)
                        )
                        Text("SATUSEHAT: $syncedCount Bundle OK", style = MaterialTheme.typography.labelSmall, color = Color(0xFF065F46))
                    }
                }
            }
        }

        // Epidemiological ICD-10 Surveillance Chart Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Surveilans Epidemiologi 5 Besar Penyakit (ICD-10)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Laporan otomatis bulanan untuk Dinas Kesehatan & evaluasi stok obat apotek (KFA):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    topDiseases.forEach { (label, count, barColor) ->
                        val ratio = (count / 360f).coerceIn(0.15f, 1f)
                        Column(modifier = Modifier.padding(vertical = 5.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "$count Kasus",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                            ) {
                                drawRoundRect(
                                    color = Color(0xFFE2E8F0),
                                    size = size,
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                                drawRoundRect(
                                    color = barColor,
                                    size = Size(size.width * ratio, size.height),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Branch Comparison Summary
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Perbandingan Kinerja Antar Cabang & Klaim BPJS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Total Kunjungan PCare BPJS Ter-bridging: ${bpjsClaims.size} Klaim Aktif\n" +
                            "• Rata-rata Waktu Tunggu Lintas Cabang: 11 Menit (Target Kemenkes < 30 Menit)\n" +
                            "• Rata-rata No-Show setelah DP QRIS: 4.1% (Turun dari 28%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
