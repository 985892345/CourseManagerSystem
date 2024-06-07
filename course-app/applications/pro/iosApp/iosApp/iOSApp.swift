import SwiftUI
import AppApplicationsPro

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
