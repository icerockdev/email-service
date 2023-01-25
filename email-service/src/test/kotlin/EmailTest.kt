/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.dumbster.smtp.SimpleSmtpServer
import com.icerockdev.service.email.MailerService
import com.icerockdev.service.email.SMTPConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class EmailTest {
    private lateinit var server: SimpleSmtpServer
    private lateinit var mailerService: MailerService
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Before
    fun setUp() {
        server = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT)
        mailerService = MailerService(
            scope,
            SMTPConfig(
                host = "localhost",
                port = server.port
            )
        )
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        server.stop()
    }

    @Test
    fun testSend() {

        runBlocking {
            mailerService.compose().apply {
                fromEmail = "from@icerockdev.com"
                fromName = "From Person"
                subject = "TEST EMAIL"
                to = mutableMapOf("to@icerockdev.com" to "Test Person")
                html = "<h1>Test test test</h1>"
            }.sendAsync()
                .join()
        }

        val emails = server.receivedEmails
        assertEquals(expected = 1, actual = emails.size)
        val email = emails[0]

        assertEquals(expected = "TEST EMAIL", actual = email.getHeaderValue("Subject"))
        assertContains(email.body, "<h1>Test test test</h1>")
        assertTrue { email.headerNames.contains("Date") }
        assertTrue { email.headerNames.contains("From") }
        assertTrue { email.headerNames.contains("To") }
        assertEquals(expected = 1, email.getHeaderValues("To").size)
        assertTrue { email.getHeaderValues("To").contains("Test Person <to@icerockdev.com>") }
    }
}
