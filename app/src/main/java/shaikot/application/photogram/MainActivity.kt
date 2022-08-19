package shaikot.application.photogram

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import shaikot.application.photogram.databinding.ActivityMainBinding
import shaikot.application.photogram.fragments.HomeFragment
import shaikot.application.photogram.fragments.NotificationFragment
import shaikot.application.photogram.fragments.ProfileFragment
import shaikot.application.photogram.fragments.SearchFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_view,HomeFragment()).commit()

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                }
                R.id.navigation_search -> {
                    loadFragment(SearchFragment())
                }
                R.id.navigation_add -> {
                    it.isChecked = false
                    startActivity(Intent(this, AddpostActivity::class.java))
                }
                R.id.navigation_notifications -> {
                    loadFragment(NotificationFragment())
                }
                R.id.navigation_profile -> {
                    loadFragment(ProfileFragment())
                }
            }

            false
        }

    }



    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_view,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


}







