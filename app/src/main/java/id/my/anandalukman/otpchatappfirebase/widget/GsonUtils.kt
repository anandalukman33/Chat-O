package id.my.anandalukman.otpchatappfirebase.widget

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import java.util.*

object GsonUtils {

    internal var mGson = GsonBuilder().disableHtmlEscaping().create()

    fun <T> json2Bean(result: String, clazz: Class<T>): T {
        return mGson.fromJson(result, clazz)
    }

    fun <T> json2Array(result: String, clazz: Class<T>): List<T> {
        val list = ArrayList<T>()
        val parser = JsonParser()
        val element = parser.parse(result)
        if (element.isJsonObject) {
            list.add(mGson.fromJson(element, clazz))
        } else if (element.isJsonArray) {
            val var5 = element.asJsonArray.iterator()

            while (var5.hasNext()) {
                val obj = var5.next() as JsonElement
                val t = mGson.fromJson(obj, clazz)
                list.add(t)
            }
        }

        return list
    }

    fun bean2Json(obj: Any): String {
        return mGson.toJson(obj)
    }

    fun <T> array2Json(lists: List<T>): String {
        val listType = object : TypeToken<T>() {

        }.getType()
        return mGson.toJson(lists, listType)
    }


    fun <T, R> listHashMap2Json(list: List<Map<T, R>>): String {
        val listMapType = object : TypeToken<List<HashMap<T, R>>>() {

        }.type
        return mGson.toJson(list, listMapType)
    }

    fun <T> json2Array(result: String, typeToken: TypeToken<List<T>>): List<T>? {
        return mGson.fromJson<Any>(result, typeToken.type) as List<T>
    }

    fun json2Map(result: String): Map<String, String> {
        return mGson.fromJson(result, Map::class.java) as Map<String, String>
    }

    fun map2Json(result: Map<String, Any>): String {
        return mGson.toJson(result)
    }

    fun toJsonString(obj: Any?): String {
        return mGson.toJson(obj)
    }

    fun <T> toBean(gsonString: String, cls: Class<T>): T {
        return mGson.fromJson(gsonString, cls)
    }
}

