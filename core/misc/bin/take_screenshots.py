from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
import time
import config
import sys

out_folder = sys.argv[1]
device_name = sys.argv[2]
apk_location = sys.argv[3]

print()
print('Screenshots will be located in ' + out_folder)

package = config.get_package()
deviceName = 'emulator-5580'


def take_screenshot(filename_postfix):
    screenshot = device.takeSnapshot()
    screenshot.writeToFile(out_folder + '/' + device_name + '_' + filename_postfix + '.png', 'png')
    return


print('Waiting for device ' + deviceName + '...')

device = MonkeyRunner.waitForConnection(100, deviceName)

if device:
    device.removePackage(package)
    device.installPackage(apk_location)

    config.start_activity(device, config.get_start_activity())
    time.sleep(7)
    device.shell('am force-stop ' + config.get_package())

    for name, action in config.get_actions(device_name).iteritems():
        print('Running action: ' + name)
        action(device)
        time.sleep(3)

        take_screenshot(name)
        device.shell('am force-stop ' + config.get_package())
else:
    print('Error: Waiting for device failed')