package com.doepiccoding.viewmodel.examples.collection

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.Toast
import com.doepiccoding.viewmodel.R
import com.doepiccoding.viewmodel.models.CollectionViewModel
import com.doepiccoding.viewmodel.models.CollectionViewModel.DeltaObserver
import kotlinx.android.synthetic.main.collection_activity.*

class CollectionMainActivity : AppCompatActivity() {

    lateinit var model: WordsViewModelSingleItem
    val wordsAdapter = WordsAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.collection_activity)


        //Setup the recycler view
        words.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        words.adapter = wordsAdapter


        //Setup DeltaObserver(All changes to your view have to happen here, since you will always be notified for actions)...
        val deltaObserver = object: DeltaObserver<String> {
            override fun onChanged(t: String?, action: CollectionViewModel.DeltaAction, position: Int) {
                if (action == CollectionViewModel.DeltaAction.ADD) {
                    if (t != null && t.isNotEmpty()) {
                        wordsAdapter.addWord(t, position)
                        words.scrollToPosition(position)
                    }
                }else if(action == CollectionViewModel.DeltaAction.REMOVE) {
                    //Do the proper to remove it...
                    wordsAdapter.removeWord(position)
                    words.scrollToPosition(position)
                }else {
                    throw IllegalStateException("Action not recognized")
                }
            }

            override fun onLoaded(t: List<String>?) {
                if (t != null && t.isNotEmpty()) {
                    wordsAdapter.addAllWords(t)
                }

                if (loading.visibility == View.VISIBLE) {
                    loading.visibility = View.GONE
                }
            }
        }

        //Setup Model...
        model = ViewModelProviders.of(this).get(WordsViewModelSingleItem::class.java)
        model.observeDelta(object : CollectionViewModel.DeltaSubscriber {
            override fun getLifecycleOwner(): LifecycleOwner {
                return this@CollectionMainActivity
            }

            override fun getDeltaObserver(): DeltaObserver<*> {
                return deltaObserver
            }
        })


        add.setOnClickListener({
            val word = wordInput.text.toString()
            if (word.isEmpty()) {
                Toast.makeText(this, "Add a word!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //All the data interaction must go through the model...
            model.addData(word)
            wordInput.setText("")
        })
        remove.setOnClickListener({
            val word = wordInput.text.toString()
            if (word.isEmpty()) {
                Toast.makeText(this, "Add a word to remove!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //All the data interaction must go through the model...
            model.removeData(word)
            wordInput.setText("")
        })
    }
}
