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
    @Query("SELECT * FROM Userinfo")
    List<Userinfo> getAll();

    @Query("SELECT * FROM Userinfo WHERE userTOKEN IN (:userTOKENs)")
    List<Userinfo> loadAllByIds(int[] userTOKENs);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Userinfo SKKUAssignments);

    @Update
    void update(Userinfo Userinfos);

    @Delete
    void delete(Userinfo Userinfos);
}