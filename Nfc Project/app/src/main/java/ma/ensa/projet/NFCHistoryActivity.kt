package ma.ensa.projet

import android.os.Bundle
import android.widget.SearchView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import androidx.recyclerview.widget.ItemTouchHelper
import ma.ensa.projet.Controller.NFCHistoryViewModel
import ma.ensa.projet.adapter.NFCHistoryAdapter
import ma.ensa.projet.beans.NFCDataType
import ma.ensa.projet.beans.NFCTag

class NFCHistoryActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var filterChipGroup: ChipGroup
    private val nfcHistoryAdapter = NFCHistoryAdapter()
    private val viewModel: NFCHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfchistory)

        setupToolbar()
        setupRecyclerView()
        setupSearchView()
        setupFilterChips()
        setupObservers()

        // Get and add the transmitted tag
        val receivedTag = intent.getParcelableExtra<NFCTag>("NFC_TAG")
        receivedTag?.let {
            viewModel.addTag(it)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NFCHistoryActivity)
            adapter = nfcHistoryAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }

        // Attach ItemTouchHelper for swipe to delete
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentList = nfcHistoryAdapter.currentList.toMutableList()
                    val tagToDelete = currentList[position]

                    // Show confirmation dialog
                    AlertDialog.Builder(this@NFCHistoryActivity)
                        .setTitle("Delete Tag")
                        .setMessage("Are you sure you want to delete this NFC tag?")
                        .setPositiveButton("Delete") { _, _ ->
                            // User confirmed deletion
                            viewModel.deleteTag(tagToDelete)
                            currentList.removeAt(position)
                            nfcHistoryAdapter.submitList(currentList)
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            // User canceled, refresh the list to restore the swiped item
                            nfcHistoryAdapter.notifyItemChanged(position)
                        }
                        .setCancelable(false) // Prevent dismissing by tapping outside the dialog
                        .show()
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun setupSearchView() {
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchTags(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchTags(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupFilterChips() {
        filterChipGroup = findViewById(R.id.filterChipGroup)
        NFCDataType.values().forEach { type ->
            val chip = Chip(this).apply {
                text = type.name
                isCheckable = true
                setOnCheckedChangeListener { _, isChecked ->
                    viewModel.updateFilter(type, isChecked)
                }
            }
            filterChipGroup.addView(chip)
        }
    }

    private fun setupObservers() {
        viewModel.filteredTags.observe(this) { tags ->
            nfcHistoryAdapter.submitList(tags)
        }
    }
}
