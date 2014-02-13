import unittest

from mpptest import MppTest


class ContactsTest(MppTest):
    def test_contacts_should_be_sorted_correctly(self):
        self.add_test_account()
        self.open_contacts()

        contacts = self.find_elements_by_id("mpp_li_contact_name_textview")
        contact_names = [c.text for c in contacts[0:5]]

        self.open_chats()
        chats = self.find_elements_by_id("mpp_li_chat_title_textview")
        chat_names = [c.text for c in chats[0:5]]

        self.assertEqual(contact_names, chat_names)

    def test_contact_should_be_last_after_message_is_sent(self):
        self.add_test_account()
        self.open_contacts()

        contacts = self.find_elements_by_id("mpp_li_contact_name_textview")
        contact_name = contacts[2].text.split("(")[0]

        self.send_message(contact_name, "test")

        self.open_contacts()
        contacts = self.find_elements_by_id("mpp_li_contact_name_textview")
        self.assertEqual(contact_name, contacts[0].text.split("(")[0])



if __name__ == '__main__':
    unittest.main()