package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Employees ---
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Int)

    @Query("SELECT * FROM employees WHERE phone = :phone LIMIT 1")
    suspend fun getEmployeeByPhone(phone: String): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Int): EmployeeEntity?


    // --- Requests (Grants & Loans) ---
    @Query("SELECT * FROM requests ORDER BY submissionDate DESC")
    fun getAllRequests(): Flow<List<GrantLoanRequestEntity>>

    @Query("SELECT * FROM requests WHERE employeeId = :employeeId ORDER BY submissionDate DESC")
    fun getRequestsByEmployee(employeeId: Int): Flow<List<GrantLoanRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: GrantLoanRequestEntity): Long

    @Update
    suspend fun updateRequest(request: GrantLoanRequestEntity)

    @Query("DELETE FROM requests WHERE id = :id")
    suspend fun deleteRequestById(id: Int)


    // --- Meetings ---
    @Query("SELECT * FROM meetings ORDER BY meetingNumber DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity): Long

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: Int)


    // --- Committee Members ---
    @Query("SELECT * FROM committee_members ORDER BY id ASC")
    fun getAllCommitteeMembers(): Flow<List<CommitteeMemberEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitteeMember(member: CommitteeMemberEntity): Long

    @Query("DELETE FROM committee_members WHERE id = :id")
    suspend fun deleteCommitteeMemberById(id: Int)


    // --- Budget ---
    @Query("SELECT * FROM budget WHERE year = :year LIMIT 1")
    suspend fun getBudgetForYear(year: Int): BudgetEntity?

    @Query("SELECT * FROM budget ORDER BY year DESC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Update
    suspend fun updateBudget(budget: BudgetEntity)


    // --- Complaints ---
    @Query("SELECT * FROM complaints ORDER BY submissionDate DESC")
    fun getAllComplaints(): Flow<List<ComplaintEntity>>

    @Query("SELECT * FROM complaints WHERE employeeId = :employeeId ORDER BY submissionDate DESC")
    fun getComplaintsByEmployee(employeeId: Int): Flow<List<ComplaintEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComplaint(complaint: ComplaintEntity): Long

    @Update
    suspend fun updateComplaint(complaint: ComplaintEntity)

    @Query("DELETE FROM complaints WHERE id = :id")
    suspend fun deleteComplaintById(id: Int)


    // --- Backup Logs ---
    @Query("SELECT * FROM backup_logs ORDER BY backupDate DESC")
    fun getAllBackupLogs(): Flow<List<BackupLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackupLog(log: BackupLogEntity): Long


    // --- App Settings ---
    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AppSettingEntity): Long
}
