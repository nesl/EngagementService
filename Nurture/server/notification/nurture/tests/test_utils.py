import datetime

from django.test import TestCase

from nurture import utils


class UtilsTestCase(TestCase):

    def test_is_file_extended(self):

        with open('/tmp/test_nt_blank', 'w') as fo:
            pass
        with open('/tmp/test_nt_pine', 'w') as fo:
            fo.write('pine')
        with open('/tmp/test_nt_pineapple', 'w') as fo:
            fo.write('pineapple')
        with open('/tmp/test_nt_banana', 'w') as fo:
            fo.write('banana')

        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_blank'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_pine'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_pineapple'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_blank', '/tmp/test_nt_banana'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_blank'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_pine'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_pineapple'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pine', '/tmp/test_nt_banana'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_blank'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_pine'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_pineapple'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_pineapple', '/tmp/test_nt_banana'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_blank'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_pine'))
        self.assertFalse(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_pineapple'))
        self.assertTrue(utils.is_file_extended('/tmp/test_nt_banana', '/tmp/test_nt_banana'))
