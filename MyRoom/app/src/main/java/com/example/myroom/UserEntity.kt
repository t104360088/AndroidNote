package com.example.myroom

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = UserEntity.tableName, primaryKeys = ["id", "name"])
class UserEntity(name: String, age: Int) {

    //靜態寫法(object即單例本身)
    companion object {
        const val tableName = "user"
    }

    //@PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var name: String = ""

    var age: Int = 0

    init {
        this.name = if (name.isEmpty()) "No name" else name
        this.age = if (age < 0) 0 else age
    }
}