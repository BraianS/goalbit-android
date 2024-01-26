package dev.braian.goalbit.view.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dev.braian.goalbit.R
import dev.braian.goalbit.constants.DataBaseConstants
import dev.braian.goalbit.databinding.FragmentMeBinding
import dev.braian.goalbit.model.UserModel
import dev.braian.goalbit.view.activities.MainActivity
import dev.braian.goalbit.view.viewholder.HabitViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [MeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MeFragment : Fragment() {

    private var _binding: FragmentMeBinding? = null
    private val binding get() = _binding!!

    private lateinit var loginProgressBar: ProgressBar
    private lateinit var buttonGoogleLogin: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var habitViewModel: HabitViewModel

    private lateinit var mGoogleSignInClient: GoogleSignInClient

    private lateinit var dialog: BottomSheetDialog

    private lateinit var someActivityResultLauncher: ActivityResultLauncher<Intent>


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        dialog = BottomSheetDialog(requireContext())
        _binding = FragmentMeBinding.inflate(inflater, container, false)

        habitViewModel = HabitViewModel()

        binding.buttonBackupAndSyncronized.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                dialog.show()
            }
        })

        createDialog()

     
        val url = auth.currentUser?.photoUrl

        if (auth.currentUser != null && !auth.currentUser!!.isAnonymous) {
            binding.buttonBackupAndSyncronized.visibility = View.INVISIBLE
            binding.cardViewUserInfo.visibility = View.VISIBLE

            binding.buttonLogout.setOnClickListener {

                auth.signOut()
                auth.currentUser == null

//                val homeFragment = HomeFragment()
//                val fragmentManager = requireActivity().supportFragmentManager
//
//                // Replace the current fragment with HomeFragment
//                fragmentManager.beginTransaction()
//                    .replace(R.id.frame_layout, homeFragment)
//                    .commit()

                var intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)

            }

            binding.textViewUserName.text =
              auth.currentUser?.displayName.toString()

            Glide.with(this)
                .load(url)
                .into(binding.imageUserProfile)

        } else {
            binding.buttonBackupAndSyncronized.visibility = View.VISIBLE
            binding.cardViewUserInfo.visibility = View.INVISIBLE
            binding.buttonLogout.visibility = View.INVISIBLE
        }


        // Initialize the ActivityResultLauncher
        someActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            object : ActivityResultCallback<ActivityResult?> {
                override fun onActivityResult(result: ActivityResult?) {
                    handleActivityResult(result)
                }
            }
        )

        return binding.root
    }

    private fun createDialog() {
        var view: View = layoutInflater.inflate(R.layout.botton_dialog_login, null, false)

        val imageGoogleLogin: ImageView = view.findViewById(R.id.image_google_login)

        dialog.setContentView(view)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail().build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        imageGoogleLogin.setOnClickListener {
            googleSignIn()
        }
    }

    private fun googleSignIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        someActivityResultLauncher.launch(signInIntent)
    }

    private fun handleActivityResult(result: ActivityResult?) {
        if (result?.resultCode == Activity.RESULT_OK) {
            val task: Task<GoogleSignInAccount> =
                GoogleSignIn.getSignedInAccountFromIntent(result.data)

            // Rest of your code
          auth.currentUser?.delete()

            Log.i(
                DataBaseConstants.TAG.LOGIN_FRAGMENT,
                "User name: ${auth.currentUser?.displayName} is anon: ${auth.currentUser?.isAnonymous}"
            )

            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                firebaseAuth(account.idToken)

                // Consider using requireActivity() instead of activity, depending on your use case
                requireActivity()

            } catch (e: ApiException) {
                Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Error no login", Toast.LENGTH_SHORT).show()
        }
    }


    private fun firebaseAuth(idToken: String?) {

        var anonymousId = ""

        if (auth.currentUser?.isAnonymous == true) {
            anonymousId = auth.currentUser!!.uid
        }

        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(object : OnCompleteListener<AuthResult> {

                override fun onComplete(task: Task<AuthResult>) {

                    if (task.isSuccessful) {
                        var user: FirebaseUser = auth.currentUser!!

                        habitViewModel.transferAnonymousHabitToGoogleAccount(anonymousId, user.uid)

                        Log.i(
                            DataBaseConstants.TAG.LOGIN_FRAGMENT,
                            "User name Firebase: ${auth.currentUser?.displayName} " +
                                    "is anon: ${auth.currentUser?.isAnonymous} \n id: ${user.uid}"
                        )

                        var userModel = UserModel()

                        userModel.id = user.uid
                        userModel.name = user.displayName.toString()
                        userModel.photoUrl = user.photoUrl.toString()
                        userModel.save()

                        var intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

            })
    }


}