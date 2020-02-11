/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.email

data class SMTPConfig(
    val host: String,
    val port: Int,
    val smtpSecure: SMTPSecure?=null,
    val smtpAuth: Boolean=false,
    val username: String?=null,
    val password: String?=null
)

enum class SMTPSecure(val value: String) {
    SSL("ssl"),
    TLS("tls")
}