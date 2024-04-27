package com.example.counter

import androidx.compose.runtime.mutableIntStateOf
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item_table")
data class Item(
    @PrimaryKey(autoGenerate = true) val id:Int,
    val name:String, val price:Int, var number: Int
)
