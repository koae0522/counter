package com.example.counter

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TotalViewModel :ViewModel(){
    private val _uiState = MutableStateFlow(0)
    val uiState: StateFlow<Int> = _uiState.asStateFlow()

    suspend fun changeTotalValue(price: Int,pn:Boolean) {
        Log.d("tamago",price.toString())
        Log.d("tamago",_uiState.value.toString())
        if(pn) {
            _uiState.emit(_uiState.value + price)
        }else{
            _uiState.emit(_uiState.value - price)
        }
    }

    suspend fun loadTotalValue(price:Int){
        _uiState.value = price
    }
}