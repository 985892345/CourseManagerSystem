import SwiftUI
import AppApplicationsPro

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
