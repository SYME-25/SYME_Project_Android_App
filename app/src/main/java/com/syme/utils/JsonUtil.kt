package com.syme.utils

import com.google.common.reflect.TypeToken
import com.google.gson.Gson

private val gson = Gson()

/**
 * Convert any object to JSON string safely.
 */
fun toJson(data: Any?): String {
    return try {
        gson.toJson(data)
    } catch (e: Exception) {
        "{}"
    }
}

/**
 * Generic JSON deserialization for any type.
 */
fun <T> fromJson(json: String?): T? {
    return if (json.isNullOrEmpty()) null
    else try {
        gson.fromJson<T>(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        null
    }
}

/**
 * JSON deserialization specifically for Map<String, Any>.
 */
fun String?.fromJsonToMap(): Map<String, Any>? {
    return if (this.isNullOrEmpty()) null else try {
        gson.fromJson<Map<String, Any>>(this, object : TypeToken<Map<String, Any>>() {}.type)
    } catch (e: Exception) {
        null
    }
}

/**
 * JSON deserialization specifically for Map<String, Boolean>.
 */
fun String?.fromJsonToMapBoolean(): Map<String, Boolean>? {
    return if (this.isNullOrEmpty()) null else try {
        gson.fromJson<Map<String, Boolean>>(
            this,
            object : TypeToken<Map<String, Boolean>>() {}.type
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * JSON deserialization specifically for Map<String, String>.
 */
fun String?.fromJsonToMapString(): Map<String, String>? {
    return if (this.isNullOrEmpty()) null else try {
        gson.fromJson<Map<String, String>>(this, object : TypeToken<Map<String, String>>() {}.type)
    } catch (e: Exception) {
        null
    }
}

/**
 * JSON deserialization specifically for Map<String, Int>.
 */
fun String?.fromJsonToMapInt(): Map<String, Int>? {
    return if (this.isNullOrEmpty()) null else try {
        gson.fromJson<Map<String, Int>>(this, object : TypeToken<Map<String, Int>>() {}.type)
    } catch (e: Exception) {
        null
    }
}

/**
 * JSON deserialization specifically for Map<String, Double>.
 */
fun String?.fromJsonToMapDouble(): Map<String, Double>? {
    return if (this.isNullOrEmpty()) null else try {
        gson.fromJson<Map<String, Double>>(this, object : TypeToken<Map<String, Double>>() {}.type)
    } catch (e: Exception) {
        null
    }
}
