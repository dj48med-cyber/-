package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String, // starts with 0, 10 digits
    val section: String, // Formerly Department
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "requests")
data class GrantLoanRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val employeePhone: String,
    val type: String, // Grant: marriage, death, circumcision, surgery, radiology, schooling, retirement, special needs. Loan: exceptional, housing, marriage.
    val isLoan: Boolean,
    val amount: Double,
    val durationMonths: Int = 0, // ONLY for loans
    val monthlyDeduction: Double = 0.0, // ONLY for loans
    val submissionDate: Long = System.currentTimeMillis(),
    val status: String = "Pending", // Pending, Approved, Rejected
    val rejectionReason: String? = null,
    val attachmentPaths: String = "", // Comma-separated fake local/Drive paths
    val meetingNumber: Int? = null, // Meeting processed in
    val decidedDate: Long? = null
)

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val meetingNumber: Int,
    val meetingDate: Long = System.currentTimeMillis(),
    val attendeesPresent: String, // comma-separated names of present committee members
    val attendeesAbsent: String, // comma-separated names of absent committee members
    val decisionsSummary: String, // AI-generated or manual text summarizing decisions
    val signaturesJson: String // JSON map of attendee name -> base64 signature/indicator (only present ones can sign)
)

@Entity(tableName = "committee_members")
data class CommitteeMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String // رئيس اللجنة، نائب الرئيس، الكاتب، عضو، إلخ
)

@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val year: Int,
    val totalBudget: Double,
    val allocatedGrants: Double = 0.0,
    val allocatedLoans: Double = 0.0,
    val spentGrants: Double = 0.0,
    val spentLoans: Double = 0.0
)

@Entity(tableName = "complaints")
data class ComplaintEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int,
    val employeeName: String,
    val employeePhone: String,
    val subject: String,
    val details: String,
    val submissionDate: Long = System.currentTimeMillis(),
    val status: String = "Pending", // Pending, Resolved
    val resolutionNote: String? = null
)

@Entity(tableName = "backup_logs")
data class BackupLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val backupDate: Long = System.currentTimeMillis(),
    val backupType: String, // Google Drive, Email
    val status: String, // Success, Failed
    val recipientEmail: String = "",
    val details: String = ""
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
