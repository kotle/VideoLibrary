package com.yizisu.videolibrary

import android.content.Context
import org.json.JSONArray


fun readTvBeans(context: Context): MutableList<TvBean>? {
    return try {
        val reader = context.assets.open("tv_info.json").bufferedReader()
        val text = reader.readText()
        reader.close()
        val json = JSONArray(text)
        val count = json.length()
        val tvs = mutableListOf<TvBean>()
        repeat(count) {
            val tv = json.getJSONObject(it)
            val name = tv.getString("name")
            val urlBean = tv.getJSONArray("urlBean")
            val urlCont = urlBean.length()
            val tvBean = TvBean(name, mutableListOf())
            repeat(urlCont) {
                val urlJSON = urlBean.getJSONObject(it)
                val urlName = urlJSON.getString("name")
                val url = "http://ivi.bupt.edu.cn" + urlJSON.getString("url")
                tvBean.urlBean.add(TvUrlBean(urlName, url))
            }
            tvs.add(tvBean)
        }
        tvs
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}

data class TvBean(
    val name: String,
    val urlBean: MutableList<TvUrlBean>
)

data class TvUrlBean(val name: String, val url: String)