package lt.tacreports.model

/** The example templates from the owner's printed report cards (Lithuanian). */
object Defaults {

    private fun header(type: String) = listOf(
        Field(label = "Kam", remember = true),
        Field(label = "Nuo", remember = true),
        Field(label = "Raportas", kind = FieldKind.FIXED, value = type),
        Field(label = "Laikas", value = "DTG"),
        Field(label = "Raporto Nr."),
    )

    private fun section(title: String) = Field(label = title, kind = FieldKind.HEADER)

    /** An empty header ends the section above it. */
    private val END get() = section("")
    private fun input(label: String, hint: String = "", vararg choices: String) =
        Field(label = label, value = hint, choices = choices.toList())

    fun all(): List<Template> = listOf(sitrep(), contrep(), medevac(), persrep())

    fun sitrep() = Template(
        id = "sitrep", name = "SITREP",
        fields = header("SITREP") + listOf(
            section("B Savos pajėgos"),
            input("1 Vieta", "MGRS"),
            input("2 Veiksmai"),
            input("3 Statusas", "amunicija, sužeistieji, ekipuotė"),
            section("C Priešo pajėgos"),
            input("1 Dydis"),
            input("2 Veiksmai"),
            input("3 Vieta", "MGRS"),
            END,
            input("D Vado ketinimai ir pasiūlymai"),
            input("E Papildoma informacija"),
        ),
    )

    fun contrep() = Template(
        id = "contrep", name = "CONTREP",
        fields = header("CONTREP") + listOf(
            input("A Pranešimo siuntėjo vieta", "MGRS"),
            section("B"),
            input("S Dydis", "", "grandis", "skyrius", "būrys", "kuopa"),
            input("A Priešo veiksmai"),
            input("L Priešo koordinatės", "MGRS"),
            input("U Priešo padalinys", "", "žvalgai", "pėst.", "mech."),
            input("T Kontakto laikas", "DTG"),
            input("E Priešo turima įranga"),
            END,
            input("C Savų pajėgų veiksmai"),
            input("D Papildoma informacija"),
        ),
    )

    fun medevac() = Template(
        id = "medevac", name = "MEDEVAC",
        fields = header("MEDEVAC") + listOf(
            input("1 Surinkimo vieta", "MGRS"),
            input("2 Dažnis, šaukinys, slaptažodis"),
            input(
                "3 Sužeistųjų skaičius pagal pirmenybę", "pvz. A2 C1",
                "A = Neatidėliotini (iki 2 val)", "B = Neatid.-chirurginiai (2 val)",
                "C = Skubūs (per 4 val)", "D = Įprastinė evak. (per 24 val)", "E = Pagal galimybę",
            ),
            input(
                "4 Reikalinga speciali įranga", "",
                "A = Nereikalinga", "B = Keltuvai", "C = Neštuvai", "D = Dirbt. plaučių ventiliavimas",
            ),
            input("5 Sužeistųjų skaičius pagal tipus", "pvz. L1 A2", "L = Gulintys (neštuvuose)", "A = Ambulatoriniai (sėdintys)"),
            input(
                "6 Surinkimo vietos pavojaus laipsnis", "",
                "N = Rajone priešo nėra", "P = Priešas gali būti rajone",
                "E = Priešas rajone", "X = Priešas rajone, reikia ginkl. palydos",
            ),
            input(
                "7 Surinkimo vietos pažymėjimo būdai", "",
                "A = Spalvotas ženklas", "B = Pirotechnika", "C = Dūmai",
                "D = Nėra", "E = Kitais būdais", "F = Pasitiks žmogus",
            ),
            input(
                "8 Sužeistųjų tautybė ir statusas", "",
                "A = LT karys", "B = LT civilis", "C = Ne LT karys", "D = Ne LT civilis", "E = Priešo karo belaisvis",
            ),
            input("9 MNG (CBRN) situacija ir užterštumas", "", "A = Branduolinis", "B = Biologinis", "C = Cheminis", "D = Nėra"),
        ),
    )

    fun persrep() = Template(
        id = "persrep", name = "PERSREP",
        fields = header("PERSREP") + listOf(
            input("A Padalinio identifikacija"),
            section("B Personalo duomenys (K/P/E/Iš viso)"),
            input("1 Etatinis pajėgumas (WE)", "K/P/E/Iš viso"),
            input("2 Priskirti", "K/P/E/Iš viso"),
            input("3 Esamas pajėgumas", "K/P/E/Iš viso"),
            input("4 Nedarbingi / sužeisti", "K/P/E/Iš viso"),
            input("5 Žuvę", "K/P/E/Iš viso"),
            input("6 Karo belaisviai (PW)", "K/P/E/Iš viso"),
            END,
            input("C Laikas", "DTG"),
            input("D Personalo vertinimas", "spalva"),
            input("E Vado vertinimas"),
        ),
    )
}
