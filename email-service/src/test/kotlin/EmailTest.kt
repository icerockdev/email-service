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
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test


class EmailTest {

    private var server: SimpleSmtpServer? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var mailerService: MailerService? = null

    @Before
    fun setUp() {
        server = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT)
        mailerService = MailerService(
            scope,
            SMTPConfig(
                host = "localhost",
                port = server!!.port
            )
        )
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
        server?.stop()
    }

    @Test
    fun testSend() {

        runBlocking {
            mailerService!!.compose().apply {
                fromEmail = "from@icerockdev.com"
                fromName = "From Person"
                subject = "TEST EMAIL"
                to = mutableMapOf("to@icerockdev.com" to "Test Person")
                html = "<h1>Test test test</h1>"
            }.sendAsync()
                .join()
        }

        val emails = server!!.receivedEmails
        assertThat(emails, hasSize(1))
        val email = emails[0]
        assertThat(email.getHeaderValue("Subject"), `is`("TEST EMAIL"))
        assertThat(email.body, containsString("<h1>Test test test</h1>"))
        assertThat(email.headerNames, hasItem("Date"))
        assertThat(email.headerNames, hasItem("From"))
        assertThat(email.headerNames, hasItem("To"))
        assertThat(email.headerNames, hasItem("Subject"))
        assertThat(email.getHeaderValues("To"), contains("Test Person <to@icerockdev.com>"))
        assertThat(email.getHeaderValue("To"), `is`("Test Person <to@icerockdev.com>"))
    }
}