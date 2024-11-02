// NFCHistoryViewModel.kt
package ma.ensa.projet.Controller

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ma.ensa.projet.beans.NFCDataType
import ma.ensa.projet.beans.NFCTag
import ma.ensa.projet.database.NFCTagDatabase
import java.util.Date

class NFCHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val _filteredTags = MutableLiveData<List<NFCTag>>()
    val filteredTags: LiveData<List<NFCTag>> get() = _filteredTags

    private var currentQuery = ""
    private val activeFilters = mutableSetOf<NFCDataType>()
    private val database = NFCTagDatabase.getDatabase(application)
    private val nfcTagDao = database.nfcTagDao()

    val allTags: LiveData<List<NFCTag>> = nfcTagDao.getAllTags().asLiveData()

    init {
        allTags.observeForever {
            applyFilters() // Appel automatique pour appliquer le filtre à chaque mise à jour
        }
    }


    fun searchTags(query: String) {
        currentQuery = query
        applyFilters()
    }

    fun updateFilter(type: NFCDataType, isActive: Boolean) {
        if (isActive) {
            activeFilters.add(type)
        } else {
            activeFilters.remove(type)
        }
        applyFilters()
    }

    private fun applyFilters() {
        val filteredList = allTags.value?.filter { tag ->
            val matchesQuery = tag.content.contains(currentQuery, ignoreCase = true)
            val matchesFilter = activeFilters.isEmpty() || tag.type in activeFilters
            matchesQuery && matchesFilter
        } ?: emptyList()

        _filteredTags.value = filteredList // Update filteredTags instead of allTags
    }


    fun addTag(tag: NFCTag) {
        viewModelScope.launch {
            nfcTagDao.insert(tag.copy(timestamp = Date()))
            applyFilters()  // Update filters if necessary after adding
        }
    }
    fun deleteTag(tag: NFCTag) {
        viewModelScope.launch {
            nfcTagDao.delete(tag)
            applyFilters() // Refresh the filtered list after deletion
        }
    }


}
