package com.klinikku.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.klinikku.app.data.ClinicFormatters

enum class CrisisScenarioId {
    OVERBOOKING_SURGE,
    DOCTOR_EMERGENCY_ABSENCE,
    MASS_NO_SHOW_GHOST,
    BPJS_GATEWAY_DOWNTIME,
    CRITICAL_TRIAGE_MISROUTE
}

data class CrisisScenarioItem(
    val id: CrisisScenarioId,
    val title: String,
    val category: String,
    val severityLabel: String,
    val severityScore: Int, // 1..100
    val triggerCondition: String,
    val worstCaseImpact: String,
    val operationalConsequence: String,
    val mitigationProtocol: String,
    val estimatedDelayMinutes: Int,
    val potentialLossRupiah: Long
)

val DefaultCrisisScenarios = listOf(
    CrisisScenarioItem(
        id = CrisisScenarioId.OVERBOOKING_SURGE,
        title = "1. Bentrok Slot & Overbooking Massal (Race Condition)",
        category = "Kapasitas & Konkurensi",
        severityLabel = "KRITIS TINGGI",
        severityScore = 92,
        triggerCondition = "Puluhan pasien menekan tombol 'Konfirmasi' pada jam praktik yang sama secara bersamaan, ditambah pasien walk-in datang tanpa kuota terpisah.",
        worstCaseImpact = "1 slot jam praktik terisi 4–6 pasien sekaligus. Ruang tunggu melampaui kapasitas fisik (>180%), waktu tunggu membengkak hingga >2,5 jam, dan memicu komplain terbuka di lobi.",
        operationalConsequence = "Dokter kelelahan (burnout), kualitas anamnesis turun drastis menjadi <4 menit/pasien, risiko salah diagnosis meningkat.",
        mitigationProtocol = "Atomic Database Slot-Locking saat pemilihan jam, pemisahan kuota Online (70%) vs Walk-In (30%), serta pembatasan kuota harian keras.",
        estimatedDelayMinutes = 115,
        potentialLossRupiah = 8500000L
    ),
    CrisisScenarioItem(
        id = CrisisScenarioId.DOCTOR_EMERGENCY_ABSENCE,
        title = "2. Dokter Spesialis Mendadak Absen / Tindakan Darurat",
        category = "Sumber Daya Medis",
        severityLabel = "KRITIS EKSTREM",
        severityScore = 96,
        triggerCondition = "Dokter spesialis dengan 18 pasien terjadwal (termasuk pasien MCU yang sudah puasa 10 jam) mendadak harus operasi darurat atau sakit.",
        worstCaseImpact = "Pasien yang sudah Check-In terlantar tanpa kepastian. Pasien lansia/diabetes yang berpuasa untuk cek lab berisiko mengalami hipoglikemia di ruang tunggu.",
        operationalConsequence = "Penumpukan tuntutan refund tunai/asuransi di kasir dan jatuhnya reputasi klinik secara instan.",
        mitigationProtocol = "Auto-Failover ke Dokter Spesialis Pengganti (Backup On-Call), pemisahan pengambilan sampel lab MCU oleh perawat tepat waktu agar puasa pasien tidak sia-sia, & kompensasi Reschedule Prioritas #1.",
        estimatedDelayMinutes = 140,
        potentialLossRupiah = 14200000L
    ),
    CrisisScenarioItem(
        id = CrisisScenarioId.MASS_NO_SHOW_GHOST,
        title = "3. Gelombang 'Ghost Booking' / Pasien No-Show Massal (65%)",
        category = "Efisiensi Kuota",
        severityLabel = "TINGGI",
        severityScore = 80,
        triggerCondition = "Pasien memesan banyak jadwal (terutama slot pagi & layanan gratis/BPJS) namun tidak datang tanpa membatalkan di aplikasi.",
        worstCaseImpact = "Aplikasi menampilkan 'Kuota Penuh' sehingga pasien yang benar-benar sakit parah ditolak sistem, padahal ruang praktik dokter justru kosong melompong.",
        operationalConsequence = "Klinik kehilangan pendapatan hingga 60% pada jam produktif sementara biaya operasional dokter & perawat tetap berjalan.",
        mitigationProtocol = "Auto-Release Slot jika pasien belum Check-In 15 menit sebelum jadwal, sistem Antrean Cadangan (Waitlist Otomatis), & penangguhan akun setelah 3x No-Show.",
        estimatedDelayMinutes = 35,
        potentialLossRupiah = 11800000L
    ),
    CrisisScenarioItem(
        id = CrisisScenarioId.BPJS_GATEWAY_DOWNTIME,
        title = "4. Server Verifikasi BPJS / Jaringan Rekam Medis Down",
        category = "Infrastruktur & Integrasi",
        severityLabel = "KRITIS TINGGI",
        severityScore = 89,
        triggerCondition = "API verifikasi kepesertaan BPJS (P-Care/VClaim) mengalami timeout pada jam sibuk (08:00–10:00 WIB) saat puluhan pasien hendak Check-In.",
        worstCaseImpact = "Mesin Kiosk Mandiri gagal mencetak tiket antrean. Pasien BPJS tidak bisa diverifikasi status rujukannya sehingga antrean mengular hingga ke luar gedung.",
        operationalConsequence = "Efek domino keterlambatan pada seluruh poliklinik dan potensi klaim layanan tidak tertagih.",
        mitigationProtocol = "Mode Offline-First Queue (Room Local Cache): izinkan Check-In & pemeriksaan medis berjalan menggunakan nomor antrean lokal, lalu lakukan sinkronisasi klaim di latar belakang saat server pulih.",
        estimatedDelayMinutes = 85,
        potentialLossRupiah = 9600000L
    ),
    CrisisScenarioItem(
        id = CrisisScenarioId.CRITICAL_TRIAGE_MISROUTE,
        title = "5. Pasien Kritis Masuk Antrean Reguler (Gagal Triase Darurat)",
        category = "Keselamatan Pasien (Patient Safety)",
        severityLabel = "FATAL / MEDIS",
        severityScore = 100,
        triggerCondition = "Pasien dengan gejala serangan jantung (nyeri dada kiri menjalar), sesak napas akut, atau kejang demam anak memesan Poli Umum/Dalam reguler dan duduk menunggu antrean.",
        worstCaseImpact = "Pasien mengalami henti jantung atau perburukan fatal di kursi ruang tunggu karena melewati Golden Hour penanganan darurat sambil menunggu nomor antrean dipanggil.",
        operationalConsequence = "Insiden keselamatan pasien fatal (Sentinel Event) dan tanggung jawab hukum medis.",
        mitigationProtocol = "Algoritma Red-Flag Symptom Screening wajib pada formulir keluhan: otomatis memblokir antrean reguler & mengaktifkan alarm pengalihan langsung ke IGD 24 Jam.",
        estimatedDelayMinutes = 60,
        potentialLossRupiah = 25000000L
    )
)

