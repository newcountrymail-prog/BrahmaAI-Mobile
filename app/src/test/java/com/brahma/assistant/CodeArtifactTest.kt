package com.brahma.assistant

import com.brahma.assistant.code.CodeArtifact
import com.brahma.assistant.code.CodeLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CodeArtifactTest {

    @Test
    fun testCodeArtifactCreation() {
        val artifact = CodeArtifact(
            filename = "calculator.py",
            language = CodeLanguage.PYTHON,
            sourceCode = "print(2 + 2)"
        )

        assertNotNull(artifact.id)
        assertEquals("calculator.py", artifact.filename)
        assertEquals(CodeLanguage.PYTHON, artifact.language)
        assertEquals("print(2 + 2)", artifact.sourceCode)
    }
}
