package com.andiasrafil.asahaitest.helpers.enums


enum class VoiceGender {
    male, female
}
enum class Voice(
    val id: Int,
    val displayName: String,
    val voiceGender: VoiceGender,
) {
    MEADOW(
        id = 1,
        displayName = "Meadow",
        voiceGender = VoiceGender.female
    ),
    CYPRESS(
        id = 2,
        displayName = "Cypress",
        voiceGender = VoiceGender.male
    ),
    IRIS(
        id = 3,
        displayName = "Iris",
        voiceGender = VoiceGender.female
    ),
    HAWKE(
        id = 4,
        displayName = "Hawke",
        voiceGender = VoiceGender.male
    ),
    SEREN(
        id = 5,
        displayName = "Seren",
        voiceGender = VoiceGender.female
    ),
    STONE(
        id = 6,
        displayName = "Stone",
        voiceGender = VoiceGender.male
    );

    fun getImageUrl(): String {
        return "https://static.dailyfriend.ai/images/voices/${displayName.lowercase()}.svg"
    }

    fun getVoiceSample(index: Int): String {
        return "https://static.dailyfriend.ai/conversations/samples/${id}/${index}/audio.mp3"
    }
}

