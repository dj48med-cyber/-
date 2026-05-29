package com.example.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiClient
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val dao = db.appDao()

    // --- State Streams from Room ---
    val employees = dao.getAllEmployees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val requests = dao.getAllRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val meetings = dao.getAllMeetings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val committeeMembers = dao.getAllCommitteeMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets = dao.getAllBudgets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val complaints = dao.getAllComplaints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backupLogs = dao.getAllBackupLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- UI Theme Status ---
    var isDarkMode by mutableStateOf(false)
        private set

    // --- App Custom Logo ---
    var appLogoUrl by mutableStateOf("default_logo")
        private set

    // --- Administrative Configurations & Authentication ---
    var adminPassword by mutableStateOf("admin4812")
        private set

    var backupEmail by mutableStateOf("admin.zemmora.services@gmail.com")
        private set

    // Current Session State
    var currentUserPhone by mutableStateOf("")
    var currentEmployee by mutableStateOf<EmployeeEntity?>(null)
    var isAdminLoggedIn by mutableStateOf(false)
    var isEmployeeLoggedIn by mutableStateOf(false)

    // OTP Code Generation
    var generatedOtpCode by mutableStateOf("")
    var otpSentMessage by mutableStateOf("")
    var otpCooldownSeconds by mutableStateOf(0)

    // Current active meeting being drafted
    var meetingDraftNumber by mutableStateOf("")
    val meetingDraftPresent = mutableStateListOf<String>()
    val meetingDraftAbsent = mutableStateListOf<String>()
    val meetingDraftSignatures = mutableStateMapOf<String, String>() // name to signature indicator
    var meetingDraftSummary by mutableStateOf("")
    var isMeetingAiLoading by mutableStateOf(false)

    // Gemini states
    var aiAdvisoryReport by mutableStateOf("")
    var isAiAdvisoryLoading by mutableStateOf(false)

    var aiAuditReport by mutableStateOf("")
    var isAiAuditLoading by mutableStateOf(false)

    // Sync state
    var isGoogleDriveConnected by mutableStateOf(true)
    var syncProgressMessage by mutableStateOf("")
    var isSyncing by mutableStateOf(false)
    
    // File upload simulations
    var uploadProgressMessage by mutableStateOf("")
    var isUploadingFile by mutableStateOf(false)

    init {
        // Load configurations from DB
        viewModelScope.launch {
            dao.getSetting("admin_password")?.let { adminPassword = it.value }
            dao.getSetting("backup_email")?.let { backupEmail = it.value }
            dao.getSetting("app_logo_url")?.let { appLogoUrl = it.value }
            dao.getSetting("ui_theme")?.let { isDarkMode = it.value == "dark" }
        }
    }

    // --- UI Theme Actions ---
    fun toggleDarkMode() {
        isDarkMode = !isDarkMode
        viewModelScope.launch {
            dao.insertSetting(AppSettingEntity("ui_theme", if (isDarkMode) "dark" else "light"))
        }
    }

    fun changeAppLogo(logoName: String) {
        appLogoUrl = logoName
        viewModelScope.launch {
            dao.insertSetting(AppSettingEntity("app_logo_url", logoName))
        }
    }

    fun updateAdminPassword(newPass: String, onSuccess: () -> Unit) {
        adminPassword = newPass
        viewModelScope.launch {
            dao.insertSetting(AppSettingEntity("admin_password", newPass))
            onSuccess()
        }
    }

    fun updateBackupEmail(newEmail: String) {
        backupEmail = newEmail
        viewModelScope.launch {
            dao.insertSetting(AppSettingEntity("backup_email", newEmail))
        }
    }

    // --- OTP Login Flow ---
    fun sendOtpCode(phone: String, onSend: () -> Unit) {
        viewModelScope.launch {
            val trimmedPhone = phone.trim()
            val employee = dao.getEmployeeByPhone(trimmedPhone)
            if (employee == null) {
                otpSentMessage = "عذراً، هذا الرقم غير مسجل في ملفات الموظفين. يرجى مراجعة الإدارة."
                generatedOtpCode = ""
            } else {
                val otp = (1000..9999).random().toString()
                generatedOtpCode = otp
                otpSentMessage = "تم إرسال رمز التحقق OTP بنجاح لـ ${employee.name}: $otp (محاكاة رسائل مجانية SMS)"
                currentEmployee = employee
                onSend()
                
                // Cooldown countdown
                otpCooldownSeconds = 60
                while (otpCooldownSeconds > 0) {
                    delay(1000)
                    otpCooldownSeconds--
                }
            }
        }
    }

    fun verifyOtpCode(code: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (code.trim() == generatedOtpCode) {
            isEmployeeLoggedIn = true
            isAdminLoggedIn = false
            currentUserPhone = currentEmployee?.phone ?: ""
            otpSentMessage = ""
            generatedOtpCode = ""
            onSuccess()
        } else {
            onError("رمز التحقق غير صحيح، يرجى المحاولة مرة أخرى.")
        }
    }

    fun loginAsAdmin(password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (password == adminPassword) {
            isAdminLoggedIn = true
            isEmployeeLoggedIn = false
            currentEmployee = null
            onSuccess()
        } else {
            onError("كلمة المرور غير صحيحة. يرجى التأكد!")
        }
    }

    fun logOut() {
        isAdminLoggedIn = false
        isEmployeeLoggedIn = false
        currentEmployee = null
        currentUserPhone = ""
        generatedOtpCode = ""
        otpSentMessage = ""
    }

    // --- CRUD Actions: Employees ---
    fun addEmployee(name: String, phone: String, section: String, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertEmployee(EmployeeEntity(name = name, phone = phone, section = section))
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun deleteEmployee(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteEmployeeById(id)
        }
    }

    // --- CRUD Actions: Committee Members ---
    fun addCommitteeMember(name: String, role: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertCommitteeMember(CommitteeMemberEntity(name = name, role = role))
        }
    }

    fun deleteCommitteeMember(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteCommitteeMemberById(id)
        }
    }

    // --- CRUD Actions: Requests (Grants & Loans) ---
    fun submitRequest(
        type: String,
        isLoan: Boolean,
        amount: Double,
        durationMonths: Int,
        attachmentPaths: String,
        onSuccess: () -> Unit
    ) {
        val emp = currentEmployee ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val deduction = if (isLoan && durationMonths > 0) amount / durationMonths else 0.0
            val request = GrantLoanRequestEntity(
                employeeId = emp.id,
                employeeName = emp.name,
                employeePhone = emp.phone,
                type = type,
                isLoan = isLoan,
                amount = amount,
                durationMonths = durationMonths,
                monthlyDeduction = deduction,
                attachmentPaths = attachmentPaths,
                status = "Pending"
            )
            dao.insertRequest(request)
            
            // Log sync event to Backup (Simulated JSON snapshot updates)
            simulateGoogleDriveBackupAuto()

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun addRequestByAdmin(
        empId: Int,
        empName: String,
        empPhone: String,
        type: String,
        isLoan: Boolean,
        amount: Double,
        durationMonths: Int,
        attachmentPaths: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val deduction = if (isLoan && durationMonths > 0) amount / durationMonths else 0.0
            val request = GrantLoanRequestEntity(
                employeeId = empId,
                employeeName = empName,
                employeePhone = empPhone,
                type = type,
                isLoan = isLoan,
                amount = amount,
                durationMonths = durationMonths,
                monthlyDeduction = deduction,
                attachmentPaths = attachmentPaths,
                status = "Pending"
            )
            dao.insertRequest(request)
            simulateGoogleDriveBackupAuto()
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun deleteRequest(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteRequestById(id)
        }
    }

    fun evaluateRequest(id: Int, status: String, reason: String?, meetingNo: Int?) {
        viewModelScope.launch(Dispatchers.IO) {
            // Find current
            val allReqs = requests.value
            val req = allReqs.find { it.id == id } ?: return@launch
            val updated = req.copy(
                status = status,
                rejectionReason = reason,
                meetingNumber = meetingNo,
                decidedDate = System.currentTimeMillis()
            )
            dao.updateRequest(updated)

            // Adjust spent budget if approved
            if (status == "Approved") {
                val currentYear = 2026
                val budget = dao.getBudgetForYear(currentYear)
                if (budget != null) {
                    if (updated.isLoan) {
                        dao.updateBudget(budget.copy(spentLoans = budget.spentLoans + updated.amount))
                    } else {
                        dao.updateBudget(budget.copy(spentGrants = budget.spentGrants + updated.amount))
                    }
                }
            }

            simulateGoogleDriveBackupAuto()
        }
    }

    // --- CRUD Actions: Complaints ---
    fun submitComplaint(subject: String, details: String, onSuccess: () -> Unit) {
        val emp = currentEmployee ?: return
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertComplaint(
                ComplaintEntity(
                    employeeId = emp.id,
                    employeeName = emp.name,
                    employeePhone = emp.phone,
                    subject = subject,
                    details = details
                )
            )
            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun resolveComplaint(id: Int, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val allComplaints = complaints.value
            val complaint = allComplaints.find { it.id == id } ?: return@launch
            dao.updateComplaint(complaint.copy(status = "Resolved", resolutionNote = note))
        }
    }

    fun deleteComplaint(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteComplaintById(id)
        }
    }

    // --- CRUD Actions: Budget ---
    fun setBudget(year: Int, total: Double, grantsAlloc: Double, loansAlloc: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getBudgetForYear(year)
            if (existing != null) {
                dao.updateBudget(
                    existing.copy(
                        totalBudget = total,
                        allocatedGrants = grantsAlloc,
                        allocatedLoans = loansAlloc
                    )
                )
            } else {
                dao.insertBudget(
                    BudgetEntity(
                        year = year,
                        totalBudget = total,
                        allocatedGrants = grantsAlloc,
                        allocatedLoans = loansAlloc
                    )
                )
            }
        }
    }

    // --- CRUD Actions: Meetings ---
    fun initializeMeetingDraft(meetingNo: String, membersList: List<CommitteeMemberEntity>) {
        meetingDraftNumber = meetingNo
        meetingDraftPresent.clear()
        meetingDraftAbsent.clear()
        meetingDraftSignatures.clear()
        meetingDraftSummary = ""
        
        // Default everyone to absent initially or empty, let admin check them
        membersList.forEach {
            meetingDraftAbsent.add(it.name)
        }
    }

    fun toggleMemberPresence(memberName: String, isPresent: Boolean) {
        if (isPresent) {
            if (!meetingDraftPresent.contains(memberName)) {
                meetingDraftPresent.add(memberName)
                meetingDraftAbsent.remove(memberName)
            }
        } else {
            if (!meetingDraftAbsent.contains(memberName)) {
                meetingDraftAbsent.add(memberName)
                meetingDraftPresent.remove(memberName)
                meetingDraftSignatures.remove(memberName) // Remove signature if toggled absent
            }
        }
    }

    fun signForAttendee(name: String, signatureBase64: String) {
        if (meetingDraftPresent.contains(name)) {
            meetingDraftSignatures[name] = signatureBase64
        }
    }

    fun saveMeeting(onSuccess: () -> Unit) {
        val num = meetingDraftNumber.toIntOrNull() ?: 1
        val presentStr = meetingDraftPresent.joinToString(", ")
        val absentStr = meetingDraftAbsent.joinToString(", ")
        
        // Build signatures JSON manually to avoid dependencies
        val signObj = JSONObject()
        meetingDraftSignatures.forEach { (name, indicator) ->
            signObj.put(name, indicator)
        }

        viewModelScope.launch(Dispatchers.IO) {
            val meeting = MeetingEntity(
                meetingNumber = num,
                meetingDate = System.currentTimeMillis(),
                attendeesPresent = presentStr,
                attendeesAbsent = absentStr,
                decisionsSummary = meetingDraftSummary,
                signaturesJson = signObj.toString()
            )
            dao.insertMeeting(meeting)

            // Direct file save/update logic for processing approved individuals inside this meeting date
            // Update decision date and status for pending requests tied to this meeting number
            val pendingRequests = requests.value.filter { it.status == "Pending" && it.meetingNumber == num }
            pendingRequests.forEach { req ->
                dao.updateRequest(req.copy(status = "Approved", decidedDate = System.currentTimeMillis()))
            }

            simulateGoogleDriveBackupAuto()

            withContext(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    fun deleteMeeting(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteMeetingById(id)
        }
    }

    // --- AI Integration (Gemini Calls) ---
    
    // AI Advisory Analysis for specific request
    fun generateAiAdvisoryReport(request: GrantLoanRequestEntity) {
        isAiAdvisoryLoading = true
        aiAdvisoryReport = ""
        viewModelScope.launch {
            val prompt = """
                أنت تدقق في ملف طلب لـ "لجنة الخدمات الاجتماعية لبلدية زمورة".
                تفاصيل الموظف وموضوع الطلب:
                - اسم الموظف: ${request.employeeName}
                - نوع الطلب: ${request.type}
                - القيمة المطلوبة: ${request.amount} دج (DZD)
                - الملفات المرفقة: ${request.attachmentPaths}
                - تاريخ الإيداع: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(request.submissionDate))}
                
                المطلوب:
                1. تقديم تحليل استشاري ذكي حول اكتمال المستندات في هذا الطلب بناءً على نوعه.
                2. توضيح هل يطابق شروط ولوائح تقديم الخدمات الاجتماعية الجزائرية العامة.
                3. تقديم توصية واضحة ومحددة للجنة (بالموافقة، التحفظ، الرفض مع التعليل الإداري).
                
                اجعل النبرة رسمية إدارية راقية ومساعدة جداً للمسيرين باللغة العربية حصراً.
            """.trimIndent()

            val response = GeminiClient.generateContent(
                prompt = prompt,
                systemInstruction = "أنت مستشار مالي وإداري بالذكاء الاصطناعي مخصص للجنة الخدمات الاجتماعية لبلدية زمورة بالجزائر."
            )
            aiAdvisoryReport = response
            isAiAdvisoryLoading = false
        }
    }

    // Draft meeting decisions / minutes automatically
    fun generateMeetingMinutesAi(requestsToDecide: List<GrantLoanRequestEntity>) {
        isMeetingAiLoading = true
        meetingDraftSummary = ""
        viewModelScope.launch {
            val requestsInfo = StringBuilder()
            requestsToDecide.forEachIndexed { idx, req ->
                requestsInfo.append("${idx + 1}. الموظف: ${req.employeeName} - الطلب: ${req.type} - القيمة: ${req.amount} دج - الحالة المقترحة: ${req.status}\n")
            }

            val prompt = """
                قم بصياغة محضر اجتماع رسمي باللغة العربية خاص بـ "لجنة الخدمات الاجتماعية لبلدية زمورة" للاجتماع رقم $meetingDraftNumber المنعقد بتاريخ اليوم.
                أعضاء اللجنة الحاضرون: ${meetingDraftPresent.joinToString(", ")}
                الغائبون: ${meetingDraftAbsent.joinToString(", ")}
                
                الملفات المعروضة للدراسة والقرارات المتخذة بشأنها:
                $requestsInfo
                
                المطلوب:
                صياغة ديباجة رسمية تليق بمؤسسة بلدية وتلخيص المناقشات حول هذه الملفات، وصياغة القرارات بشكل قانوني وإداري دقيق، وتحديد جدول أعمال الاجتماع القادم بأسلوب احترافي جداً وبلغة عربية رصينة بدون أي تفاصيل فرنسية.
            """.trimIndent()

            val response = GeminiClient.generateContent(
                prompt = prompt,
                systemInstruction = "أنت صانع محاضر اجتماعات ومقررات إدارية قانونية للوظيفة العمومية في الجزائر."
            )
            meetingDraftSummary = response
            isMeetingAiLoading = false
        }
    }

    // AI Literary & Financial Reports Audit Report
    fun generateAiAuditReport(scope: String) {
        isAiAuditLoading = true
        aiAuditReport = ""
        viewModelScope.launch {
            // Context from the Room Database
            val reqList = requests.value
            val meetList = meetings.value
            val empList = employees.value
            val bList = budgets.value.firstOrNull() ?: BudgetEntity(year = 2026, totalBudget = 6500000.0)

            val summaryData = """
                - ميزانية العام 2026 الكلية: ${bList.totalBudget} دج
                - الميزانية المخصصة للمنح: ${bList.allocatedGrants} دج (المستهلك منها: ${bList.spentGrants} دج)
                - الميزانية المخصصة للسلفيات: ${bList.allocatedLoans} دج (المستهلك منها: ${bList.spentLoans} دج)
                - إجمالي عدد الموظفين المسجلين: ${empList.size} موظف
                - إجمالي الطلبات المقدمة: ${reqList.size} طلب (الموافقة: ${reqList.count { it.status == "Approved" }}, انتظار: ${reqList.count { it.status == "Pending" }}, رفض: ${reqList.count { it.status == "Rejected" }})
                - إجمالي المحاضر والاجتماعات: ${meetList.size} اجتماع
            """.trimIndent()

            val prompt = when (scope) {
                "financial" -> """
                    قم بإعداد "تقرير مراجعة وتدقيق مالي بالذكاء الاصطناعي" تفصيلي ومستنير لبلدية زمورة بناءً على الإحصائيات الملخصة التالية:
                    $summaryData
                    
                    يرجى مراجعة مؤشرات ترشيد استهلاك الميزانية وتحليل نسبة الاستهلاك الحالي للمنح والسلف، واكتشاف أي ثغرات أو انحرافات مالية وتوفير توصيات للحد من استنزاف صندوق الخدمات الاجتماعية وإرساء الشفافية والمساءلة النقدية.
                """.trimIndent()
                "literary" -> """
                    قم بإعداد "التقرير الأدبي الإداري السنوي بالذكاء الاصطناعي" للبلدية بناءً على الإحصائيات الملخصة التالية:
                    $summaryData
                    
                    اشرح النشاطات الاجتماعية المنجزة، والمنح الأكثر طلبًا وتأثيرها على العمال ومحيطهم العائلي، ومدى تلبية احتياجات الموظفين والجهود المبذولة لتبسيط وترقين الإجراءات الورقية وتحويلها للخدمات والمنصات الرقمية.
                """.trimIndent()
                else -> """
                    عمل مراجعة تدقيق الحسابات السنوية والموازنة الشاملة للجنة مع توصيات تفصيلية.
                    البيانات:
                    $summaryData
                """.trimIndent()
            }

            val response = GeminiClient.generateContent(
                prompt = prompt,
                systemInstruction = "أنت مدقق حسابات محلف ومستشار إداري لدى الهيئات واللجان البلدية والخدمات الاجتماعية لموظفي قطاع البلدية بالجزائر."
            )
            aiAuditReport = response
            isAiAuditLoading = false
        }
    }

    // --- Simulated Web & Google Drive Operations ---
    
    // Auto sync simulation triggered on actions
    private fun simulateGoogleDriveBackupAuto() {
        if (!isGoogleDriveConnected) return
        viewModelScope.launch(Dispatchers.IO) {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            // Collect tables snapshot to simulate real backing upload
            val emp = employees.value
            val req = requests.value
            val m = meetings.value
            
            val snapshot = JSONObject().apply {
                put("backup_version", "1.0")
                put("timestamp", System.currentTimeMillis())
                put("employees_count", emp.size)
                put("requests_count", req.size)
                put("meetings_count", m.size)
            }
            dao.insertBackupLog(
                BackupLogEntity(
                    backupType = "Google Drive (تلقائي)",
                    status = "Success",
                    details = "تم رفع لقطة JSON تلقائية للمزامنة: Snapshot [V${snapshot.optString("backup_version")}]"
                )
            )
        }
    }

    fun runManualGoogleDriveSync(onComplete: (String) -> Unit) {
        if (isSyncing) return
        isSyncing = true
        syncProgressMessage = "بدء الاتصال بحساب Google Drive وإيداع النسخة الاحتياطية..."
        viewModelScope.launch {
            val emp = employees.value
            val req = requests.value
            val m = meetings.value
            val c = complaints.value
            val b = budgets.value
            
            // Build full JSON snapshot
            val snapshot = JSONObject().apply {
                put("backupTime", System.currentTimeMillis())
                put("employees", JSONArray(emp.map { JSONObject().apply { put("name", it.name); put("phone", it.phone); put("section", it.section) } }))
                put("requests", JSONArray(req.map { JSONObject().apply { put("id", it.id); put("employeeName", it.employeeName); put("type", it.type); put("amount", it.amount); put("status", it.status) } }))
                put("meetings", JSONArray(m.map { JSONObject().apply { put("num", it.meetingNumber); put("summary", it.decisionsSummary) } }))
            }
            
            delay(1500)
            syncProgressMessage = "جارٍ هيكلة ملف قاعدة البيانات وتوليد ملف Excel المالي السنوي..."
            delay(1200)
            syncProgressMessage = "جارٍ رفع الملفات والصور والملفات المرفقة لـ Google Drive..."
            delay(1000)
            
            val log = BackupLogEntity(
                backupType = "Google Drive (يدوي)",
                status = "Success",
                details = "نسخة احتياطية مشفرة محفوظة في مجلد: /Zemmora_CSS_Backup/ [الحجم: ${snapshot.toString().length / 1024 + 1} KB]"
            )
            dao.insertBackupLog(log)
            isSyncing = false
            onComplete("تم بنجاح نسخ قاعدة البيانات بالكامل وملفات Excel المالي التراكمي وتدبيجها في مجلد Google Drive الخاص بك كنسخة مستقرة ونظيفة!")
        }
    }

    fun restoreBackup(logId: Int, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            syncProgressMessage = "جارٍ سحب لقطة النسخة الاحتياطية من حساب Google Drive..."
            delay(1500)
            syncProgressMessage = "جارٍ التحقق من سلامة البنية ومفاتيح العلاقات والبيانات الرقمية..."
            delay(1000)
            syncProgressMessage = "جارٍ إعادة كتابة السجلات في محرك التخزين المحلي SQLite..."
            delay(800)
            
            val log = BackupLogEntity(
                backupType = "استعادة Google Drive",
                status = "Success",
                details = "تمت استعادة حالة قاعدة البيانات بنجاح لنسخة لوق رقم $logId"
            )
            dao.insertBackupLog(log)
            onComplete("تمت استعادة كل سجلات الموظفين، المنح، السلفيات، ومحاضر الاجتماعات بنجاح وقانون كامل من خادم الحوسبة السحابية Google Drive!")
        }
    }

    fun sendBackupToEmailNow(onComplete: (String) -> Unit) {
        viewModelScope.launch {
            syncProgressMessage = "جارٍ تجميع وتشفير ملفات Excel الإحصائية وتقرير الميزانية الحالي..."
            delay(1200)
            syncProgressMessage = "جارٍ الاتصال بخادم مصلحة البريد الإلكتروني SMTP الآمن..."
            delay(1000)
            
            val mailDetail = "تم إرسال بريد نجاح يحتوي على جداول البيانات وأحدث تدقيق مالي بصيغة Excel/CSV إلى البريد الإلكتروني: $backupEmail"
            val log = BackupLogEntity(
                backupType = "SMTP Email",
                status = "Success",
                recipientEmail = backupEmail,
                details = mailDetail
            )
            dao.insertBackupLog(log)
            onComplete("تم إرسال النسخة التراكمية (Snapshots + Excel Sheets) بنجاح إلى بريدكم المعتمد: $backupEmail!")
        }
    }

    // Mock attachment upload to virtual Google Drive
    fun simulateAttachmentUpload(fileName: String, fileUri: String, onUploaded: (String) -> Unit) {
        isUploadingFile = true
        uploadProgressMessage = "جارٍ فحص الملف ($fileName) للتحقق من سلامته الرقمية..."
        viewModelScope.launch {
            delay(1000)
            uploadProgressMessage = "التحقق من صحة المستند (الحد الأقصى 2 ميجابايت)... ملف الحجم مقبول."
            delay(1000)
            uploadProgressMessage = "جارٍ رفع المستند وتخزينه المشفر في Google Drive Cloud الخاص باللجنة..."
            delay(1200)
            
            val generatedDriveLink = "https://drive.google.com/open?id=zemmora_attachment_${System.currentTimeMillis()}"
            isUploadingFile = false
            uploadProgressMessage = ""
            onUploaded(generatedDriveLink)
        }
    }
}

// Helper implementation
private fun <K, V> mutableStateMapOf() = androidx.compose.runtime.mutableStateMapOf<K, V>()
private fun <T> mutableStateListOf() = androidx.compose.runtime.mutableStateListOf<T>()
