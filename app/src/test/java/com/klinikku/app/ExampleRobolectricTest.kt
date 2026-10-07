package com.klinikku.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.klinikku.app.data.ClinicFormatters
import com.klinikku.app.data.Phase3ClinicalSafetyEngine
import com.klinikku.app.data.Phase4IntegrationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("KlinikKu", appName)
    }

    @Test
    fun `format rupiah and polyclinic prefix works`() {
        assertEquals("Rp 0 (Ditanggung)", ClinicFormatters.formatRupiah(0L))
        assertTrue(ClinicFormatters.formatRupiah(150000L).contains("150"))
        assertEquals("B", ClinicFormatters.polyclinicPrefix("Poli Gigi & Mulut"))
        assertEquals("D", ClinicFormatters.polyclinicPrefix("Poli Penyakit Dalam"))
    }

    @Test
    fun `phase 3 clinical safety engine detects penicillin allergy conflict`() {
        val (conflict, msg) = Phase3ClinicalSafetyEngine.checkDrugAllergyConflict(
            patientAllergies = "Antibiotik Golongan Penisilin",
            medicationListText = "Amoxicillin 500mg Kaplet (3x1)|Paracetamol 500mg"
        )
        assertTrue(conflict)
        assertTrue(msg.contains("BAHAYA ALERGI OBAT"))

        val (safeConflict, _) = Phase3ClinicalSafetyEngine.checkDrugAllergyConflict(
            patientAllergies = "Antibiotik Golongan Penisilin",
            medicationListText = "Clindamycin 300mg|Paracetamol 500mg"
        )
        assertFalse(safeConflict)
    }

    @Test
    fun `phase 4 satusehat fhir bundle generator includes encounter condition loinc and kfa`() {
        val json = Phase4IntegrationEngine.buildSatusehatFhirBundleJson(
            bundleId = "FHIR-BND-TEST-01",
            orgId = "100028491",
            patientIhs = "P10293847501",
            practitionerIhs = "N10002938411",
            icd10Code = "K21.9",
            icd10Display = "GERD",
            loincCode = "2093-3",
            kfaCode = "93001019"
        )
        assertTrue(json.contains("\"resourceType\": \"Bundle\""))
        assertTrue(json.contains("\"resourceType\": \"Encounter\""))
        assertTrue(json.contains("\"code\": \"K21.9\""))
        assertTrue(json.contains("\"code\": \"2093-3\""))
        assertTrue(json.contains("\"code\": \"93001019\""))
    }
}
