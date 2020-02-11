/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.email

import kotlinx.coroutines.CoroutineScope

class MailerService(private val coroutineScope: CoroutineScope?, private val config: SMTPConfig) {
    // TODO: append html render
    fun compose(): Mail {
        return Mail(coroutineScope, config)
    }
}