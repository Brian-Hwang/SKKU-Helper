package edu.skku.skkuhelper.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SKKUAssignmentDao {
    @Query("SELECT * FROM SKKUAssignment")
    List<SKKUAssignment> getAll();

    @Query("SELECT * FROM SKKUAssignment WHERE assignmentId IN (:assignmentIds)")
    List<SKKUAssignment> loadAllByIds(int[] assignmentIds);


    @Query("SELECT * FROM SKKUAssignment WHERE SKKUAssignment.assignmentId == :assignmentid")
    SKKUAssignment getById(long assignmentid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SKKUAssignment SKKUAssignments);

    @Update
    void update(SKKUAssignment assignments);

    @Delete
    void delete(SKKUAssignment assignments);

    @Query("DELETE FROM SKKUAssignment")
    void nukeTable();

    @Query("SELECT COUNT(SKKUAssignment.assignmentId) FROM SKKUAssignment")
    int getRowCount();

}
