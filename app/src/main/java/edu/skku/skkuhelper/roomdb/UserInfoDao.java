package edu.skku.skkuhelper.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface UserInfoDao {
    @Query("SELECT * FROM UserInfo")
    List<UserInfo> getAll();

    @Query("SELECT userTOKEN FROM UserInfo")
    String getTOKEN();

    @Query("SELECT * FROM UserInfo WHERE userTOKEN IN (:userTOKENs)")
    List<UserInfo> loadAllByIds(int[] userTOKENs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserInfo SKKUAssignments);

    @Update
    void update(UserInfo userinfos);

    @Delete
    void delete(UserInfo userinfos);
}