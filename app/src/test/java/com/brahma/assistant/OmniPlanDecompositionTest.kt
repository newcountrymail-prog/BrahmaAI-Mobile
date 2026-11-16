package com.brahma.assistant

import com.brahma.assistant.planner.PlanStep
import com.brahma.assistant.planner.RawPlanResponseJson
import com.brahma.assistant.planner.SubagentRole
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OmniPlanDecompositionTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun testPlanGraphParsing() {
        val sampleJson = """
            {
              "steps": [
                {
                  "stepId": 1,
                  "title": "Query Battery Level",
                  "description": "Inspect battery percentage from BatteryManager",
                  "assignedRole": "EXECUTOR",
                  "actionType": "SYSTEM_DIAGNOSTICS",
                  "actionParameters": {"metric": "battery"}
                },
                {
                  "stepId": 2,
                  "title": "Enable Battery Saver",
                  "description": "Navigate to settings and activate battery saver mode",
                  "assignedRole": "NAVIGATOR",
                  "actionType": "DEVICE_SETTINGS",
                  "actionParameters": {"setting": "battery_saver"}
                }
              ]
            }
        """.trimIndent()

        val parsed = json.decodeFromString<RawPlanResponseJson>(sampleJson)
        assertEquals(2, parsed.steps.size)
        assertEquals("SYSTEM_DIAGNOSTICS", parsed.steps[0].actionType)
        assertEquals("battery", parsed.steps[0].actionParameters["metric"])
        assertEquals("NAVIGATOR", parsed.steps[1].assignedRole)
    }
}
