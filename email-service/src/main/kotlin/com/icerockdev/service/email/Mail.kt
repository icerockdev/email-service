/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.email

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.Email
import org.apache.commons.mail.EmailConstants.UTF_8
import org.apache.commons.mail.HtmlEmail
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.activation.URLDataSource
import javax.mail.util.ByteArrayDataSource

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

    private companion object {
        val LOGGER: Logger = LoggerFactory.getLogger(Mail::class.java)
    }
}
