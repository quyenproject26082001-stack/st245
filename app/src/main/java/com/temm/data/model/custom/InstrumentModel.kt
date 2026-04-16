package com.temm.data.model.custom

data class InstrumentModel(
    val id: String,                 // instrument folder name, e.g. "piano"
    val navPath: String,            // path to nav.png, e.g. "instrument/let_play/piano/nav.png"
    val noteCount: Int,             // number of notes: 2 or 8
    val isSelected: Boolean = false,
    val isUnlocked: Boolean = false,

    )
