package edu.skku.skkuhelper.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SKKUNoticeDao {
    @Query("SELECT * FROM SKKUNotice")
    List<SKKUNotice> getAll();


    @Query("SELECT * FROM SKKUNotice WHERE noticeid IN (:noticeids)")
    List<SKKUNotice> loadAllByIds(int[] noticeids);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SKKUNotice SKKUNotices);

    @Update
    void update(SKKUNotice notices);

    @Delete
    void delete(SKKUNotice notices);

    @Query("DELETE FROM SKKUNotice")
    void nukeTable();
}
