package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EmployeeEntity::class,
        GrantLoanRequestEntity::class,
        MeetingEntity::class,
        CommitteeMemberEntity::class,
        BudgetEntity::class,
        ComplaintEntity::class,
        BackupLogEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zemmora_social_services_db"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.appDao())
                }
            }
        }

        suspend fun populateDatabase(dao: AppDao) {
            // 1. Initial Committee Members
            dao.insertCommitteeMember(CommitteeMemberEntity(name = "بن يحيى عبد القادر", role = "رئيس اللجنة"))
            dao.insertCommitteeMember(CommitteeMemberEntity(name = "بلحاج عيسى أحمد", role = "نائب الرئيس"))
            dao.insertCommitteeMember(CommitteeMemberEntity(name = "طاهري فاطمة", role = "أمينة اللجنة"))
            dao.insertCommitteeMember(CommitteeMemberEntity(name = "قادري محمد", role = "عضو مكلف بالتنظيم"))
            dao.insertCommitteeMember(CommitteeMemberEntity(name = "بلعباس يوسف", role = "عضو مكلف بالمالية"))

            // 2. Initial Budget for 2026
            dao.insertBudget(
                BudgetEntity(
                    year = 2026,
                    totalBudget = 6500000.0,
                    allocatedGrants = 3000000.0,
                    allocatedLoans = 3500000.0,
                    spentGrants = 850000.0,
                    spentLoans = 1200000.0
                )
            )

            // 3. Initial Employees
            dao.insertEmployee(EmployeeEntity(name = "سفيان براهيمي", phone = "0661122334", section = "الأشغال العمومية"))
            dao.insertEmployee(EmployeeEntity(name = "ليلى بن منصور", phone = "0770554433", section = "النظافة والتطهير"))
            dao.insertEmployee(EmployeeEntity(name = "عبد الحميد بوزيدي", phone = "0555123456", section = "مصلحة الحالة المدنية"))
            dao.insertEmployee(EmployeeEntity(name = "نادية حداد", phone = "0658998877", section = "الإدارة العامة والإعلام الآلي"))

            // 4. Initial Requests (sample history to show nicely on first load)
            val req1 = dao.insertRequest(
                GrantLoanRequestEntity(
                    employeeId = 1,
                    employeeName = "سفيان براهيمي",
                    employeePhone = "0661122334",
                    type = "منحة الختان",
                    isLoan = false,
                    amount = 15000.0,
                    status = "Approved",
                    attachmentPaths = "birth_certificate.pdf, circumcision_document.pdf",
                    meetingNumber = 12,
                    decidedDate = System.currentTimeMillis() - 86400000 * 5
                )
            )

            val req2 = dao.insertRequest(
                GrantLoanRequestEntity(
                    employeeId = 2,
                    employeeName = "ليلى بن منصور",
                    employeePhone = "0770554433",
                    type = "سلفة استثنائية",
                    isLoan = true,
                    amount = 50000.0,
                    durationMonths = 10,
                    monthlyDeduction = 5000.0,
                    status = "Approved",
                    attachmentPaths = "request_letter.docx",
                    meetingNumber = 12,
                    decidedDate = System.currentTimeMillis() - 86400000 * 5
                )
            )

            val req3 = dao.insertRequest(
                GrantLoanRequestEntity(
                    employeeId = 3,
                    employeeName = "عبد الحميد بوزيدي",
                    employeePhone = "0555123456",
                    type = "منحة التقاعد",
                    isLoan = false,
                    amount = 60000.0,
                    status = "Pending",
                    attachmentPaths = "pension_file.pdf"
                )
            )

            // 5. Initial Meeting
            dao.insertMeeting(
                MeetingEntity(
                    meetingNumber = 12,
                    meetingDate = System.currentTimeMillis() - 86400000 * 5,
                    attendeesPresent = "بن يحيى عبد القادر, بلحاج عيسى أحمد, طاهري فاطمة, قادري محمد",
                    attendeesAbsent = "بلعباس يوسف",
                    decisionsSummary = "تمت دراسة الملفات المعروضة على اللجنة وتمت الموافقة على منحة الختان للموظف سفيان براهيمي، وكذلك تقديم سلفة استثنائية بقيمة 50,000 دج للموظفة ليلى بن منصور. تم تأجيل دراسة الملفات الأخرى للاجتماع المقبل.",
                    signaturesJson = """{"بن يحيى عبد القادر":"signed_ok","بلحاج عيسى أحمد":"signed_ok","طاهري فاطمة":"signed_ok","قادري محمد":"signed_ok"}"""
                )
            )

            // 6. Default settings
            dao.insertSetting(AppSettingEntity("backup_email", "admin.zemmora.services@gmail.com"))
            dao.insertSetting(AppSettingEntity("admin_password", "admin4812")) // Default requested password
            dao.insertSetting(AppSettingEntity("app_logo_url", "default_logo"))
        }
    }
}
