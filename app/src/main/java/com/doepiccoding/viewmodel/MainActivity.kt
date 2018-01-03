package com.doepiccoding.viewmodel

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.doepiccoding.viewmodel.examples.collection.CollectionMainActivity
import com.doepiccoding.viewmodel.examples.pagination.PaginationActivity
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)


        pagination.setOnClickListener({
            val intent = Intent(this@MainActivity, PaginationActivity::class.java)
            startActivity(intent)
        })

        basicCollection.setOnClickListener({
            val intent = Intent(this@MainActivity, CollectionMainActivity::class.java)
            startActivity(intent)
        })

    }

}