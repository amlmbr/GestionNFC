package ma.ensa.projet

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.nfc.*
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import java.io.IOException
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import ma.ensa.projet.beans.NFCDataType
import ma.ensa.projet.beans.NFCTag
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "NFCDemo"
        private const val MIME_TYPE = "application/ma.ensa.projet"
        private val PDF_REQUEST_CODE = 1001
    }

    private lateinit var pdfNameTextView: TextView
    private lateinit var viewPdfButton: Button
    private lateinit var selectAnotherPdfButton: Button

    private var selectedPdfUri: Uri? = null

    private lateinit var messageInput: EditText
    private lateinit var writeButton: Button
    private lateinit var readTextView: TextView
    private lateinit var dataTypeSpinner: Spinner
    private lateinit var statusTextView: TextView
    private lateinit var p2pButton: Button


    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var nfcFilters: Array<IntentFilter>
    private lateinit var techListsArray: Array<Array<String>>
    private var detectedTag: Tag? = null


    private var messageToWrite: String = ""
    private var dataTypes = arrayOf("Text", "URL", "Contact", "PDF")
    private var currentDataType: NFCDataType = NFCDataType.TEXT

    private var isWriteMode = false
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

// Hide the default title since we added our custom TextView for the title
        supportActionBar?.setDisplayShowTitleEnabled(false)

