package com.example.counter

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Flowable

@Dao
interface ItemDao {
    @Insert
    fun insert(item:Item)

    @Query("SELECT * FROM item_table")
    fun getAll():Flowable<List<Item>>

    @Update
    fun updateItem(item:Item)

    @Query("DELETE FROM item_table")
    fun deleteAll()
}