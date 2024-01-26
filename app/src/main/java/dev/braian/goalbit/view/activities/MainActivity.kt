package dev.braian.goalbit.view.activities


import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import dev.braian.goalbit.R
import dev.braian.goalbit.databinding.ActivityMainBinding
import dev.braian.goalbit.utils.NotificationHelper
import dev.braian.goalbit.view.fragment.HistoricFragment
import dev.braian.goalbit.view.fragment.HomeFragment
import dev.braian.goalbit.view.fragment.MeFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedItemId: Int = R.id.item_home
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        navigationView()
        displayFragment(HomeFragment())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        Thread.sleep(2000)
        installSplashScreen()
        notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.createNotificationChannel()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    private fun displayFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

    private fun navigationView() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.item_home -> {
                    displayFragment(HomeFragment())
                    selectedItemId = R.id.item_home
                    true
                }

                R.id.item_add -> {
                    startActivity(Intent(this, AddHabitFormActivity::class.java))
                    false
                }

                R.id.item_hystory -> {
                    displayFragment(HistoricFragment())
                    selectedItemId = R.id.item_hystory
                    true
                }

                R.id.item_me -> {
                    displayFragment(MeFragment())
                    selectedItemId = R.id.item_me
                    true
                }

                else -> false
            }
        }
    }
}