// Setup drawer icon
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)

        }

        // Setup DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Setup NavigationView listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_history -> {
                    startActivity(Intent(this, NFCHistoryActivity::class.java))
                    drawerLayout.closeDrawers()
                    true
                }
                R.id.nav_exit -> {
                    finish()
                    true
                }
                else -> false
            }
        }

        // Initialize other views and setup
        initializeViews()
        setupNfcAdapter()
        setupDataTypeSpinner()
        setupClickListeners()
        handleP2PAvailability()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            R.id.action_save -> {
                // gestion de suvgarde
                writeButton.performClick()
                true
            }
            R.id.action_history -> {
                startActivity(Intent(this, NFCHistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    private fun initializeViews() {
        messageInput = findViewById(R.id.messageInput)
        writeButton = findViewById(R.id.writeButton)
        readTextView = findViewById(R.id.readTextView)
        dataTypeSpinner = findViewById(R.id.dataTypeSpinner)
        statusTextView = findViewById(R.id.statusTextView)
        p2pButton = findViewById(R.id.p2pButton)
        pdfNameTextView = findViewById(R.id.pdfNameTextView)
        viewPdfButton = findViewById(R.id.viewPdfButton)
        selectAnotherPdfButton = findViewById(R.id.selectAnotherPdfButton)
        pdfNameTextView.visibility = View.GONE
        viewPdfButton.visibility = View.GONE
        selectAnotherPdfButton.visibility = View.GONE
    }

    private fun handleP2PAvailability() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            p2pButton.visibility = View.GONE
        } else {
            p2pButton.visibility = View.VISIBLE
        }
    }

    private fun setupNfcAdapter() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            showMessage("This device doesn't support NFC.")
            finish()
            return
        }

        // Create PendingIntent with appropriate flags based on SDK version
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE
        } else {
            0
        }

        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            flags
        )

        val tagDetected = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val ndefDetected = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        ndefDetected.addDataType("*/*")

        nfcFilters = arrayOf(tagDetected, ndefDetected)
        techListsArray = arrayOf(arrayOf(Ndef::class.java.name))
    }

    private fun setupDataTypeSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dataTypeSpinner.adapter = adapter

        // Ajout du listener pour le Spinner
        dataTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentDataType = when (dataTypes[position]) {
                    "Text" -> NFCDataType.TEXT
                    "URL" -> NFCDataType.URL
                    "Contact" -> NFCDataType.CONTACT
                    "PDF" -> NFCDataType.PDF
                    else -> NFCDataType.TEXT
                }

                // Mettre à jour l'interface en fonction du type sélectionné
                updateUIForSelectedType(currentDataType)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                currentDataType = NFCDataType.TEXT
            }
        }

    }
    private fun updateUIForSelectedType(type: NFCDataType) {
        when (type) {
            NFCDataType.PDF -> {
                messageInput.visibility = View.GONE
                pdfNameTextView.visibility = View.VISIBLE
                viewPdfButton.visibility = View.VISIBLE
                selectAnotherPdfButton.visibility = View.VISIBLE
            }
            else -> {
                messageInput.visibility = View.VISIBLE
                pdfNameTextView.visibility = View.GONE
                viewPdfButton.visibility = View.GONE
                selectAnotherPdfButton.visibility = View.GONE

                // Mettre à jour le hint du messageInput
                messageInput.hint = when (type) {
                    NFCDataType.TEXT -> "Enter text"
                    NFCDataType.URL -> "Enter URL"
                    NFCDataType.CONTACT -> "Enter contact info"
                    else -> "Enter text"
                }
            }
        }
    }


    private fun setupClickListeners() {
        writeButton.setOnClickListener {
            messageToWrite = messageInput.text.toString()
            if (messageToWrite.isNotEmpty()) {
                if (dataTypeSpinner.selectedItem == "PDF") {
                    openFileChooser()
                } else {
                    isWriteMode = true
                    showMessage("Place an NFC tag to write")
                }
            } else {
                showMessage("Please enter a message to write")
            }
        }

        viewPdfButton.setOnClickListener {
            selectedPdfUri?.let { uri ->
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(uri, "application/pdf")
                startActivity(intent)
            } ?: showMessage("No PDF selected.")
        }

        selectAnotherPdfButton.setOnClickListener {
            openFileChooser()
        }

        p2pButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                showMessage("P2P/Android Beam is not supported on Android 11 and above")
            } else {
                showMessage("P2P functionality is no longer supported")
            }
        }
    }




    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                // Set the selected PDF URI
                selectedPdfUri = uri

                // Display the selected PDF name
                val pdfName = uri.lastPathSegment?.substringAfterLast("/") ?: "PDF"
                pdfNameTextView.text = pdfName
                pdfNameTextView.visibility = View.VISIBLE
                viewPdfButton.visibility = View.VISIBLE
                selectAnotherPdfButton.visibility = View.VISIBLE

                // Read the PDF content if needed and prepare it for writing
                val pdfContent = readFileContent(uri)
                messageToWrite = pdfContent
                isWriteMode = true
                showMessage("Selected PDF: $pdfName. Place an NFC tag to write.")
            }
        }
    }

    private fun readFileContent(uri: Uri): String {
        // Here, read the PDF file and return the content as a String (or process accordingly)
        return uri.toString() // For simplicity, replace with your actual PDF processing logic
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            pendingIntent,
            nfcFilters,
            techListsArray
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_TECH_DISCOVERED == intent.action ||
            NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action
        ) {
            detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            handleNfcIntent(intent)
        }
    }


    private fun handleNfcIntent(intent: Intent) {
        if (isWriteMode && detectedTag != null) {
            writeToTag()
        } else {
            readFromTag(intent)
        }
    }

    private fun writeToTag() {
        try {
            val ndefMessage = createNdefMessage()
            if (ndefMessage.records.isEmpty()) {
                showMessage("No data to write. Please select a message or PDF.")
                return
            }

            val ndef = Ndef.get(detectedTag!!)
            ndef?.let {
                it.connect()
                if (it.isWritable) {
                    it.writeNdefMessage(ndefMessage)

                    // Create NFCTag object with the current type
                    val nfcTag = NFCTag(
                        uuid = UUID.randomUUID().toString(),
                        type = currentDataType, // Using the currently selected type
                        content = messageToWrite,
                        timestamp = Date()
                    )

                    // Add to history
                    addTagToHistory(nfcTag)

                    showMessage("Successfully wrote data to NFC tag")
                } else {
                    showMessage("NFC tag is read-only.")
                }
                it.close()
            } ?: run {
                val ndefFormatable = NdefFormatable.get(detectedTag)
                ndefFormatable?.let { formatable ->
                    formatable.connect()
                    formatable.format(ndefMessage)
                    formatable.close()
                    showMessage("Tag formatted and written successfully")
                } ?: showMessage("Tag doesn't support NDEF")
            }
        } catch (e: Exception) {
            showMessage("Error writing to NFC tag: ${e.message}")
            Log.e(TAG, "Error writing to tag", e)
        }
    }



    private fun readFromTag(intent: Intent) {
        val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

        if (rawMessages != null) {
            val messages = rawMessages.map { it as NdefMessage }
            val processedContent = processNdefMessages(messages)

            // Set the content directly to readTextView with type information
            readTextView.text = processedContent

            // Get the currently selected data type
            val tagType = when (dataTypeSpinner.selectedItem.toString()) {
                "Text" -> NFCDataType.TEXT
                "URL" -> NFCDataType.URL
                "Contact" -> NFCDataType.CONTACT
                "PDF" -> NFCDataType.PDF
                else -> NFCDataType.TEXT
            }

            // Check for PDF content
            val pdfRecord = messages.firstOrNull()?.records?.firstOrNull {
                Arrays.equals(it.type, "application/pdf".toByteArray(Charset.forName("UTF-8")))
            }

            if (pdfRecord != null) {
                val pdfData = pdfRecord.payload
                try {
                    val pdfUri = writeToTempFile(pdfData, "temp.pdf")
                    pdfNameTextView.text = "Read PDF from tag"
                    pdfNameTextView.visibility = View.VISIBLE
                    viewPdfButton.visibility = View.VISIBLE
                    selectAnotherPdfButton.visibility = View.GONE
                    selectedPdfUri = pdfUri
                } catch (e: Exception) {
                    showMessage("Error handling PDF: ${e.message}")
                }
            }

            // Create and save the tag to history
            val nfcTag = NFCTag(
                uuid = UUID.randomUUID().toString(),
                type = tagType,
                content = processedContent,
                timestamp = Date()
            )
            addTagToHistory(nfcTag)

            showMessage("Tag read successfully")
        } else {
            showMessage("No NDEF messages found on tag")
            readTextView.text = "No content found on tag"
        }
    }

    private fun writeToTempFile(data: ByteArray, fileName: String): Uri {
        val tempFile = File(cacheDir, fileName)
        tempFile.writeBytes(data)
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", tempFile) // Use FileProvider
    }



    private fun processNdefMessages(messages: List<NdefMessage>): String {
        val builder = StringBuilder()
        messages.forEach { message ->
            message.records.forEach { record ->
                when (record.tnf) {
                    NdefRecord.TNF_WELL_KNOWN -> {
                        when {
                            Arrays.equals(record.type, NdefRecord.RTD_TEXT) -> {
                                val textData = String(record.payload, 1, record.payload.size - 1,
                                    Charset.forName("UTF-8"))
                                builder.append("Text: ").append(textData)
                            }
                            Arrays.equals(record.type, NdefRecord.RTD_URI) -> {
                                val uriData = String(record.payload, 1, record.payload.size - 1,
                                    Charset.forName("UTF-8"))
                                builder.append("URL: ").append(uriData)
                            }
                        }
                    }
                    NdefRecord.TNF_MIME_MEDIA -> {
                        val mimeType = String(record.type)
                        if (mimeType.startsWith("text/vcard")) {
                            val data = String(record.payload, Charset.forName("UTF-8"))
                            builder.append("Contact: ").append(data)
                        } else if (mimeType.startsWith("application/pdf")) {
                            builder.append("PDF File detected")
                        } else {
                            val data = String(record.payload, Charset.forName("UTF-8"))
                            builder.append("Data: ").append(data)
                        }
                    }
                    else -> {
                        try {
                            val data = String(record.payload, Charset.forName("UTF-8"))
                            builder.append("Other: ").append(data)
                        } catch (e: Exception) {
                            builder.append("[Unsupported Content]")
                        }
                    }
                }
            }
        }
        return builder.toString()
    }

    private fun addTagToHistory(tag: NFCTag) {
        showMessage("Tag added to history. Redirecting...")


        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, NFCHistoryActivity::class.java)
            intent.putExtra("NFC_TAG", tag)
            startActivity(intent)
        }, 3000) // Délai de 2000 ms (2 secondes)
    }


    private fun processWellKnownRecord(record: NdefRecord, builder: StringBuilder) {
        when {
            Arrays.equals(record.type, NdefRecord.RTD_TEXT) -> {
                val textData = String(record.payload, 1, record.payload.size - 1)
                builder.append("Text: $textData\n")
            }
            Arrays.equals(record.type, NdefRecord.RTD_URI) -> {
                val uriData = String(record.payload, 1, record.payload.size - 1)
                builder.append("URI: $uriData\n")
            }
        }
    }
    private fun processFileRecord(record: NdefRecord, builder: StringBuilder) {
        val mimeType = String(record.type)
        if (mimeType.startsWith("application/pdf")) {
            builder.append("PDF File detected\n")
            // Additional handling for PDF files if needed
        }
    }

    private fun processMimeRecord(record: NdefRecord, builder: StringBuilder) {
        val mimeType = String(record.type)
        when {
            mimeType.startsWith("application/pdf") -> processFileRecord(record, builder)
            mimeType.startsWith("image/") -> processFileRecord(record, builder)
            else -> {
                val data = String(record.payload)
                builder.append("MIME [$mimeType]: $data\n")
            }
        }
    }

    private fun createNdefMessage(): NdefMessage {
        val record = when (currentDataType) {
            NFCDataType.PDF -> {
                if (selectedPdfUri == null) {
                    showMessage("Please select a PDF first")
                    return NdefMessage(arrayOf())
                }
                createFileRecord(selectedPdfUri)
            }
            NFCDataType.TEXT -> createTextRecord(messageToWrite)
            NFCDataType.URL -> createUrlRecord(messageToWrite)
            NFCDataType.CONTACT -> createContactRecord(messageToWrite)
        }

        return NdefMessage(arrayOf(record))
    }






    private fun createFileRecord(uri: Uri?): NdefRecord {
        // Read PDF file as byte array
        val fileData = readPdfFile(uri)
        return NdefRecord.createMime(
            "application/pdf", // MIME type for PDF
            fileData
        )
    }
    private fun readPdfFile(uri: Uri?): ByteArray {
        uri?.let {
            contentResolver.openInputStream(it).use { inputStream ->
                if (inputStream != null) {
                    return inputStream.readBytes() // Ensure that this is returning a non-empty byte array
                }
            }
        }
        return ByteArray(0) // Return empty array if URI is null or read fails
    }



    private fun createTextRecord(text: String): NdefRecord {
        val langBytes = Locale.getDefault().language.toByteArray(Charset.forName("US-ASCII"))
        val textBytes = text.toByteArray(Charset.forName("UTF-8"))
        val payload = ByteArray(1 + langBytes.size + textBytes.size)

        payload[0] = langBytes.size.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + langBytes.size, textBytes.size)

        return NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, ByteArray(0), payload)
    }

    private fun createUrlRecord(urlStr: String): NdefRecord {
        return NdefRecord.createUri(urlStr)
    }

    private fun createContactRecord(contact: String): NdefRecord {
        val vcardMessage = """
            BEGIN:VCARD
            VERSION:3.0
            N:$contact
            END:VCARD
        """.trimIndent()
        return NdefRecord.createMime(
            "text/vcard",
            vcardMessage.toByteArray(Charset.forName("UTF-8"))
        )
    }

    private fun createCustomRecord(data: String): NdefRecord {
        val domain = "com.ensa.nfcproject"
        val type = "custom"
        return NdefRecord.createExternal(domain, type, data.toByteArray())
    }

    private fun writeNdefMessage(tag: Tag, message: NdefMessage) {
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(message)
                } else {
                    throw IOException("Tag is read-only")
                }
            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    ndefFormatable.connect()
                    ndefFormatable.format(message)
                } else {
                    throw IOException("Tag doesn't support NDEF")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to write NDEF message", e)
            throw e
        }
    }

    private fun encryptData(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return Base64.encodeToString(hash, Base64.DEFAULT)
    }

    private fun showMessage(message: String) {
        val snackbar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)

        // Customize Snackbar appearance and animation
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(ContextCompat.getColor(this, R.color.your_snackbar_background_color)) // Set your desired background color

        // Animate appearance
        snackbarView.alpha = 0f // Start invisible
        snackbarView.animate().alpha(1f).setDuration(300).start() // Fade in animation

        snackbar.show()
        statusTextView.text = message
    }




}