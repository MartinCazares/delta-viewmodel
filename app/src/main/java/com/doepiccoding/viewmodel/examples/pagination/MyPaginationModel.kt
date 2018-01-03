package com.doepiccoding.viewmodel.examples.pagination

import com.doepiccoding.viewmodel.models.PaginationViewModel

class MyPaginationModel: PaginationViewModel<String>() {

    private val simulatedWaitTime = 2600L
    private val simulatedTimeOffByPage = 300L
    private var currentPage: Int = 0
    private var filterValue = ""

    fun paginate(filter: String) {

        if (filterValue != filter) {
            //Prepare the pagination model to start with new set of data...
            reset()
            currentPage = 0
        }
        filterValue = filter

        currentPage++
        nextPage(currentPage.toString())
    }

    override fun getPageData(pageId: String): MutableList<String> {

        val page = pageId.toInt()
        simulateWaitWhenPaginating(page)

        //Simulate the pagination call result
        val listOfWords = mutableListOf<String>()
        simulatedPageResponse(page, listOfWords)

        return listOfWords
    }

    private fun simulateWaitWhenPaginating(page: Int) {
        //SIMULATE LONG TASK GETTING PAGE INFO
        val diffTime = simulatedTimeOffByPage * page
        var waitTime = simulatedWaitTime - diffTime
        waitTime = if (waitTime <= 1L) 1L else waitTime
        Thread.sleep(waitTime)
    }

    private fun simulatedPageResponse(page: Int, listOfWords: MutableList<String>) {
        for (i in 0 until page) {
            val label = StringBuilder()
            for (j in 0..i) {
                label.append("-").append(page)
            }
            listOfWords.add("$filterValue, Page: $label")
        }
    }
}