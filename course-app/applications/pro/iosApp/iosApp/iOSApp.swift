import SwiftUI
import AppApplicationsPro

@main
struct iOSApp: App {
    init() {
        MainViewControllerKt.initApp()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
        }
	}
}
