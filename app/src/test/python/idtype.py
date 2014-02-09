class IdType(object):
    PACKAGE = "package"
    FULL = "full"
    ANDROID = "android"

    @classmethod
    def is_valid(cls, id_type):
        for attr in dir(cls):
            if id_type == getattr(cls, attr):
                return True
        return False
