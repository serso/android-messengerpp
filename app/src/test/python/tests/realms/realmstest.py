import unittest

from android.idtype import IdType
from mpptest import MppTest


class RealmsTest(MppTest):
    def test_should_open_realms_screen(self):
        self.open_realms()
        title = self.find_element_by_id("action_bar_title", IdType.ANDROID)
        self.assertEqual(title.text, "Accounts")

        realms_list = self.find_element_by_id("list", IdType.ANDROID)
        realms = realms_list.find_elements_by_id(self.r_id("mpp_li_realm_name_textview"))
        self.assertTrue(len(realms) > 5)


if __name__ == '__main__':
    unittest.main()