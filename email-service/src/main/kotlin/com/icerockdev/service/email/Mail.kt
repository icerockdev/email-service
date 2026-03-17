/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.email

import jakarta.activation.DataSource
import jakarta.activation.FileDataSource
import jakarta.activation.URLDataSource
import jakarta.mail.util.ByteArrayDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.mail2.core.EmailConstants.UTF_8
import org.apache.commons.mail2.jakarta.DefaultAuthenticator
import org.apache.commons.mail2.jakarta.Email
import org.apache.commons.mail2.jakarta.HtmlEmail
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL

class Mail(private val coroutineScope: CoroutineScope?, private val config: SMTPConfig) {

    var subject: String = ""
    var to: MutableMap<String, String?> = HashMap()
    var text: String = ""
        set(value) {
            this.isHtml = false
            field = value
        }
    var html: String = ""
        set(value) {
            this.isHtml = true
            field = value
        }
    var fromName: String = ""
    var fromEmail: String = ""
    var charset = UTF_8
    var attachments: List<Attachment> = emptyList()
    var inlineAttachments: List<InlineAttachment> = emptyList()
    var replyToEmail: String? = null
    var replyToName: String? = null
    private var isHtml: Boolean = false

    private fun prepareEmail(): Email {

        val email = HtmlEmail()
        email.hostName = config.host
        email.setSmtpPort(config.port)
        if (config.smtpAuth) {
            email.setAuthenticator(DefaultAuthenticator(config.username, config.password))
        }

        if (config.smtpSecure !== null) {
            if (config.smtpSecure == SMTPSecure.SSL) {
                email.isSSLOnConnect = true
            } else if (config.smtpSecure == SMTPSecure.TLS) {
                email.isStartTLSEnabled = true
                email.isStartTLSRequired = true
            }
        }

        email.setFrom(this.fromEmail, this.fromName)
        email.subject = this.subject

        if (this.isHtml) {
            email.setHtmlMsg(this.html)
        } else {
            email.setTextMsg(this.text)
        }

        for (entry in this.to.entries) {
            email.addTo(entry.key, entry.value)
        }

        email.setCharset(charset)
        attachments.forEach { attachment ->
            email.attach(attachment.dataSource, attachment.name, attachment.description)
        }

        inlineAttachments.forEach { inline ->
            email.embed(inline.dataSource, inline.name, inline.cidName)
        }

        if (!replyToEmail.isNullOrBlank()) {
            email.addReplyTo(replyToEmail, replyToName)
        }

        return email
    }

    fun send() {
        prepareEmail().send()
    }

    fun sendAsync(): Job {
        if (coroutineScope === null) {
            throw MailException("Async sending unsupported")
        }

        val email = prepareEmail()
        return coroutineScope.launch {
            try {
                email.send()
            } catch (t: Throwable) {
                LOGGER.error(t.localizedMessage, t)
            }
        }
    }

    class Attachment(
        val dataSource: DataSource,
        val name: String,
        val description: String? = null
    ) {
        constructor(file: File, name: String, description: String? = null) : this(
            FileDataSource(file), name, description
        )

        constructor(url: URL, name: String, description: String? = null) : this(URLDataSource(url), name, description)

        constructor(data: ByteArray, name: String, description: String? = null, charset: String = UTF_8) : this(
            ByteArrayDataSource(data, "application/octet-stream;charset=$charset"), name, description
        )
    }

    class InlineAttachment(
        val dataSource: DataSource,
        val cidName: String,
        val name: String,
    ) {
        constructor(file: File, cidName: String, name: String) : this(FileDataSource(file), cidName, name)
        constructor(url: URL, cidName: String, name: String) : this(URLDataSource(url), cidName, name)
        constructor(data: ByteArray, cidName: String, name: String, charset: String = UTF_8) : this(
            ByteArrayDataSource(data, "application/octet-stream;charset=$charset"), cidName, name
        )
    }

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Mail::class.java)
    }
}
