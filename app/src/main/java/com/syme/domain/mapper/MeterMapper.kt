package com.syme.domain.mapper

import com.syme.R
import com.syme.domain.model.enumeration.MeterStatus
import com.syme.domain.model.enumeration.MeterType

val MeterType.labelResId: Int
    get() = when (this) {

        // 🏠 Domestique
        MeterType.MASTER -> R.string.meter_type_master
        MeterType.SLAVE -> R.string.meter_type_slave
    }

val MeterStatus.labelResId: Int
    get() = when (this) {

        // 🏠 Domestique
        MeterStatus.ACTIVE -> R.string.meter_status_active
        MeterStatus.OFFLINE -> R.string.meter_status_offline
        MeterStatus.MAINTENANCE -> R.string.meter_status_maintenance
    }