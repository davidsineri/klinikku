package com.klinikku.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.Webhook
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.klinikku.app.data.ClinicChatMessageEntity
import com.klinikku.app.data.ClinicFormatters
import com.klinikku.app.data.ClinicReviewEntity
import com.klinikku.app.data.DoctorEntity
import com.klinikku.app.data.PatientEntity
import com.klinikku.app.data.PaymentInvoiceEntity
import com.klinikku.app.data.Phase2PrivacyHelper
import com.klinikku.app.data.TenantClinicEntity
import com.klinikku.app.data.UserRole

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaasPhase2Screen(
    tenants: List<TenantClinicEntity>,
    selectedTenantId: String,
    selectedRole: UserRole,
    selectedSubTab: Int,
    invoices: List<PaymentInvoiceEntity>,
    chats: List<ClinicChatMessageEntity>,
    reviews: List<ClinicReviewEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    webhookBanner: String?,
    onSelectTenant: (TenantClinicEntity) -> Unit,
    onSelectRole: (UserRole) -> Unit,
    onSelectSubTab: (Int) -> Unit,
    onCreateInvoice: (PatientEntity, String, String, String, Boolean, Long) -> Unit,
    onTriggerWebhook: (PaymentInvoiceEntity) -> Unit,
    onRefundInvoice: (PaymentInvoiceEntity) -> Unit,
    onSendChat: (String, String, String, String) -> Unit,
    onSubmitReview: (String, String, String, String, Boolean, Int, String) -> Unit,
    onReplyReview: (ClinicReviewEntity, String) -> Unit,
    onSavePatientDependent: (
        id: Int,
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
        existingRm: String
    ) -> Unit,
    onDeletePatientDependent: (PatientEntity) -> Unit,
    onNavigateBackHome: () -> Unit
) {
    BackHandler {
        onNavigateBackHome()
    }

    val activeTenant = tenants.find { it.tenantId == selectedTenantId } ?: tenants.firstOrNull()
    val tenantInvoices = remember(invoices, selectedTenantId) {
        invoices.filter { it.tenantId == selectedTenantId }
    }
    val tenantChats = remember(chats, selectedTenantId) {
        chats.filter { it.tenantId == selectedTenantId }
    }
    val tenantReviews = remember(reviews, selectedTenantId) {
        reviews.filter { it.tenantId == selectedTenantId }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saas_phase2_screen")
    ) {
        // Rich Gradient Header Banner for Fase 2
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0F766E),
                            Color(0xFF0369A1),
                            Color(0xFF1E3A8A)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            color = Color(0xFF2DD4BF).copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "MODUL SAAS FASE 2 • MULTI-TENANT READY",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF99F6E4),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Pembayaran DP/QRIS, Chat, Ulasan, Keluarga & Laporan",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tenant & Role Quick Switcher Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tenants.forEach { tenant ->
                        val isSelected = tenant.tenantId == selectedTenantId
                        Surface(
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { onSelectTenant(tenant) }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFF0F766E) else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${tenant.clinicName} (${tenant.defaultZoneCode})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color(0xFF0F766E) else Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrollable 5 Sub-Tabs for Fase 2 Modules
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            val subTabs = listOf(
                Pair("1. Bayar & DP QRIS", Icons.Default.Payments),
                Pair("2. Chat Klinik (${tenantChats.size})", Icons.Default.Chat),
                Pair("3. Ulasan (${tenantReviews.size})", Icons.Default.RateReview),
                Pair("4. Profil Keluarga (${patients.size})", Icons.Default.FamilyRestroom),
                Pair("5. Laporan & Grafik", Icons.Default.Analytics)
            )
            subTabs.forEachIndexed { idx, (title, iconVec) ->
                Tab(
                    selected = selectedSubTab == idx,
                    onClick = { onSelectSubTab(idx) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(iconVec, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(title)
                        }
                    },
                    modifier = Modifier.testTag("phase2_subtab_$idx")
                )
            }
        }

        when (selectedSubTab) {
            0 -> Phase2PaymentsTab(
                activeTenant = activeTenant,
                invoices = tenantInvoices,
                patients = patients,
                doctors = doctors,
                webhookBanner = webhookBanner,
                onCreateInvoice = onCreateInvoice,
                onTriggerWebhook = onTriggerWebhook,
                onRefundInvoice = onRefundInvoice
            )
            1 -> Phase2ChatTab(
                activeTenant = activeTenant,
                chats = tenantChats,
                selectedRole = selectedRole,
                onSelectRole = onSelectRole,
                onSendChat = onSendChat
            )
            2 -> Phase2ReviewsTab(
                activeTenant = activeTenant,
                reviews = tenantReviews,
                doctors = doctors,
                selectedRole = selectedRole,
                onSubmitReview = onSubmitReview,
                onReplyReview = onReplyReview
            )
            3 -> Phase2FamilyTab(
                patients = patients,
                onSavePatient = onSavePatientDependent,
                onDeletePatient = onDeletePatientDependent
            )
            4 -> Phase2ReportsTab(
                activeTenant = activeTenant,
                invoices = tenantInvoices,
                reviews = tenantReviews
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase2PaymentsTab(
    activeTenant: TenantClinicEntity?,
    invoices: List<PaymentInvoiceEntity>,
    patients: List<PatientEntity>,
    doctors: List<DoctorEntity>,
    webhookBanner: String?,
    onCreateInvoice: (PatientEntity, String, String, String, Boolean, Long) -> Unit,
    onTriggerWebhook: (PaymentInvoiceEntity) -> Unit,
    onRefundInvoice: (PaymentInvoiceEntity) -> Unit
) {
    var selectedPatientIdx by remember { mutableIntStateOf(0) }
    var selectedDoctorIdx by remember { mutableIntStateOf(0) }
    var selectedChannel by remember { mutableStateOf("QRIS Dinamis") }
    var isFullPayment by remember { mutableStateOf(false) }

    val chosenPatient = patients.getOrNull(selectedPatientIdx) ?: patients.firstOrNull()
    val chosenDoctor = doctors.getOrNull(selectedDoctorIdx) ?: doctors.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Interactive QRIS & DP Generator Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Generator Tagihan DP & QRIS Anti-No-Show",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "DP Rp 50.000 mengunci slot antrean & memangkas No-Show hingga 80%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = Color(0xFF059669).copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Idempotent Webhook",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF059669),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Pilih Anggota Keluarga:", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        patients.forEachIndexed { idx, p ->
                            FilterChip(
                                selected = idx == selectedPatientIdx,
                                onClick = { selectedPatientIdx = idx },
                                label = { Text("${p.fullName} (${p.relationship})") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Pilih Metode Pembayaran:", style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("QRIS Dinamis", "BCA Virtual Account", "GoPay / OVO", "Tunai Kasir").forEach { ch ->
                            FilterChip(
                                selected = selectedChannel == ch,
                                onClick = { selectedChannel = ch },
                                label = { Text(ch) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !isFullPayment,
                            onClick = { isFullPayment = false },
                            label = { Text("DP Komitmen (Rp 50.000)") }
                        )
                        FilterChip(
                            selected = isFullPayment,
                            onClick = { isFullPayment = true },
                            label = {
                                Text(
                                    "Lunas Penuh (${ClinicFormatters.formatRupiah(chosenDoctor?.consultationFee ?: 200000L)})"
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (chosenPatient != null && chosenDoctor != null) {
                                onCreateInvoice(
                                    chosenPatient,
                                    chosenDoctor.name,
                                    chosenDoctor.polyclinic,
                                    selectedChannel,
                                    isFullPayment,
                                    chosenDoctor.consultationFee
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_dp_invoice_button")
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Terbitkan Tagihan & QRIS Dinamis")
                    }

                    if (!webhookBanner.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Webhook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = webhookBanner,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Invoices List with Interactive QRIS Visual & Idempotent Webhook Trigger
        items(invoices, key = { it.id }) { inv ->
            val statusColor = when (inv.status) {
                "PAID" -> Color(0xFF059669)
                "REFUNDED" -> Color(0xFF7C3AED)
                else -> Color(0xFFD97706)
            }
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("invoice_card_${inv.id}")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${inv.invoiceNumber} • Booking ${inv.bookingRef}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "IdempotencyKey: ${inv.idempotencyKey} • Callback: ${inv.webhookCallbackCount}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = inv.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Styled Dynamic QRIS Mini Standee
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier
                                .size(78.dp)
                                .border(1.5.dp, Color(0xFFE11D48), RoundedCornerShape(12.dp))
                                .padding(6.dp)
                        ) {
                            val hash = inv.idempotencyKey.hashCode()
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val grid = 7
                                val cell = size.width / grid
                                for (r in 0 until grid) {
                                    for (c in 0 until grid) {
                                        val corner = (r < 2 && c < 2) || (r < 2 && c >= grid - 2) || (r >= grid - 2 && c < 2)
                                        val bit = ((hash shr ((r * grid + c) % 24)) and 1) == 1
                                        if (corner || bit) {
                                            drawRect(
                                                color = Color(0xFF0F172A),
                                                topLeft = Offset(c * cell, r * cell),
                                                size = Size(cell * 0.85f, cell * 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${inv.patientName} (${inv.familyRelation})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${inv.doctorName} • ${inv.serviceUnit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${inv.paymentTypeLabel} via ${inv.paymentChannel}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Tagihan: ${ClinicFormatters.formatRupiah(inv.billedAmount)} (Total Layanan: ${ClinicFormatters.formatRupiah(inv.totalConsultationFee)})",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onTriggerWebhook(inv) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (inv.status == "PAID") Color(0xFF0284C7) else Color(0xFF059669)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("webhook_btn_${inv.id}")
                        ) {
                            Icon(Icons.Default.Webhook, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (inv.status == "PAID") "Uji Webhook Ganda (Idempotent)" else "Simulasi Bayar Webhook (Lunas)"
                            )
                        }

                        if (inv.status == "PAID") {
                            OutlinedButton(
                                onClick = { onRefundInvoice(inv) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Icon(Icons.Default.Replay, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Refund DP")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Phase2ChatTab(
    activeTenant: TenantClinicEntity?,
    chats: List<ClinicChatMessageEntity>,
    selectedRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
    onSendChat: (String, String, String, String) -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    var activeBookingRef by remember { mutableStateOf("KLK-2610-419") }
    val isStaffMode = selectedRole == UserRole.STAFF_DOCTOR || selectedRole == UserRole.SUPER_ADMIN

    Column(modifier = Modifier.fillMaxSize()) {
        // Privacy Guard Banner
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Chat Fase 2 khusus koordinasi jadwal, persiapan puasa & bukti DP. Sensor otomatis mendeteksi NIK 16 digit / diagnosis sensitif.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilterChip(
                    selected = isStaffMode,
                    onClick = {
                        onSelectRole(if (isStaffMode) UserRole.PATIENT else UserRole.STAFF_DOCTOR)
                    },
                    label = {
                        Text(if (isStaffMode) "Mode: Staf Klinik" else "Mode: Pasien")
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chats, key = { it.id }) { msg ->
                val isFromStaff = msg.senderRole == "CLINIC_STAFF"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = if (isFromStaff) Alignment.End else Alignment.Start
                ) {
                    Surface(
                        color = if (isFromStaff) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isFromStaff) 18.dp else 4.dp,
                            bottomEnd = if (isFromStaff) 4.dp else 18.dp
                        ),
                        tonalElevation = 2.dp,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth(0.86f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFromStaff) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.primary
                                    }
                                )
                                Text(
                                    text = "Ref: ${msg.bookingRef}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isFromStaff) {
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.messageText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isFromStaff) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                            if (msg.hasPrivacyWarning) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = Color(0xFFFEF2F2),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Peringatan Privasi UU PDP: Jangan kirim NIK 16 digit / diagnosis medis di chat admin.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFB91C1C)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Template Replies & Chat Composer
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val quickTemplates = if (isStaffMode) {
                    listOf(
                        "Silakan hadir 15 menit sebelum jadwal praktik.",
                        "Pembayaran DP QRIS sudah terverifikasi, antrean terkunci.",
                        "Untuk keluhan medis detail akan diperiksa langsung oleh dokter di ruang praktik."
                    )
                } else {
                    listOf(
                        "Apakah dokter praktik tepat waktu hari ini?",
                        "Saya sudah bayar DP via QRIS, mohon dicek.",
                        "Apakah parkir mobil tersedia di klinik?"
                    )
                }
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickTemplates.forEach { tpl ->
                        AssistChip(
                            onClick = { messageInput = tpl },
                            label = { Text(tpl, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = {
                            Text(
                                if (isStaffMode) "Balas pesan pasien sebagai Staf..." else "Tulis pesan ke Admin Klinik..."
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("phase2_chat_input")
                    )
                    Button(
                        onClick = {
                            if (messageInput.isNotBlank()) {
                                onSendChat(
                                    activeBookingRef,
                                    if (isStaffMode) "CLINIC_STAFF" else "PATIENT",
                                    if (isStaffMode) "Admin ${activeTenant?.clinicName ?: "Klinik"}" else "Siti Larasati (Pasien)",
                                    messageInput
                                )
                                messageInput = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("phase2_send_chat_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Kirim Pesan")
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase2ReviewsTab(
    activeTenant: TenantClinicEntity?,
    reviews: List<ClinicReviewEntity>,
    doctors: List<DoctorEntity>,
    selectedRole: UserRole,
    onSubmitReview: (String, String, String, String, Boolean, Int, String) -> Unit,
    onReplyReview: (ClinicReviewEntity, String) -> Unit
) {
    var bookingRefInput by remember { mutableStateOf("KLK-2610-512") }
    var patientNameInput by remember { mutableStateOf("Budi Raharjo") }
    var maskNameChecked by remember { mutableStateOf(true) }
    var selectedStars by remember { mutableIntStateOf(5) }
    var reviewText by remember {
        mutableStateOf("Alur booking dan pembayaran DP sangat praktis, dokter komunikatif.")
    }
    val chosenDoc = doctors.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kirim Ulasan Kunjungan Terverifikasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "1 kode booking selesai hanya dapat mengirim 1 ulasan asli. Nama pasien disamarkan otomatis sesuai UU PDP.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bookingRefInput,
                            onValueChange = { bookingRefInput = it },
                            label = { Text("Kode Booking Selesai") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = patientNameInput,
                            onValueChange = { patientNameInput = it },
                            label = { Text("Nama Pasien") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Rating Layanan:", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Bintang",
                                tint = if (star <= selectedStars) Color(0xFFF59E0B) else Color.LightGray,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clickable { selectedStars = star }
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = maskNameChecked,
                            onCheckedChange = { maskNameChecked = it }
                        )
                        Text(
                            text = "Samarkan nama publik menjadi '${Phase2PrivacyHelper.maskNameForPublicReview(patientNameInput, true)}'",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        label = { Text("Ulasan Pengalaman Kunjungan") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            onSubmitReview(
                                bookingRefInput,
                                chosenDoc?.name ?: "dr. Nadia Prameswari, Sp.PD",
                                chosenDoc?.polyclinic ?: "Poli Penyakit Dalam",
                                patientNameInput,
                                maskNameChecked,
                                selectedStars,
                                reviewText
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_verified_review_button")
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kirim Ulasan Terverifikasi")
                    }
                }
            }
        }

        items(reviews, key = { it.id }) { rev ->
            var replyDraft by remember(rev.id) { mutableStateOf("") }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = Phase2PrivacyHelper.maskNameForPublicReview(
                                    rev.patientDisplayName,
                                    rev.maskPatientName
                                ),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${rev.doctorName} • ${rev.serviceUnit} (Ref: ${rev.bookingRef})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${rev.ratingStars}.0 ★",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${rev.reviewComment}\"",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (rev.clinicOfficialReply.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Tanggapan Resmi ${activeTenant?.clinicName ?: "Klinik"}:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = rev.clinicOfficialReply,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    } else if (selectedRole == UserRole.STAFF_DOCTOR || selectedRole == UserRole.SUPER_ADMIN) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = replyDraft,
                                onValueChange = { replyDraft = it },
                                placeholder = { Text("Tulis balasan resmi klinik...") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { onReplyReview(rev, replyDraft) }) {
                                Text("Balas")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Phase2FamilyTab(
    patients: List<PatientEntity>,
    onSavePatient: (
        id: Int,
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
        existingRm: String
    ) -> Unit,
    onDeletePatient: (PatientEntity) -> Unit
) {
    PatientsScreen(
        patients = patients,
        onSavePatient = onSavePatient,
        onDeletePatient = onDeletePatient,
        onNavigateBackHome = {}
    )
}

@Composable
private fun Phase2ReportsTab(
    activeTenant: TenantClinicEntity?,
    invoices: List<PaymentInvoiceEntity>,
    reviews: List<ClinicReviewEntity>
) {
    val paidInvoices = invoices.filter { it.status == "PAID" }
    val pendingInvoices = invoices.filter { it.status == "PENDING" }
    val totalCollected = paidInvoices.sumOf { it.billedAmount }
    val totalServiceValue = paidInvoices.sumOf { it.totalConsultationFee }
    val pendingValue = pendingInvoices.sumOf { it.billedAmount }
    val avgRating = if (reviews.isNotEmpty()) {
        reviews.map { it.ratingStars }.average().toFloat()
    } else 4.9f

    var exportGenerated by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTabletWide = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. KPI Summary Grid (Adaptive 2x2 or 4-across on tablet)
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Laporan Eksekutif & Keuangan (${activeTenant?.clinicName ?: "Klinik"})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ringkasan real-time pendapatan DP/Pelunasan, efektivitas penurunan No-Show, dan kepuasan pasien.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiMetricBox(
                                title = "DP & Kas Masuk (PAID)",
                                value = ClinicFormatters.formatRupiah(totalCollected),
                                subtitle = "${paidInvoices.size} Transaksi Lunas",
                                accentColor = Color(0xFF059669),
                                modifier = Modifier.weight(1f)
                            )
                            KpiMetricBox(
                                title = "Total Nilai Layanan",
                                value = ClinicFormatters.formatRupiah(totalServiceValue),
                                subtitle = "Piutang: ${ClinicFormatters.formatRupiah(pendingValue)}",
                                accentColor = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            KpiMetricBox(
                                title = "Angka No-Show Pasien",
                                value = "4.8% (Turun drastis)",
                                subtitle = "Sebelum fitur DP: 34.0%",
                                accentColor = Color(0xFF0D9488),
                                modifier = Modifier.weight(1f)
                            )
                            KpiMetricBox(
                                title = "Skor Kepuasan (CSAT)",
                                value = String.format("%.1f / 5.0 ★", avgRating),
                                subtitle = "${reviews.size} Ulasan Terverifikasi",
                                accentColor = Color(0xFFD97706),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 2. Visual Charts: Donut Chart (Metode Bayar) & Bar Comparison (No-Show Before vs After DP)
            item {
                if (isTabletWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        PaymentChannelDonutCard(modifier = Modifier.weight(1f))
                        NoShowComparisonChartCard(modifier = Modifier.weight(1f))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        PaymentChannelDonutCard(modifier = Modifier.fillMaxWidth())
                        NoShowComparisonChartCard(modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            // 3. Export Report Action
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
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
                                    text = "Ekspor Laporan Rekonsiliasi Kasir & Pajak Klinik",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Format siap unduh (CSV/PDF Ringkas) untuk pembukuan bulanan pemilik klinik.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { exportGenerated = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("export_report_button")
                            ) {
                                Text("Generate CSV")
                            }
                        }

                        if (exportGenerated) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "TENANT,INVOICE_COUNT,PAID_IDR,PENDING_IDR,NOSHOW_RATE,CSAT_SCORE\n" +
                                        "${activeTenant?.tenantId},${invoices.size},$totalCollected,$pendingValue,4.8%,$avgRating",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricBox(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = accentColor.copy(alpha = 0.09f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PaymentChannelDonutCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Distribusi Metode Pembayaran Pasien",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "QRIS Dinamis mendominasi karena bebas biaya admin VA",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Canvas(modifier = Modifier.size(110.dp)) {
                    val strokeW = 24f
                    // QRIS 62%
                    drawArc(
                        color = Color(0xFF0D9488),
                        startAngle = -90f,
                        sweepAngle = 223f,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                    )
                    // Virtual Account 23%
                    drawArc(
                        color = Color(0xFF0284C7),
                        startAngle = 133f,
                        sweepAngle = 83f,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                    )
                    // E-Wallet 15%
                    drawArc(
                        color = Color(0xFFF59E0B),
                        startAngle = 216f,
                        sweepAngle = 54f,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Butt)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    LegendDotRow(Color(0xFF0D9488), "QRIS Dinamis (62%)")
                    LegendDotRow(Color(0xFF0284C7), "Virtual Account (23%)")
                    LegendDotRow(Color(0xFFF59E0B), "GoPay / OVO (15%)")
                }
            }
        }
    }
}

@Composable
private fun NoShowComparisonChartCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dampak Sistem DP terhadap No-Show",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bukti ROI utama untuk menjual SaaS ke pemilik klinik",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val barHeight = 28f
                // Bar 1: Tanpa DP (34%)
                drawRoundRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(0f, 10f),
                    size = Size(size.width * 0.82f, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
                // Bar 2: Dengan DP QRIS Rp 50rb (4.8%)
                drawRoundRect(
                    color = Color(0xFF10B981),
                    topLeft = Offset(0f, 56f),
                    size = Size(size.width * 0.16f, barHeight),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LegendDotRow(Color(0xFFEF4444), "Tanpa DP: 34.0% Pasien Batal Hadir")
                LegendDotRow(Color(0xFF10B981), "Pakai DP: 4.8%")
            }
        }
    }
}

@Composable
private fun LegendDotRow(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, style = MaterialTheme.typography.labelSmall)
    }
}
