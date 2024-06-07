import SwiftUI
import AppApplicationsLocal

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.doInitApp()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
        }
	}
}
