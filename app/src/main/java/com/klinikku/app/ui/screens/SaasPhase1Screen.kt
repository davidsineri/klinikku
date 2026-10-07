package com.klinikku.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klinikku.app.data.AuditLogEntity
import com.klinikku.app.data.ClinicFormatters
import com.klinikku.app.data.IndonesianZone
import com.klinikku.app.data.Phase1ReservationEntity
import com.klinikku.app.data.SaasTimeAndSlotEngine
import com.klinikku.app.data.ScheduleRuleEntity
import com.klinikku.app.data.TenantClinicEntity
import com.klinikku.app.data.UserRole

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaasPhase1Screen(
    tenants: List<TenantClinicEntity>,
    selectedTenantId: String,
    selectedRole: UserRole,
    selectedZone: IndonesianZone,
    scheduleRules: List<ScheduleRuleEntity>,
    allPhase1Reservations: List<Phase1ReservationEntity>,
    auditLogs: List<AuditLogEntity>,
    concurrencyReport: String?,
    onSelectTenant: (TenantClinicEntity) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectZone: (IndonesianZone) -> Unit,
    onBookSlot: (ScheduleRuleEntity, String, String, Long, Long) -> Unit,
    onRunConcurrencyTest: (ScheduleRuleEntity, Long, Long) -> Unit,
    onUpdateReservationStatus: (Phase1ReservationEntity, String) -> Unit,
    onRunNoShowSweeper: () -> Unit,
    onUpdateSlotDuration: (ScheduleRuleEntity, Int) -> Unit,
    onToggleHoliday: (TenantClinicEntity) -> Unit,
    onExecutePdpErasure: (String) -> Unit,
    onNavigateBackHome: () -> Unit
) {
    BackHandler {
        onNavigateBackHome()
    }

    val activeTenant = tenants.find { it.tenantId == selectedTenantId } ?: tenants.firstOrNull()
    val tenantReservations = remember(allPhase1Reservations, selectedTenantId) {
        allPhase1Reservations.filter { it.tenantId == selectedTenantId }
    }

    var selectedRuleIndex by remember(selectedTenantId) { mutableIntStateOf(0) }
    val activeRule = scheduleRules.getOrNull(selectedRuleIndex) ?: scheduleRules.firstOrNull()

    val generatedSlots = remember(activeRule, selectedZone, tenantReservations) {
        if (activeRule != null) {
            SaasTimeAndSlotEngine.generateDynamicSlotsForToday(
                rule = activeRule,
                clinicZone = selectedZone,
                existingReservations = tenantReservations
            )
        } else {
            emptyList()
        }
    }

    var selectedSlotStartUtc by remember(generatedSlots) {
        val firstAvailable = generatedSlots.firstOrNull { !it.isBreakTime && !it.isOccupied }
            ?: generatedSlots.firstOrNull { !it.isBreakTime }
        mutableLongStateOf(firstAvailable?.slotStartUtcMillis ?: System.currentTimeMillis())
    }
    val selectedSlotObj = generatedSlots.find { it.slotStartUtcMillis == selectedSlotStartUtc }
        ?: generatedSlots.firstOrNull()

    var patientNameInput by remember { mutableStateOf("Budi Raharjo") }
    var visitCategory by remember { mutableStateOf("Konsultasi Baru (Non-Medis Fase 1)") }
    var pdpConsentChecked by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saas_phase1_screen"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Multi-Tenant & Role Switcher Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Arsitektur SaaS Multi-Klinik • Fase 1",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Reservasi & Antrean (Tanpa Data Medis/SOAP) • Anti-Double Booking DB Lock • UTC <-> WIB/WITA/WIT • UU PDP",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "1. Pilih Peran Pengguna (Simulasi RLS / RBAC):",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        UserRole.entries.forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { onSelectRole(role) },
                                label = { Text(role.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("role_chip_${role.code}")
                            )
                        }
                    }
                    Text(
                        text = "Hak Akses Aktif: ${selectedRole.description}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "2. Pilih Tenant Klinik (Isolasi Multi-Tenant):",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tenants.forEach { tenant ->
                            FilterChip(
                                selected = tenant.tenantId == selectedTenantId,
                                onClick = { onSelectTenant(tenant) },
                                label = {
                                    Text("${tenant.clinicName} (${tenant.defaultZoneCode})")
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Business,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                modifier = Modifier.testTag("tenant_chip_${tenant.tenantId}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tampilan Zona Waktu:",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IndonesianZone.entries.forEach { zone ->
                                FilterChip(
                                    selected = selectedZone == zone,
                                    onClick = { onSelectZone(zone) },
                                    label = { Text(zone.code) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Role-Specific Interactive Workspace
        when (selectedRole) {
            UserRole.PATIENT -> {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Booking Slot Dinamis (${activeTenant?.clinicName ?: "-"})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Fase 1: Tanpa pengumpulan diagnosis/data medis sensitif",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (activeTenant?.isHolidayClosed == true) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Klinik sedang tutup (Hari Libur Operasional). Pemesanan slot dinonaktifkan sementara oleh Admin Klinik.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else if (activeRule != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                if (scheduleRules.size > 1) {
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        scheduleRules.forEachIndexed { idx, r ->
                                            FilterChip(
                                                selected = idx == selectedRuleIndex,
                                                onClick = { selectedRuleIndex = idx },
                                                label = { Text("${r.doctorName} (${r.slotDurationMinutes}m)") }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }

                                Text(
                                    text = "Pilih Slot (${selectedZone.code} • Disimpan di DB sebagai UTC):",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    generatedSlots.forEach { slot ->
                                        val isSelected = slot.slotStartUtcMillis == selectedSlotStartUtc
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = {
                                                if (!slot.isBreakTime) {
                                                    selectedSlotStartUtc = slot.slotStartUtcMillis
                                                }
                                            },
                                            enabled = !slot.isBreakTime,
                                            label = {
                                                val tag = when {
                                                    slot.isBreakTime -> "${slot.displayLocalTime} (Istirahat)"
                                                    slot.isOccupied -> "${slot.displayLocalTime} (Terkunci)"
                                                    else -> "${slot.displayLocalTime} (${slot.displayUtcTime})"
                                                }
                                                Text(tag, style = MaterialTheme.typography.labelSmall)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = patientNameInput,
                                    onValueChange = { patientNameInput = it },
                                    label = { Text("Nama Pasien") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Kategori Kunjungan Administratif (Bukan Diagnosis Medis):",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(
                                        "Konsultasi Baru (Non-Medis Fase 1)",
                                        "Kontrol Terjadwal",
                                        "Tindakan Ringan",
                                        "Administrasi / Surat"
                                    ).forEach { cat ->
                                        AssistChip(
                                            onClick = { visitCategory = cat },
                                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Checkbox(
                                        checked = pdpConsentChecked,
                                        onCheckedChange = { pdpConsentChecked = it }
                                    )
                                    Text(
                                        text = "Saya menyetujui pemrosesan data identitas dasar untuk keperluan reservasi antrean sesuai UU PDP No. 27/2022 (Versi PDP-v1.0-2026).",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                if (selectedSlotObj != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = SaasTimeAndSlotEngine.buildPrivacySafeNotificationMessage(
                                                    clinicName = activeTenant?.clinicName ?: "KlinikKu",
                                                    bookingRef = "P1-XXXX",
                                                    queueCode = "Q-XX",
                                                    slotUtcMillis = selectedSlotObj.slotStartUtcMillis,
                                                    zone = selectedZone
                                                ),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (selectedSlotObj != null) {
                                                onBookSlot(
                                                    activeRule,
                                                    patientNameInput,
                                                    visitCategory,
                                                    selectedSlotObj.slotStartUtcMillis,
                                                    selectedSlotObj.slotEndUtcMillis
                                                )
                                            }
                                        },
                                        enabled = pdpConsentChecked && selectedSlotObj != null && !selectedSlotObj.isBreakTime,
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .testTag("saas_book_slot_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Kunci Slot (UTC)")
                                    }

                                    OutlinedButton(
                                        onClick = { onExecutePdpErasure(patientNameInput) },
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("pdp_erase_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Hapus Data PDP")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            UserRole.STAFF_DOCTOR -> {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Konsol Dokter & Staf Klinik (Tablet Mode)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Atur slot dinamis, uji bentrok 2 request bersamaan, dan sapu otomatis pasien No-Show.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            if (activeRule != null) {
                                Text(
                                    text = "Durasi Layanan Dinamis (${activeRule.doctorName}):",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(15, 20, 30, 45).forEach { mins ->
                                        FilterChip(
                                            selected = activeRule.slotDurationMinutes == mins,
                                            onClick = { onUpdateSlotDuration(activeRule, mins) },
                                            label = { Text("$mins Menit") }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (activeTenant != null) {
                                    OutlinedButton(
                                        onClick = { onToggleHoliday(activeTenant) },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            if (activeTenant.isHolidayClosed) "Buka Kembali Klinik" else "Set Hari Libur Klinik"
                                        )
                                    }
                                }
                                Button(
                                    onClick = onRunNoShowSweeper,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("run_noshow_sweeper_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sapu No-Show")
                                }
                            }
                        }
                    }
                }
            }

            UserRole.SUPER_ADMIN -> {
                item {
                    val totalMrr = tenants.filter { it.subscriptionActive }.sumOf { it.monthlyFeeIdr }
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Super Admin SaaS Multi-Tenant",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Total Pendapatan Berulang Bulanan (MRR): ${ClinicFormatters.formatRupiah(totalMrr)} / bulan",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            tenants.forEach { t ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${t.clinicName} (${t.city})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Tenant ID: ${t.tenantId} •Zona: ${t.defaultZoneCode} • Paket: ${t.subscriptionPlan}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = ClinicFormatters.formatRupiah(t.monthlyFeeIdr),
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Anti-Double Booking Concurrency Test Lab (Always accessible for verification)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Uji Anti-Double Booking (2 Request Bersamaan)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Menembakkan 2 transaksi paralel (Coroutines async) pada milidetik yang sama ke slot UTC yang sama. Database UNIQUE INDEX + @Transaction wajib menerima 1 dan menolak yang lain (409 Conflict).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (activeRule != null && selectedSlotObj != null) {
                                onRunConcurrencyTest(
                                    activeRule,
                                    selectedSlotObj.slotStartUtcMillis,
                                    selectedSlotObj.slotEndUtcMillis
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("run_concurrency_test_button")
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tembakkan 2 Request Bersamaan ke Slot Terpilih")
                    }

                    if (!concurrencyReport.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = concurrencyReport,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Tenant-Isolated Reservations List (Fase 1)
        item {
            Text(
                text = "Daftar Reservasi Tenant Aktif (${tenantReservations.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(tenantReservations, key = { it.id }) { res ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${res.bookingRef} • Antrean ${res.queueCode}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = res.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pasien: ${res.patientDisplayName} (${res.patientPhoneHash})",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Dokter: ${res.doctorName} • Kategori: ${res.visitCategory}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Waktu Lokal (${selectedZone.code}): ${SaasTimeAndSlotEngine.formatUtcMillisToZone(res.slotStartUtcMillis, selectedZone)}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Waktu DB (UTC): ${SaasTimeAndSlotEngine.formatIsoUtc(res.slotStartUtcMillis)} • LockKey=${res.activeLockKey}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (res.status == "BOOKED" || res.status == "CHECKED_IN") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onUpdateReservationStatus(res, "CHECKED_IN") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Check-In", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { onUpdateReservationStatus(res, "COMPLETED") },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Selesai", style = MaterialTheme.typography.labelSmall)
                            }
                            OutlinedButton(
                                onClick = { onUpdateReservationStatus(res, "CANCELLED") },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Batal (Lepas Slot)", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // 5. UU PDP Audit Log Trail
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Audit Log Kepatuhan UU PDP No. 27/2022",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    auditLogs.take(6).forEach { log ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(
                                text = "[${log.actorRole}] ${log.actionType} • Tenant: ${log.tenantId}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${log.targetResource} (${SaasTimeAndSlotEngine.formatIsoUtc(log.timestampUtcMillis)})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
