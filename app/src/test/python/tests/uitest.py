import unittest

from idtype import IdType
from mpptest import MppTest


class UiTest(MppTest):
    def test_should_be_at_least_4_menu_items(self):
        self.open_menu()
        menu_items = self.find_elements_by_id('title', IdType.ANDROID)
        self.assertTrue(len(menu_items) > 3, "At least 4 menu items must be in the menu")

    def test_exit_should_be_the_last_menu_item(self):
        self.open_menu()
        menu_items = self.find_elements_by_id('title', IdType.ANDROID)
        self.assertEqual(menu_items[len(menu_items) - 1].text, 'Exit')


if __name__ == '__main__':
    unittest.main()