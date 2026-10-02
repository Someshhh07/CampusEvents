package com.example.data.repository

import com.example.data.local.EventReportEntity
import com.example.data.local.ReportDao
import kotlinx.coroutines.flow.Flow

class ReportRepository(private val reportDao: ReportDao) {
    val allReports: Flow<List<EventReportEntity>> = reportDao.getAllReports()

    suspend fun submitReport(report: EventReportEntity) {
        reportDao.insertReport(report)
    }

    suspend fun resolveReport(reportId: String, status: String, notes: String) {
        reportDao.updateReportStatus(reportId, status, notes)
    }
}
