package com.example.counter

import android.graphics.Insets.add
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow

class ItemViewModel: ViewModel() {

    private val _uiState = MutableStateFlow(mutableListOf<Item>())
    val uiState: StateFlow<List<Item>> = _uiState.asStateFlow()


    suspend fun addItem(item: Item) {
        _uiState.emit(_uiState.value.toMutableList().apply { add(item) })
    }

    suspend fun changeNumber(n: Int, pn: Boolean) {
        if (n < 0 || n >= _uiState.value.size) {
            return
        }

        val newItem:Item=_uiState.value[n]

        if(pn){
            newItem.number++
        }else{
            newItem.number--
        }

        _uiState.value.removeAt(n)

        _uiState.emit(_uiState.value.toMutableList().apply { add(n,newItem) })

    }

    fun loadItem(item:MutableList<Item>){
        _uiState.value=item
        Log.d("tomato",_uiState.value.toString())
    }
}

