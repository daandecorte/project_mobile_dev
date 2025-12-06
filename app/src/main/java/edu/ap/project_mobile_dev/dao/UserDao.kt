package edu.ap.project_mobile_dev.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.ap.project_mobile_dev.ui.model.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE uid IN (:uids)")
    suspend fun getUsersByIds(uids: List<String>): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}