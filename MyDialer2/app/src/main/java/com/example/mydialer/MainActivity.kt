package com.example.mydialer

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

data class Contact (
    val name: String,
    val phone: String,
    val type:String
)

class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    private val name: TextView = itemView.findViewById(R.id.textName)
    private val phone: TextView = itemView.findViewById(R.id.textPhone)
    private val type: TextView = itemView.findViewById(R.id.textType)

    fun bindTo(data: Contact) {
        name.text = data.name
        phone.text = data.phone
        type.text = data.type

        itemView.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + data.phone))
            ContextCompat.startActivity(itemView.context, intent, null)
        }
    }
}

class Adapter : ListAdapter<Contact, ViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rview_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = currentList[position]
        holder.bindTo(data)
    }
}

class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
    override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem == newItem
}
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.rView)
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)

        recyclerView.addItemDecoration(dividerItemDecoration)

        val adapter = Adapter()

        val url = URL("https://drive.google.com/u/0/uc?id=1-KO-9GA3NzSgIc1dkAsNm8Dqw0fuPxcR")
        Thread {
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            var data = ""
            var contacts: ArrayList<Contact>
            try {
                data = urlConnection.inputStream.bufferedReader().readText()
            }
            finally {
                contacts = Gson().fromJson(data, Array<Contact>::class.java).toList() as ArrayList<Contact>
            }
            urlConnection.disconnect()
            runOnUiThread() {
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.adapter = adapter
                adapter.submitList(contacts)
            }

            val editText = findViewById<EditText>(R.id.textView)
            editText.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(s: Editable) {

                }
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var contact = arrayListOf<Contact>() // Список для поиска контактов
                    val etText = editText.text.toString()

                    if (etText != "") {
                        contacts.forEach { qq ->
                            if (etText.contains(qq.name) || etText.contains(qq.phone) || etText.contains(qq.type)) {
                                contact.add(qq)
                            }
                        }

                        runOnUiThread() {
                            adapter.submitList(contact)
                        }
                    }
                    else {
                        runOnUiThread() {
                            adapter.submitList(contacts)
                        }
                    }
                }
            })
        }.start()
    }
}