class IdType(object):
    """Type of resource ID:
    1. 'package' if ID is defined within the app (app package name will be used)
    2. 'full' if ID is fully qualified string, e.g. 'com.example:id/widget'
    3. 'android' if ID is defined in Android SDK ('android' prefix will be used)
    """

    PACKAGE = "package"
    FULL = "full"
    ANDROID = "android"

    @classmethod
    def is_valid(cls, id_type):
        for attr in dir(cls):
            if id_type == getattr(cls, attr):
                return True
        return False
