package com.doepiccoding.viewmodel.examples.collection

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import com.doepiccoding.viewmodel.DeltaModelApplication
import org.json.JSONArray
import org.json.JSONException
import java.util.*

class DBSimulation private constructor(context: Context) {

    private val mSharedPrefs: SharedPreferences
    private val mSharedPrefsEditor: Editor

    init {
        mSharedPrefs = context.applicationContext.getSharedPreferences(PERSISTENCE_LAYER, Context.MODE_PRIVATE)
        mSharedPrefsEditor = mSharedPrefs.edit()
    }

    val allStrings: List<String>
        get() {
            val strings = ArrayList<String>()
            try {
                val persistedArray = JSONArray(mSharedPrefs.getString(COLLECTION_KEY, EMPTY_JSON_ARRAY))
                var i = 0
                val size = persistedArray.length()
                while (i < size) {
                    strings.add(persistedArray.getString(i))
                    i++
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return strings
        }

    fun putString(input: String) {
        //Get current state of the collection
        try {
            val persistedArray = JSONArray(mSharedPrefs.getString(COLLECTION_KEY, EMPTY_JSON_ARRAY))
            persistedArray.put(input)
            mSharedPrefsEditor.putString(COLLECTION_KEY, persistedArray.toString()).commit()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    fun removeString(removedWord: String) {
        //Get current state of the collection
        try {
            val persistedArray = JSONArray(mSharedPrefs.getString(COLLECTION_KEY, EMPTY_JSON_ARRAY))
            for (i in 0..persistedArray.length()) {
                val word = persistedArray.getString(i)
                if (word == removedWord) {
                    persistedArray.remove(i)
                    break
                }
            }
            mSharedPrefsEditor.putString(COLLECTION_KEY, persistedArray.toString()).commit()
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    companion object {

        const private val PERSISTENCE_LAYER = "persistence_layer"
        const private val COLLECTION_KEY = "collection"
        const private val EMPTY_JSON_ARRAY = "[]"


        private var staticInstance: DBSimulation? = null

        fun getInstance(): DBSimulation {
            if (staticInstance == null) {
                staticInstance = DBSimulation(DeltaModelApplication.getInstance())
            }
            return staticInstance!!
        }
    }
}
