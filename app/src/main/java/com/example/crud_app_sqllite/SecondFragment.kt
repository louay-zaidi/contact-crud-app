package com.example.crud_app_sqllite

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.example.crud_app_sqllite.databinding.FragmentSecondBinding
// author louay zaidi
class SecondFragment : Fragment() {


    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private var isEditMode = false
    private lateinit var dbHelper: DBHelper
    private var contactId: Long = -1 //a variable to store the contact ID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true) // Enabling  options menu for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbHelper = DBHelper(requireContext())

        binding.floatingActionButton1.show()
        binding.floatingActionButton1.setOnClickListener {
            // Save or update contact to the database
            saveOrUpdateContact()
        }

        // Retrieve arguments if they are passed
        contactId = arguments?.getString("id", "")?.toLongOrNull() ?: -1
        val name = arguments?.getString("name", "")
        val email = arguments?.getString("email", "")
        val age = arguments?.getInt("age", 0) ?: 0
        val phoneNumber = arguments?.getString("phoneNumber", "")

        // Check if the fragment is in edit mode
        if (name != null) {
            isEditMode = name.isNotEmpty()
        }

        // Set the values to the EditText fields
        binding.fullnameEditText.setText(name)
        binding.emailEditText.setText(email)
        binding.ageEditText.setText(age.toString())
        binding.phoneNumberEditText.setText(phoneNumber)


        // Dynamically set the toolbar title based on isEditMode
        val toolbar = (requireActivity() as AppCompatActivity).supportActionBar
        if (isEditMode) {
            toolbar?.title = "Edit Contact"

        } else {
            toolbar?.title = "Add Contact"

        }

        if (isEditMode) {
            // making the editTexts and the Floating Action button not editable
            binding.fullnameEditText.isFocusable = false
            binding.fullnameEditText.isFocusableInTouchMode = false
            binding.emailEditText.isFocusable = false
            binding.emailEditText.isFocusableInTouchMode = false
            binding.ageEditText.isFocusable = false
            binding.ageEditText.isFocusableInTouchMode = false
            binding.phoneNumberEditText.isFocusable = false
            binding.phoneNumberEditText.isFocusableInTouchMode = false
            binding.floatingActionButton1.isEnabled = false
            // Set the title indicating edit mode
            requireActivity().title = "Edit Contact"
        } else {
            // Set the title indicating add mode
            requireActivity().title = "Add Contact"
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)

        // Hide the menu items by default
        menu.findItem(R.id.action_settings1).isVisible = false
        menu.findItem(R.id.action_settings2).isVisible = false

        // Show the items only when in edit mode
        if (isEditMode) {
            menu.findItem(R.id.action_settings1).isVisible = true
            menu.findItem(R.id.action_settings2).isVisible = true
        }
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings1 -> {

                binding.floatingActionButton1.show()
                // Enable editing mode
                enableEditMode()

                return true
            }

            R.id.action_settings2 -> {
                // Get the contact ID from your fragment arguments or ViewModel
                val contactId = arguments?.getString("id")?.toLongOrNull() ?: -1

                // Check if contactId is valid
                if (contactId != -1L) {
                    // Delete the selected contact from the database
                    deleteContact(contactId)

                    // Navigate back to the first fragment
                    findNavController().navigateUp()
                    return true
                } else {
                    Log.e("SecondFragment", "Invalid contactId for delete operation")
                }
            }

            else -> return super.onOptionsItemSelected(item)
        }
        return false
    }

    private fun enableEditMode() {
        // Enable editing for all EditText fields
        binding.fullnameEditText.isEnabled = true
        binding.emailEditText.isEnabled = true
        binding.ageEditText.isEnabled = true
        binding.phoneNumberEditText.isEnabled = true

        // Set focusable and focusable in touch mode for EditText fields
        binding.fullnameEditText.isFocusable = true
        binding.fullnameEditText.isFocusableInTouchMode = true
        binding.emailEditText.isFocusable = true
        binding.emailEditText.isFocusableInTouchMode = true
        binding.ageEditText.isFocusable = true
        binding.ageEditText.isFocusableInTouchMode = true
        binding.phoneNumberEditText.isFocusable = true
        binding.phoneNumberEditText.isFocusableInTouchMode = true
        // Enable FloatingActionButton in edit mode
        binding.floatingActionButton1.isEnabled = true
    }


    private fun saveOrUpdateContact() {
        val name = binding.fullnameEditText.text.toString()
        val email = binding.emailEditText.text.toString()
        val ageText = binding.ageEditText.text.toString()
        val phoneNumber = binding.phoneNumberEditText.text.toString()

        // Check if any of the required fields are empty
        if (name.isBlank() || email.isBlank() || ageText.isBlank() || phoneNumber.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in all the fields", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val age = ageText.toIntOrNull() ?: 0

        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DBHelper.COLUMN_NAME, name)
            put(DBHelper.COLUMN_EMAIL, email)
            put(DBHelper.COLUMN_AGE, age)
            put(DBHelper.COLUMN_PHONE_NUMBER, phoneNumber)
        }

        if (isEditMode && contactId != -1L) {
            // Update the contact if in edit mode
            val updatedRows = db.update(
                DBHelper.TABLE_NAME,
                values,
                "${DBHelper.COLUMN_ID}=?",
                arrayOf(contactId.toString())
            )

            if (updatedRows > 0) {
                Toast.makeText(
                    requireContext(),
                    "Contact updated successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // clear the input fields after updating
                clearInputFields()
            } else {

                Toast.makeText(
                    requireContext(),
                    "Failed to update contact",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            // Insert a new contact if not in edit mode
            val newContactId = db.insert(DBHelper.TABLE_NAME, null, values)

            if (newContactId != -1L) {
                Toast.makeText(
                    requireContext(),
                    "Contact added successfully",
                    Toast.LENGTH_SHORT
                ).show()

                // Optionally, you can clear the input fields after inserting
                clearInputFields()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Failed to add contact",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        db.close()

        // Navigate back to the first fragment only if all required fields are filled
        if (name.isNotBlank() && email.isNotBlank() && ageText.isNotBlank() && phoneNumber.isNotBlank()) {
            findNavController().navigateUp()
        }
    }

    private fun clearInputFields() {
        binding.fullnameEditText.text.clear()
        binding.emailEditText.text.clear()
        binding.ageEditText.text.clear()
        binding.phoneNumberEditText.text.clear()
    }


    private fun deleteContact(contactId: Long) {
        val dbHelper = DBHelper(requireContext())
        val result = dbHelper.deleteContact(contactId)
        dbHelper.close()

        if (result != -1) {
            Toast.makeText(requireContext(), "Contact deleted successfully", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(requireContext(), "Failed to delete contact", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
