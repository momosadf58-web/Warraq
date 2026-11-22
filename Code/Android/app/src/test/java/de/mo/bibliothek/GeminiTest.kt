package de.mo.bibliothek

import de.mo.bibliothek.data.GeminiException
import de.mo.bibliothek.data.GeminiFailure
import de.mo.bibliothek.data.WarraqAi
import de.mo.bibliothek.data.WarraqAi.GeminiResponse
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class GeminiTest {
    private val models = listOf(
        "gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-3.8-flash", "gemini-3.6-flash",
    )
    private val answer = """{"candidates":[{"content":{"parts":[{"text":"{\"answer\":\"نعم\"}"}]}}]}"""
    private val blocked = """{"promptFeedback":{"blockReason":"SAFETY"}}"""

    @Test fun requestBodyKeepsPersonaAndArabicUserPrompt() {
        val request = WarraqAi.geminiBody("اقترح كتابًا")
        val user = request.getJSONArray("contents").getJSONObject(0)
        assertEquals("user", user.getString("role"))
        assertEquals("اقترح كتابًا", user.getJSONArray("parts").getJSONObject(0).getString("text"))
        assertTrue(request.getJSONObject("systemInstruction").getJSONArray("parts")
            .getJSONObject(0).getString("text").contains("Allam"))
        val config = request.getJSONObject("generationConfig")
        assertEquals("application/json", config.getString("responseMimeType"))
        assertEquals(0.5, config.getDouble("temperature"), 0.0)
    }

    @Test fun concatenatesOnlyVisibleTextFromFirstCandidate() {
        val response = JSONObject("""
            {"candidates":[
              {"content":{"parts":[
                {"text":"```json\n{\"answer\":\""},
                {"thought":true,"text":"interner Text"},
                {"functionCall":{"name":"irrelevant"}},
                {"text":"نعم\"}\n```"}
              ]}},
              {"content":{"parts":[{"text":"second candidate"}]}}
            ]}
        """.trimIndent())
        assertEquals("نعم", WarraqAi.parseGeminiResponse(response)?.getString("answer"))
    }

    @Test fun missingEmptyAndBlockedResponsesAreUnusable() {
        for (body in listOf(
            "{}",
            """{"candidates":[]}""",
            blocked,
            """{"candidates":[{"finishReason":"SAFETY","content":{"parts":[{"text":"{}"}]}}]}""",
            """{"candidates":[{"content":{"parts":[{"thought":true,"text":"{}"},{"text":" "}]}}]}""",
        )) assertNull(body, WarraqAi.parseGeminiResponse(JSONObject(body)))
    }

    @Test fun fallsBackInOrderForRetryableStatusesAndEmptyOrBlockedAnswers() {
        for (first in listOf(
            GeminiResponse(429), GeminiResponse(404), GeminiResponse(500), GeminiResponse(503),
            GeminiResponse(200), GeminiResponse(200, "{}"), GeminiResponse(200, blocked),
        )) {
            val tried = mutableListOf<String>()
            val result = WarraqAi.chatGemini { model ->
                tried += model
                when (tried.size) {
                    1 -> first
                    2, 3 -> GeminiResponse(503)
                    else -> GeminiResponse(200, answer)
                }
            }
            assertEquals(models, tried)
            assertEquals("نعم", result.getString("answer"))
        }
        // A successful fallback must not change the order for later requests.
        assertEquals("نعم", WarraqAi.chatGemini { model ->
            assertEquals(models.first(), model)
            GeminiResponse(200, answer)
        }.getString("answer"))
    }

    @Test fun rejectedKeysStopTheModelChain() {
        val invalidKey = """{"error":{"status":"INVALID_ARGUMENT","details":[{"reason":"API_KEY_INVALID"}]}}"""
        for (response in listOf(GeminiResponse(400, invalidKey), GeminiResponse(401), GeminiResponse(403))) {
            val tried = mutableListOf<String>()
            val error = assertThrows(GeminiException::class.java) {
                WarraqAi.chatGemini { model -> tried += model; response }
            }
            assertEquals(listOf(models.first()), tried)
            assertEquals(GeminiFailure.KEY_REJECTED, error.failure)
        }
    }

    @Test fun unrelatedBadRequestStopsWithoutMarkingTheKeyInvalid() {
        val tried = mutableListOf<String>()
        val error = assertThrows(IOException::class.java) {
            WarraqAi.chatGemini { model -> tried += model; GeminiResponse(400, "not JSON") }
        }
        assertEquals(listOf(models.first()), tried)
        assertFalse(error is GeminiException)
    }

    @Test fun quotaMessageRequiresAllModelsToReturn429() {
        val tried = mutableListOf<String>()
        val error = assertThrows(GeminiException::class.java) {
            WarraqAi.chatGemini { model -> tried += model; GeminiResponse(429) }
        }
        assertEquals(models, tried)
        assertEquals(GeminiFailure.QUOTA_EXHAUSTED, error.failure)

        val mixedError = assertThrows(IOException::class.java) {
            WarraqAi.chatGemini { model -> GeminiResponse(if (model == models.first()) 503 else 429) }
        }
        assertFalse(mixedError is GeminiException)
    }
}
