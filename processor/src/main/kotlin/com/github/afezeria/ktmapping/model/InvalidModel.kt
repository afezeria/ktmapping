package com.github.afezeria.ktmapping.model

import com.google.devtools.ksp.symbol.KSType

class InvalidModel(type: KSType) : Model(type, false, false)