from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice
import time

import config
import sys

out_folder = sys.argv[1]
out_filename_prefix = sys.argv[2]
apk_location = sys.argv[3]

print()
print('Screenshots will be located in ' + out_folder)

package = config.get_package()
deviceName = 'emulator-5580'


def take_screenshot(filename_postfix):
    screenshot = device.takeSnapshot()
    screenshot.writeToFile(out_folder + '/' + out_filename_prefix + '_' + filename_postfix + '.png', 'png')
    return


def take_activity_screenshot(activity):
    runComponent = package + '/' + activity

    print('Starting activity ' + runComponent + '...')
    device.startActivity(component=runComponent)

    # sleep while application will be loaded
    MonkeyRunner.sleep(15)

    print('Taking screenshot...')
    take_screenshot(activity)
    return


print('Waiting for device ' + deviceName + '...')

device = MonkeyRunner.waitForConnection(100, deviceName)

if device:

    print('Device found, removing application if any ' + package + '...')
    device.removePackage(package)

    print('Installing apk ' + apk_location + '...')
    device.installPackage(apk_location)

    for activity in config.get_activities():
        take_activity_screenshot(activity)

    print '#########'
    print 'Finished!'
    print '#########'
else:
    print '#########'
    print 'Failure!'
    print '#########'