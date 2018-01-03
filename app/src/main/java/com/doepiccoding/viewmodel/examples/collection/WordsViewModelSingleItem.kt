package com.doepiccoding.viewmodel.examples.collection

import com.doepiccoding.viewmodel.models.CollectionViewModel

open class WordsViewModelSingleItem : CollectionViewModel<String>() {


    override fun loadInitialData(): List<String> {
        //Simulate loading task that takes long...
        Thread.sleep(3000)
        return DBSimulation.getInstance().allStrings
    }

    override fun updatePersistedData(updatedData: String, action: DeltaAction, position: Int) {

        //Note: Here you can check the delta action to add or delete
        if (action == DeltaAction.ADD) {
            DBSimulation.getInstance().putString(updatedData)
        }else if (action == DeltaAction.REMOVE) {
            DBSimulation.getInstance().removeString(updatedData)
        }else {
            throw IllegalStateException("Action not recognized")
        }
    }
}