import AppIntents
import Shared

/// "New report": opens the app on the report picker. iOS's stand-in for the Android bubble;
/// it shows up in the Shortcuts app and can be put on the Action Button or Back Tap.
struct NewReportIntent: AppIntent {
    static var title: LocalizedStringResource = "New report"
    static var description = IntentDescription("Open TacReports on the report picker.")
    static var openAppWhenRun: Bool = true

    @MainActor
    func perform() async throws -> some IntentResult {
        MainViewControllerKt.openReportPicker()
        return .result()
    }
}

struct TacReportsShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: NewReportIntent(),
            phrases: ["New report in \(.applicationName)", "\(.applicationName) report"],
            shortTitle: "New report",
            systemImageName: "doc.text"
        )
    }
}
