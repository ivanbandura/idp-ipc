import UIKit
import Flutter

@UIApplicationMain
@objc class AppDelegate: FlutterAppDelegate {

    var localPort: CFMessagePort?
    var remotePort: CFMessagePort?

    override func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
    ) -> Bool {

        let controller = window?.rootViewController as! FlutterViewController
        let methodChannel = FlutterMethodChannel(name: "app.client.messenger", binaryMessenger: controller.binaryMessenger)

        methodChannel.setMethodCallHandler { (call: FlutterMethodCall, result: @escaping FlutterResult) in
            if call.method == "sendData" {
                if let arguments = call.arguments as? [String: Any],
                   let message = arguments["message"] as? String {
                    self.sendMessageToServer(message: message)
                    result("Message sent to iOS native service")
                } else {
                    result(FlutterError(code: "INVALID_ARGUMENTS", message: "Invalid arguments received", details: nil))
                }
            } else {
                result(FlutterMethodNotImplemented)
            }
        }

        setupMessagePort()
        return super.application(application, didFinishLaunchingWithOptions: launchOptions)
    }

    private func setupMessagePort() {
        // Create a local message port
        let context = UnsafeMutableRawPointer(Unmanaged.passUnretained(self).toOpaque())
        localPort = CFMessagePortCreateLocal(nil, "com.example.localport" as CFString, { (port, msgID, cfData, info) -> Unmanaged<CFData>? in
            guard let info = info else { return nil }
            let selfPointer = Unmanaged<AppDelegate>.fromOpaque(info).takeUnretainedValue()
            selfPointer.handleIncomingMessage(data: cfData)
            return nil
        }, context, nil)
        CFMessagePortSetDispatchQueue(localPort, DispatchQueue.main)
    }

    private func handleIncomingMessage(data: CFData?) {
        if let data = data, let message = String(data: data as Data, encoding: .utf8) {
            print("Received message from remote: \(message)")
        }
    }

    private func sendMessageToServer(message: String) {
        guard let remotePort = CFMessagePortCreateRemote(nil, "com.example.remoteport" as CFString) else {
            print("Failed to create remote port")
            return
        }
        self.remotePort = remotePort

        let data = message.data(using: .utf8)! as CFData
        CFMessagePortSendRequest(remotePort, 0, data, 1.0, 1.0, nil, nil)
    }
}