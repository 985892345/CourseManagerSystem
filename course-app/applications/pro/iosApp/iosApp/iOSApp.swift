import SwiftUI
import ApplicationsPro

@main
struct iOSApp: App {
    init() {
        ProKtProviderInitializer.tryInitKtProvider()
    }
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}