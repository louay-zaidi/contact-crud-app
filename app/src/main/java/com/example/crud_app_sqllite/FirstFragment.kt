package com.example.crud_app_sqllite

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.navigation.fragment.findNavController
import com.example.crud_app_sqllite.databinding.FragmentFirstBinding
import androidx.core.os.bundleOf


class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DBHelper



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listView = binding.listView

        dbHelper = DBHelper(requireContext())
        registerForContextMenu(listView)

        // Load and display contacts when the fragment is created
        displayContacts()

        binding.floatingActionButton2.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun displayContacts() {
        val db = dbHelper.readableDatabase
        val projection = arrayOf(
            DBHelper.COLUMN_ID,
            DBHelper.COLUMN_NAME,
            DBHelper.COLUMN_EMAIL,
            DBHelper.COLUMN_AGE,
            DBHelper.COLUMN_PHONE_NUMBER
        )



        val cursor = db.query(DBHelper.TABLE_NAME, projection, null, null, null, null, null)

        val contactList = mutableListOf<String>()
        while (cursor.moveToNext()) {
            val IdIndex = cursor.getColumnIndex(DBHelper.COLUMN_ID)
            val nameIndex = cursor.getColumnIndex(DBHelper.COLUMN_NAME)
            val emailIndex = cursor.getColumnIndex(DBHelper.COLUMN_EMAIL)
            val ageIndex = cursor.getColumnIndex(DBHelper.COLUMN_AGE)
            val phoneIndex = cursor.getColumnIndex(DBHelper.COLUMN_PHONE_NUMBER)

            // Check if the column exists in the cursor before extracting the value
            if (nameIndex >= 0) {
                val id = cursor.getString((IdIndex))
                val name = cursor.getString(nameIndex)
                val email = if (emailIndex >= 0) cursor.getString(emailIndex) else ""
                val age = if (ageIndex >= 0) cursor.getInt(ageIndex) else 0
                val phoneNumber = if (phoneIndex >= 0) cursor.getString(phoneIndex) else ""

                val contactInfo = "Id: $id\nName: $name\nEmail: $email\nAge: $age\nPhone: $phoneNumber\n\n"
                contactList.add(contactInfo)
            }
        }

        cursor.close()
        db.close()

        val adapter = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_list_item_1,
            contactList
        )
        binding.listView.adapter = adapter
        binding.listView.setOnItemClickListener { _, _, position, _ ->
            val selectedContactInfo = contactList[position]
            val contactInfoArray = selectedContactInfo.split("\n")

            // Extracting information from the selected contact
            val id = contactInfoArray[0].substring(4) // Updated to remove "Id: "
            val name = contactInfoArray[1].substring(6) // Remove "Name: "
            val email = contactInfoArray[2].substring(7) // Remove "Email: "
            val age = contactInfoArray[3].substring(5).toInt() // Remove "Age: " and convert to Int
            val phoneNumber = contactInfoArray[4].substring(7) // Remove "Phone: "

            Log.d("FirstFragment", "Navigating to SecondFragment with id: $id") // Add this log statement
            // Navigate to SecondFragment using the action ID
            val actionId = R.id.action_FirstFragment_to_SecondFragment
            val bundle = bundleOf(
                "id" to id,
                "name" to name,
                "email" to email,
                "age" to age,
                "phoneNumber" to phoneNumber
            )
            findNavController().navigate(actionId, bundle)
        }

    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.menu_main, menu)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
