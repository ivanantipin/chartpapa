package com.firelib.techbot.menu

import firelib.core.misc.JsonHelper
import org.springframework.util.Base64Utils
import org.springframework.util.DigestUtils
import java.util.concurrent.ConcurrentHashMap

class Md5Serializer{

    val data = ConcurrentHashMap<String, Any>()

    fun serialize(obj : Any) : String{
        val bytes = JsonHelper.toJsonBytes(obj)
        val id = Base64Utils.encodeToString(DigestUtils.md5Digest(bytes))
        data.put(id, obj)
        return id
    }

    inline fun <reified T> deserialize(id : String) : T{
        return data.get(id)!! as T
    }

}