@Composable
fun CrisisSimulationSection(
    activeScenarioIds: Set<CrisisScenarioId>,
    lastMitigationLog: String?,
    onToggleScenario: (CrisisScenarioId) -> Unit,
    onTriggerAllCrises: () -> Unit,
    onMitigateAllCrises: () -> Unit
) {
    val activeScenarios = DefaultCrisisScenarios.filter { activeScenarioIds.contains(it.id) }
    val totalDelayMinutes = activeScenarios.sumOf { it.estimatedDelayMinutes }
    val totalRiskLoss = activeScenarios.sumOf { it.potentialLossRupiah }
    val stressPercent = if (activeScenarios.isEmpty()) {
        18
    } else {
        (25 + activeScenarios.sumOf { it.severityScore } / 3).coerceAtMost(100)
    }

    val statusColor = when {
        stressPercent >= 75 -> Color(0xFFDC2626)
        stressPercent >= 45 -> Color(0xFFD97706)
        else -> Color(0xFF059669)
    }

    val statusLabel = when {
        stressPercent >= 75 -> "SIAGA MERAH • KRISIS OPERASIONAL"
        stressPercent >= 45 -> "SIAGA KUNING • BEBAN TINGGI"
        else -> "NORMAL • TERKENDALI"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("crisis_simulation_list"),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 14.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Live Clinic Stress & Impact Telemetry Dashboard
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.5.dp, statusColor.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(statusColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = statusColor.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${activeScenarios.size}/5 Krisis Aktif",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Simulator Dampak Terburuk & Stress-Test Klinik",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aktifkan skenario di bawah untuk melihat dampak nyata pada antrean poliklinik, jadwal pasien, serta cara sistem memitigasinya.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stress Meter Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Indeks Beban & Risiko Sistem",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$stressPercent%",
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { stressPercent / 100f },
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(RoundedCornerShape(6.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Lonjakan Waktu Tunggu",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (totalDelayMinutes == 0) "+0 Menit (Normal)" else "+$totalDelayMinutes Menit",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalDelayMinutes > 0) statusColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Potensi Kerugian / Klaim",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (totalRiskLoss == 0L) "Rp 0" else ClinicFormatters.formatRupiah(totalRiskLoss),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (totalRiskLoss > 0L) statusColor else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onTriggerAllCrises,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFDC2626)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_all_crises_button")
                        ) {
                            Icon(
                                Icons.Default.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Krisis Total")
                        }

                        Button(
                            onClick = onMitigateAllCrises,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF059669),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.25f)
                                .testTag("mitigate_all_crises_button")
                        ) {
                            Icon(
                                Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mitigasi & Pulihkan")
                        }
                    }
                }
            }
        }

        // 2. Mitigation Result Log Banner (when executed)
        if (!lastMitigationLog.isNullOrBlank()) {
            item {
                AnimatedVisibility(visible = true) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFF15803D).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Laporan Eksekusi Protokol Mitigasi KlinikKu",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF14532D)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = lastMitigationLog,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF14532D)
                            )
                        }
                    }
                }
            }
        }

        // 3. Individual Worst-Case Scenario Cards
        items(DefaultCrisisScenarios, key = { it.id.name }) { scenario ->
            val isActive = activeScenarioIds.contains(scenario.id)
            CrisisScenarioCard(
                scenario = scenario,
                isActive = isActive,
                onToggle = { onToggleScenario(scenario.id) }
            )
        }
    }
}

