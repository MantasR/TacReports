import Shared
import SwiftUI

/// Hosts the shared Compose UI; everything else lives in Kotlin (shared/src/iosMain).
@main
struct TacReportsApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
                // tacreports://new opens the report picker (e.g. from a Shortcut or Back Tap).
                .onOpenURL { _ in MainViewControllerKt.openReportPicker() }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
