package lt.tacreports

import lt.tacreports.model.Defaults
import lt.tacreports.model.Field
import lt.tacreports.model.FieldKind
import lt.tacreports.model.ReportText
import lt.tacreports.model.Template
import lt.tacreports.model.TemplateJson
import lt.tacreports.model.Choice
import lt.tacreports.model.toggleChoice
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportTextTest {
    private val t = Template(
        name = "T",
        fields = listOf(
            Field(id = "kam", label = "Kam"),
            Field(id = "type", label = "Raportas", kind = FieldKind.FIXED, value = "SITREP"),
            Field(id = "h1", label = "B Savos pajėgos", kind = FieldKind.HEADER),
            Field(id = "b1", label = "1 Vieta"),
            Field(id = "b2", label = "2 Veiksmai"),
            Field(id = "h2", label = "C Priešo pajėgos", kind = FieldKind.HEADER),
            Field(id = "c1", label = "1 Dydis"),
            Field(id = "end", kind = FieldKind.HEADER),
            Field(id = "e", label = "E Papildoma informacija"),
        ),
    )

    @Test fun onePointPerLineAndEmptySkipped() {
        val text = ReportText.render(t, mapOf("kam" to " VILKAS ", "b1" to "35U LA 89106 61342", "e" to "nieko"))
        assertEquals(
            "Kam: VILKAS\nRaportas: SITREP\nB Savos pajėgos\n1 Vieta: 35U LA 89106 61342\nE Papildoma informacija: nieko",
            text,
        )
    }

    @Test fun headerShownWhenSectionHasValue() {
        val text = ReportText.render(t, mapOf("c1" to "skyrius"))
        assertEquals("Raportas: SITREP\nC Priešo pajėgos\n1 Dydis: skyrius", text)
    }

    @Test fun jsonRoundTrip() {
        val all = Defaults.all()
        assertEquals(all, TemplateJson.fromJson(TemplateJson.toJson(all)))
        assertEquals(listOf(t), TemplateJson.fromJson("Shared text before {\"fields\":[]} ".replace("{\"fields\":[]}", TemplateJson.toJson(listOf(t)))))
    }

    @Test fun notOurs() {
        assertEquals(null, TemplateJson.fromJson("hello"))
        assertEquals(null, TemplateJson.fromJson("{\"a\":1}"))
    }

    @Test fun choices() {
        assertEquals(Choice("A", "A – Neatidėliotini"), Choice.parse("A = Neatidėliotini"))
        assertEquals(Choice("skyrius", "skyrius"), Choice.parse(" skyrius "))
        assertEquals("A C", toggleChoice("A", "C"))
        assertEquals("C", toggleChoice("A C", "A"))
        assertEquals("A2 C", toggleChoice("A2", "C"))
    }
}
