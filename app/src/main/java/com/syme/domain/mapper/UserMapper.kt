package com.syme.domain.mapper

import com.syme.domain.model.User

fun User.toDomain(): User =
    User(
        userId = userId,
        firstName = firstName,
        lastName = lastName,
        birthday = birthday,
        email = email,
        roles = roles,
        gender = gender,
        phone = phone,
        address = address,
        trace = trace,
        metadata = metadata,
    )
