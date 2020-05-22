/*
 * MIT License
 *
 * Copyright (c) 2020. TechnoWolf FOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package `in`.technowolf.nyx.ui.encryption

import `in`.technowolf.nyx.R
import `in`.technowolf.nyx.databinding.FragmentEncryptionBinding
import `in`.technowolf.nyx.utils.ImageHelper
import `in`.technowolf.nyx.utils.viewBinding
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import java.io.FileDescriptor
import java.io.IOException


class EncryptionFragment : Fragment(R.layout.fragment_encryption) {

    private val binding by viewBinding(FragmentEncryptionBinding::bind)

    private val encryptionViewModel: EncryptionViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init() {
        attachObserver()
        setupFab()
    }

    private fun setupFab() {

        binding.appCompatImageView.setOnClickListener {
            startActivityForResult(
                ImageHelper.prepareImagePickerIntent(),
                ImageHelper.IMAGE_PICKER_INTENT
            )
        }

        binding.fabEncryptImage.setOnClickListener {
            binding.apply {
                if (isInputValid()) {
                    encryptionViewModel.encryptText(
                        etMessage.text.toString(),
                        etEncryptionKey.text.toString()
                    )
                }
            }
        }
    }

    private fun attachObserver() {
        observeEncryption()
        observeEncryptedImages()
    }

    private fun observeEncryption() {
        encryptionViewModel.encryptedMessage.observe(viewLifecycleOwner, Observer {
            prepareImageEncryption(it)
        })
    }

    private fun observeEncryptedImages() {
        encryptionViewModel.encryptedImages.observe(viewLifecycleOwner, Observer {
            // save file to internal storage
        })
    }

    private fun prepareImageEncryption(encryptedMessage: String?) {
        if (encryptedMessage != null) encryptImage(encryptedMessage)
        else Log.e("TAG", "Encrypted Message is null is empty")
    }

    private fun encryptImage(encryptedMessage: String) {
        encryptionViewModel.prepareEncryptedImage(encryptedMessage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ImageHelper.IMAGE_PICKER_INTENT -> {
                getBitmapFromUri(data?.data)?.let {
                    encryptionViewModel.imageForEncryption = it
                    binding.appCompatImageView.setImageBitmap(it)
                }
            }
        }
    }

    private fun isInputValid(): Boolean {

        if (binding.etEncryptionKey.text.toString().isEmpty()
            || binding.etEncryptionKey.text.toString().isBlank()
        ) {
            binding.tilEncryptionKey.error = "Encryption key is a required field."
            return false
        }

        if (binding.etMessage.text.toString().isEmpty()
            || binding.etMessage.text.toString().isBlank()
        ) {
            binding.tilMessage.error = "Message is a required field."
            return false
        }

        return true
    }

    @Throws(IOException::class, IllegalStateException::class)
    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        if (uri != null) {
            val parcelFileDescriptor: ParcelFileDescriptor? =
                requireContext().contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor: FileDescriptor? = parcelFileDescriptor?.fileDescriptor
            val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
            return image
        } else {
            throw IllegalStateException("URI is null!")
        }
    }

}
