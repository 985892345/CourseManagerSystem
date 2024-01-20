import SwiftUI
import ApplicationsPro

@main
struct iOSApp: App {
    init() {
        ProAppKt.shared.initApp()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}