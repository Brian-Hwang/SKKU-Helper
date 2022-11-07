package edu.skku.skkuhelper.roomdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SKKUAssignmentDao {
    @Query("SELECT * FROM SKKUAssignment")
    List<SKKUAssignment> getAll();

    @Query("SELECT * FROM SKKUAssignment WHERE assignmentId IN (:assignmentIds)")
    List<SKKUAssignment> loadAllByIds(int[] assignmentIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SKKUAssignment SKKUAssignments);

    @Delete
    void delete(SKKUAssignment assignments);
}
