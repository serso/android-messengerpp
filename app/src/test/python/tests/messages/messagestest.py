import unittest

from mpptest import MppTest


class MessagesTest(MppTest):
    def test_should_send_message(self):
        contact = "Adam Ornelas"
        message = "testsdffhshsfghsfgh"

        self.add_test_account()
        self.open_contacts()
        self.send_message(contact, message)
        self.find_element_by_name(message)

        self.open_contact(contact)
        self.find_element_by_name(message)

    def test_message_should_be_read_after_chat_is_opened(self):
        self.add_test_account()

        contact = self.find_contact('Adam Ornelas')
        if not '(' in contact.text:
            raise Exception("Contact must exist")

        contact.click()

        contact = self.find_contact('Adam Ornelas')
        self.assertFalse('(' in contact.text)



if __name__ == '__main__':
    unittest.main()