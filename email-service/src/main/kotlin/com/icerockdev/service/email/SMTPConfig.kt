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

// Attaching point for the extension function which provides the answer
interface EnumCompanion<T : Enum<T>>

// Marker interface to provide the common data
interface WithAlias {
    val alias: String
}

// searching by alias support
inline fun <reified T> EnumCompanion<T>.fromAlias(
    value: String
): T? where T : Enum<T>, T : WithAlias {
    return enumValues<T>().firstOrNull { it.alias == value }
}

enum class SMTPSecure(override val alias: String): WithAlias {
    SSL("ssl"),
    TLS("tls");

    companion object : EnumCompanion<SMTPSecure>
}