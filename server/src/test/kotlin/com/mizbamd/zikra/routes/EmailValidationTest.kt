package com.mizbamd.zikra.routes

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EmailValidationTest {
    @Test
    fun acceptsNonGmail() {
        assertTrue(isValidEmail("user@example.com"))
        assertTrue(isValidEmail("a.b+tag@domain.co.uk"))
    }

    @Test
    fun rejectsJunk() {
        assertFalse(isValidEmail(""))
        assertFalse(isValidEmail("no-at"))
        assertFalse(isValidEmail("@nodomain.com"))
        assertFalse(isValidEmail("user@"))
        assertFalse(isValidEmail("user@localhost"))
        assertFalse(isValidEmail("a b@c.com"))
    }
}
