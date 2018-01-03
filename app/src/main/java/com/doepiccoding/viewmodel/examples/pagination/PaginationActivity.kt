package com.doepiccoding.viewmodel.examples.pagination

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import com.doepiccoding.viewmodel.R
import com.doepiccoding.viewmodel.models.PaginationViewModel
import kotlinx.android.synthetic.main.pagination_activity_main.*

class PaginationActivity : AppCompatActivity() {

    lateinit var model: MyPaginationModel
    val pagesAdapter = PaginationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pagination_activity_main)


        //Setup the recycler view
        itemsInPage.layoutManager = LinearLayoutManager(this, GridLayoutManager.VERTICAL, false)
        itemsInPage.adapter = pagesAdapter


        //Setup DeltaObserver(All changes to your view have to happen here, since you will always be notified for actions)...
        val paginationObserver = object: PaginationViewModel.PaginationObserver<String> {

            override fun onNewPage(pageSubsetOnly: MutableList<String>?, pageId: String) {
                populatePage(pageSubsetOnly)
            }

            override fun onReset() {
                pagesAdapter.removeAllItems()
            }

            override fun onReloaded(entireData: MutableList<String>?) {
                populatePage(entireData)
            }

        }

        //Setup Model...
        model = ViewModelProviders.of(this).get(MyPaginationModel::class.java)
        model.observePagination(object: PaginationViewModel.PaginationSubscriber{
            override fun getLifecycleOwner(): LifecycleOwner = this@PaginationActivity
            override fun getPaginationObserver(): PaginationViewModel.PaginationObserver<String>  = paginationObserver
        })


        nextPage.setOnClickListener({
            model.paginate(filter.text.toString())
        })

    }

    private fun populatePage(items: List<String> ?) {
        if (items != null && items.isNotEmpty()) {
            pagesAdapter.addAllItems(items)
            itemsInPage.scrollToPosition(pagesAdapter.itemCount - 1)
        }
    }
}