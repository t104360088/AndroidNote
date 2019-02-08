package com.example.myroom

import android.arch.persistence.room.*

@Dao
interface UserEntityDao {

    @Query("select * from " + UserEntity.tableName)
    fun getAll(): List<UserEntity>

    @Query("select * from " + UserEntity.tableName + " where id like :id limit 1")
    fun queryById(id: Long): UserEntity

    @Query("select * from " + UserEntity.tableName + " where name like :name limit 1")
    fun queryByName(name: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUser(item: UserEntity): Long //回傳插入成功的id

    @Update
    fun updateUser(item: UserEntity): Int //回傳異動的資料數量

    @Delete
    fun deleteUser(item: UserEntity): Int //回傳異動的資料數量

    @Query("delete from " + UserEntity.tableName)
    fun deleteAll()
}