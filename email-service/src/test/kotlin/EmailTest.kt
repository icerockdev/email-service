/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

import com.dumbster.smtp.SimpleSmtpServer
import com.icerockdev.service.email.Mail
import com.icerockdev.service.email.MailerService
import com.icerockdev.service.email.SMTPConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
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

    @Test
    fun testSendWithInlineAttachments() = runBlocking {
        val imageFile = File("src/test/resources/kotlin.png")
        val cidName = "kotlin-name"
        val name = "kotlinlogo"

        mailerService.compose().apply {
            fromEmail = "from@icerockdev.com"
            fromName = "From Person"
            subject = "TEST EMAIL"
            to = mutableMapOf("to@icerockdev.com" to "Test Person")
            html = "<h1>Test Inline</h1><img src=\"cid:$cidName\">"

            inlineAttachments = listOf(
                Mail.InlineAttachment(imageFile, cidName, name)
            )
        }.sendAsync().join()

        val emails = server.receivedEmails
        val email = emails[0]

        assertContains(email.body, "<img src=\"cid:$cidName\">")
        assertContains(email.body, "Content-ID: <$cidName>")
        assertContains(email.body, "Content-Disposition: inline")
        assertContains(email.body, "name=$name")
    }

    @Test
    fun testSendWithReplyTo() = runBlocking {
        val replyToEmail = "replyto@icerockdev.com"
        val replyToName = "Reply Person"

        mailerService.compose().apply {
            fromEmail = "from@icerockdev.com"
            fromName = "From Person"
            subject = "TEST EMAIL"
            to = mutableMapOf("to@icerockdev.com" to "Test Person")
            html = "<h1>Test test test</h1>"
            this.replyToEmail = replyToEmail
            this.replyToName = replyToName
        }.sendAsync().join()

        val emails = server.receivedEmails
        assertEquals(1, emails.size)
        val email = emails[0]

        val replyToHeader = email.getHeaderValue("Reply-To")
        assertNotNull(replyToHeader)
        assertEquals("$replyToName <$replyToEmail>", replyToHeader)
    }
}
