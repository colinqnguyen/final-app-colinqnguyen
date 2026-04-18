package edu.nd.cnguyen8.hwapp.five.data.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import androidx.health.connect.client.records.metadata.Metadata

@Singleton
class HealthConnectManager @Inject constructor(
    val context: Context
) {
    private val client by lazy { HealthConnectClient.getOrCreate(context) }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class)
    )

    suspend fun hasAllPermissions(): Boolean {
        return client.permissionController
            .getGrantedPermissions()
            .containsAll(permissions)
    }

    suspend fun readTodaySteps(): Int {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = LocalDate.now()
            .atStartOfDay(zoneId)
            .toInstant()
        val now = Instant.now()

        val response = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfDay, now)
            )
        )

        return response[StepsRecord.COUNT_TOTAL]?.toInt() ?: 0
    }

    suspend fun insertTestSteps(count: Long) {
        val endTime = Instant.now()
        val startTime = endTime.minusSeconds(60 * 5)

        val record = StepsRecord(
            count = count,
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = ZoneId.systemDefault().rules.getOffset(startTime),
            endZoneOffset = ZoneId.systemDefault().rules.getOffset(endTime),
            metadata = Metadata.manualEntry()
        )

        client.insertRecords(listOf(record))
    }

    companion object {
        fun isAvailable(context: Context): Int {
            return HealthConnectClient.getSdkStatus(context)
        }
    }
}