@Composable
private fun CrisisScenarioCard(
    scenario: CrisisScenarioItem,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    val accentColor = if (isActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
    val iconVector: ImageVector = when (scenario.id) {
        CrisisScenarioId.OVERBOOKING_SURGE -> Icons.Default.Groups
        CrisisScenarioId.DOCTOR_EMERGENCY_ABSENCE -> Icons.Default.PersonOff
        CrisisScenarioId.MASS_NO_SHOW_GHOST -> Icons.Default.ErrorOutline
        CrisisScenarioId.BPJS_GATEWAY_DOWNTIME -> Icons.Default.CloudOff
        CrisisScenarioId.CRITICAL_TRIAGE_MISROUTE -> Icons.Default.Emergency
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                Color(0xFFFEF2F2)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("crisis_card_${scenario.id.name}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = accentColor.copy(alpha = 0.14f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${scenario.category} • ${scenario.severityLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isActive) {
                    Surface(
                        color = Color(0xFFDC2626),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "SEDANG TERJADI",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = scenario.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color(0xFF7F1D1D) else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))
            ScenarioDetailBlock(
                label = "Pemicu Kegagalan (Root Cause):",
                content = scenario.triggerCondition,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            ScenarioDetailBlock(
                label = "Dampak Terburuk ke Pasien & Klinik:",
                content = "${scenario.worstCaseImpact} ${scenario.operationalConsequence}",
                labelColor = Color(0xFFB91C1C)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Solusi & Arsitektur Mitigasi:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = scenario.mitigationProtocol,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Efek: Delay +${scenario.estimatedDelayMinutes} mnt",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Risiko: ${ClinicFormatters.formatRupiah(scenario.potentialLossRupiah)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onToggle,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) Color(0xFF059669) else Color(0xFFDC2626),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("toggle_crisis_${scenario.id.name}")
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.AutoFixHigh else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isActive) "Mitigasi Skenario" else "Simulasikan Dampak")
                }
            }
        }
    }
}

@Composable
private fun ScenarioDetailBlock(
    label: String,
    content: String,
    labelColor: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
