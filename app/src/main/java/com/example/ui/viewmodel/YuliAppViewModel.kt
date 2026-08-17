package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AnimalEntity
import com.example.data.AppDatabase
import com.example.data.FamilyEntity
import com.example.data.YuliAppRepository
import com.example.ui.audio.AudioRecordManager
import com.example.ui.audio.YuliAudioSynth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class YuliAppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: YuliAppRepository
    private val audioRecordManager = AudioRecordManager(application)
    private val sharedPrefs = application.getSharedPreferences("yuliapp_prefs", Context.MODE_PRIVATE)

    private val _backgroundVideoPath = MutableStateFlow<String?>(null)
    val backgroundVideoPath = _backgroundVideoPath.asStateFlow()

    init {
        _backgroundVideoPath.value = sharedPrefs.getString("bg_video_path", null)
        val database = AppDatabase.getDatabase(application)
        repository = YuliAppRepository(database.yuliAppDao())

        // Populate defaults only on the very first launch.
        if (!sharedPrefs.getBoolean("seeded_defaults", false)) {
            viewModelScope.launch {
                if (repository.allAnimals.first().isEmpty()) {
                    val defaultAnimals = listOf(
                        AnimalEntity("cow", "Vaca", "🐮", "#ff4b4b", "COW"),
                        AnimalEntity("pig", "Cerdo", "🐷", "#FCE4EC", "PIG"),
                        AnimalEntity("sheep", "Oveja", "🐑", "#ECEFF1", "SHEEP"),
                        AnimalEntity("duck", "Pato", "🦆", "#FFFDE7", "DUCK"),
                        AnimalEntity("lion", "León", "🦁", "#FFF3E0", "LION"),
                        AnimalEntity("frog", "Rana", "🐸", "#E8F5E9", "FROG")
                    )
                    database.yuliAppDao().insertAnimals(defaultAnimals)
                }

                if (repository.allFamily.first().isEmpty()) {
                    val defaultFamily = listOf(
                        FamilyEntity(relation = "Mamá", emoji = "👩", colorHex = "#ff4b4b"),
                        FamilyEntity(relation = "Papá", emoji = "👨", colorHex = "#90CAF9"),
                        FamilyEntity(relation = "Abuela", emoji = "👵", colorHex = "#CE93D8"),
                        FamilyEntity(relation = "Abuelo", emoji = "👴", colorHex = "#80CBC4")
                    )
                    database.yuliAppDao().insertFamilyMembers(defaultFamily)
                }

                sharedPrefs.edit().putBoolean("seeded_defaults", true).apply()
            }
        }
    }

    // Reactively observe animals and family
    val animals: StateFlow<List<AnimalEntity>> = repository.allAnimals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val family: StateFlow<List<FamilyEntity>> = repository.allFamily
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    private val _currentScreen = MutableStateFlow("home") // "home", "colors", "animals", "piano", "family", "drawing", "legal"
    val currentScreen = _currentScreen.asStateFlow()

    private val _isParentMode = MutableStateFlow(false)
    val isParentMode = _isParentMode.asStateFlow()

    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled = _isAudioEnabled.asStateFlow()

    private val _showTutorial = MutableStateFlow(false)
    val showTutorial = _showTutorial.asStateFlow()

    // Recording States
    private val _recordingEntityId = MutableStateFlow<String?>(null)
    val recordingEntityId = _recordingEntityId.asStateFlow()

    private val _playingCustomSoundPath = MutableStateFlow<String?>(null)
    val playingCustomSoundPath = _playingCustomSoundPath.asStateFlow()

    private val _playingAudioId = MutableStateFlow<String?>(null)
    val playingAudioId = _playingAudioId.asStateFlow()

    fun setScreen(screen: String) {
        _currentScreen.value = screen
        audioRecordManager.stopPlayback()
        _playingAudioId.value = null
    }

    fun toggleParentMode() {
        _isParentMode.value = !_isParentMode.value
    }

    fun toggleAudio() {
        _isAudioEnabled.value = !_isAudioEnabled.value
    }

    fun showTutorial(show: Boolean) {
        _showTutorial.value = show
    }

    // Play action for animal sounds
    fun playAnimalSound(animal: AnimalEntity) {
        if (!_isAudioEnabled.value) return
        _playingAudioId.value = animal.id
        if (animal.isCustomSoundEnabled && animal.customSoundPath != null) {
            _playingCustomSoundPath.value = animal.customSoundPath
            audioRecordManager.playRecordedFile(animal.customSoundPath) {
                _playingCustomSoundPath.value = null
                _playingAudioId.value = null
            }
        } else {
            val url = when (animal.synthType) {
                "COW" -> "https://actions.google.com/sounds/v1/animals/cattle_moo.ogg"
                "PIG" -> "https://actions.google.com/sounds/v1/animals/pig_grunt.ogg"
                "SHEEP" -> "https://actions.google.com/sounds/v1/animals/sheep_bleat.ogg"
                "DUCK" -> "https://actions.google.com/sounds/v1/animals/duck_quack.ogg"
                "LION" -> "https://actions.google.com/sounds/v1/animals/lion_growl.ogg"
                "FROG" -> "https://actions.google.com/sounds/v1/animals/frog_croak.ogg"
                else -> null
            }

            if (url != null) {
                audioRecordManager.playUrl(
                    url = url,
                    onFinished = {
                        _playingAudioId.value = null
                    },
                    onError = {
                        playAnimalSynthFallback(animal.synthType)
                        _playingAudioId.value = null
                    }
                )
            } else {
                YuliAudioSynth.playPop()
                _playingAudioId.value = null
            }
        }
    }

    private fun playAnimalSynthFallback(synthType: String) {
        if (!_isAudioEnabled.value) return
        when (synthType) {
            "COW" -> YuliAudioSynth.playCow()
            "PIG" -> YuliAudioSynth.playPig()
            "SHEEP" -> YuliAudioSynth.playSheep()
            "DUCK" -> YuliAudioSynth.playDuck()
            "LION" -> YuliAudioSynth.playLion()
            "FROG" -> YuliAudioSynth.playFrog()
            else -> YuliAudioSynth.playPop()
        }
    }

    // Play action for family cards
    fun playFamilySound(member: FamilyEntity) {
        if (!_isAudioEnabled.value) return
        val audioId = "family_${member.id}"
        _playingAudioId.value = audioId
        if (member.isCustomSoundEnabled && member.customSoundPath != null) {
            _playingCustomSoundPath.value = member.customSoundPath
            audioRecordManager.playRecordedFile(member.customSoundPath) {
                _playingCustomSoundPath.value = null
                _playingAudioId.value = null
            }
        } else {
            YuliAudioSynth.playFamilySparkle()
            _playingAudioId.value = null
        }
    }

    // Voice recording management
    fun startRecording(entityId: String) {
        val path = audioRecordManager.startRecording(entityId)
        if (path != null) {
            _recordingEntityId.value = entityId
        }
    }

    fun stopRecordingAndSaveAnimal(animal: AnimalEntity) {
        val path = audioRecordManager.stopRecording()
        _recordingEntityId.value = null
        if (path != null) {
            viewModelScope.launch {
                val updated = animal.copy(
                    customSoundPath = path,
                    isCustomSoundEnabled = true
                )
                repository.updateAnimal(updated)
            }
        }
    }

    fun stopRecordingAndSaveFamily(member: FamilyEntity) {
        val path = audioRecordManager.stopRecording()
        _recordingEntityId.value = null
        if (path != null) {
            viewModelScope.launch {
                val updated = member.copy(
                    customSoundPath = path,
                    isCustomSoundEnabled = true
                )
                repository.updateFamilyMember(updated)
            }
        }
    }

    fun deleteAnimalVoice(animal: AnimalEntity) {
        viewModelScope.launch {
            val updated = animal.copy(
                customSoundPath = null,
                isCustomSoundEnabled = false
            )
            repository.updateAnimal(updated)
        }
    }

    fun toggleAnimalCustomVoice(animal: AnimalEntity, enabled: Boolean) {
        viewModelScope.launch {
            val updated = animal.copy(isCustomSoundEnabled = enabled)
            repository.updateAnimal(updated)
        }
    }

    fun deleteFamilyVoice(member: FamilyEntity) {
        viewModelScope.launch {
            val updated = member.copy(
                customSoundPath = null,
                isCustomSoundEnabled = false
            )
            repository.updateFamilyMember(updated)
        }
    }

    fun toggleFamilyCustomVoice(member: FamilyEntity, enabled: Boolean) {
        viewModelScope.launch {
            val updated = member.copy(isCustomSoundEnabled = enabled)
            repository.updateFamilyMember(updated)
        }
    }

    // Manage Family Cards (CRUD)
    fun addFamilyMember(relation: String, emoji: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertFamilyMember(
                FamilyEntity(
                    relation = relation,
                    emoji = emoji,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateFamilyMember(member: FamilyEntity) {
        viewModelScope.launch {
            repository.updateFamilyMember(member)
        }
    }

    fun deleteFamilyMember(id: Int) {
        viewModelScope.launch {
            repository.deleteFamilyMember(id)
        }
    }

    // Manage Custom Animals (CRUD)
    fun addAnimal(name: String, emoji: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertAnimal(
                AnimalEntity(
                    id = "custom_" + System.currentTimeMillis().toString(),
                    name = name,
                    emoji = emoji,
                    colorHex = colorHex,
                    synthType = "CUSTOM",
                    customSoundPath = null,
                    isCustomSoundEnabled = false
                )
            )
        }
    }

    fun deleteAnimal(id: String) {
        viewModelScope.launch {
            repository.deleteAnimal(id)
        }
    }

    // Imported Audio Management (MP3 / WAV)
    fun saveImportedAudioForAnimal(animal: AnimalEntity, uri: android.net.Uri) {
        val path = audioRecordManager.saveImportedAudio(uri, "animal_${animal.id}")
        if (path != null) {
            viewModelScope.launch {
                val updated = animal.copy(
                    customSoundPath = path,
                    isCustomSoundEnabled = true
                )
                repository.updateAnimal(updated)
            }
        }
    }

    fun saveImportedAudioForFamily(member: FamilyEntity, uri: android.net.Uri) {
        val path = audioRecordManager.saveImportedAudio(uri, "family_${member.id}")
        if (path != null) {
            viewModelScope.launch {
                val updated = member.copy(
                    customSoundPath = path,
                    isCustomSoundEnabled = true
                )
                repository.updateFamilyMember(updated)
            }
        }
    }

    fun savePhotoForFamily(member: FamilyEntity, uri: android.net.Uri) {
        val path = audioRecordManager.saveImportedPhoto(uri, "family_${member.id}")
        if (path != null) {
            viewModelScope.launch {
                val updated = member.copy(
                    customPhotoPath = path
                )
                repository.updateFamilyMember(updated)
            }
        }
    }

    fun deleteFamilyPhoto(member: FamilyEntity) {
        viewModelScope.launch {
            val updated = member.copy(
                customPhotoPath = null
            )
            repository.updateFamilyMember(updated)
        }
    }

    fun saveBackgroundVideo(uri: android.net.Uri) {
        val path = audioRecordManager.saveImportedVideo(uri)
        if (path != null) {
            sharedPrefs.edit().putString("bg_video_path", path).apply()
            _backgroundVideoPath.value = path
        }
    }

    fun clearBackgroundVideo() {
        sharedPrefs.edit().remove("bg_video_path").apply()
        _backgroundVideoPath.value = null
    }

    override fun onCleared() {
        super.onCleared()
        audioRecordManager.stopPlayback()
        audioRecordManager.stopRecording()
    }
}
