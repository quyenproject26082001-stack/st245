package com.temm.activity_app.secret

import java.io.Serializable

data class Secret(val idImage: Int, var nameState: String, var isLocked: Boolean = true, var Instrument: String)
