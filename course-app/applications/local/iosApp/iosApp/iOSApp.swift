import SwiftUI
import AppApplicationsLocal

@main
struct iOSApp: App {
    init() {
        ProApp.shared.doInitApp()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
        }
	}
}
