package com.example.projetocm_g11.ui.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.projetocm_g11.ui.utils.NavigationManager
import com.example.projetocm_g11.ui.listeners.OnNavigateToFragment
import com.example.projetocm_g11.R
import com.example.projetocm_g11.data.local.entities.ParkingLot
import com.example.projetocm_g11.ui.fragments.ContactsFragment
import com.example.projetocm_g11.ui.fragments.ParkingLotsListFragment
import com.example.projetocm_g11.ui.fragments.SettingsFragment
import com.example.projetocm_g11.ui.fragments.VehiclesListFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

const val EXTRA_THEME = "com.example.projetocm_g11.ui.activities.THEME"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnNavigateToFragment {

    private val TAG = MainActivity::class.java.simpleName

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        // Navigate to selected fragment
        when(item.itemId) {

            R.id.nav_parkings_lots -> NavigationManager.goToFragment(supportFragmentManager,
                ParkingLotsListFragment()
            )

            R.id.nav_vehicles -> NavigationManager.goToFragment(supportFragmentManager,
                VehiclesListFragment()
            )

            R.id.nav_contacts -> NavigationManager.goToFragment(supportFragmentManager,
                ContactsFragment()
            )

            R.id.nav_settings -> NavigationManager.goToFragment(supportFragmentManager,
                SettingsFragment()
            )

            R.id.nav_quit -> finish()
        }

        // Close drawer
        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    override fun onBackPressed() {

        when {
            /* If drawer is open, close drawer */
            drawer.isDrawerOpen(GravityCompat.START) ->
                drawer.closeDrawer(GravityCompat.START)

            /* If there is only one Fragment on the stack, finish */
            supportFragmentManager.backStackEntryCount == 1 ->
                finish()

            else -> super.onBackPressed()
        }
    }

    private fun setupDrawerMenu() {

        // Button to open/close drawer
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close)

        // Subscribe activity to interface (navigates to fragment)
        nav_drawer.setNavigationItemSelectedListener(this)

        // Subscribe toggle button to drawer interface (toggles state)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun screenRotated(savedInstanceState: Bundle?): Boolean {

        return savedInstanceState != null
    }

    private fun launchThemeThread() {

        val dateFormatter = SimpleDateFormat("HH:mm")

        val morningDate = dateFormatter.parse("08:00")
        val afternoonDate = dateFormatter.parse("16:00")

        /* Checks every 5 minutes */
        val millisToSleep: Long = 1000 * 20 * 5

        CoroutineScope(Dispatchers.Default).launch {

            while(true) {

                /* Get last theme from shared preferences. If no ID was found, set ID to LightTheme*/
                val currentThemeID = getCurrentThemeID()

                Log.i(TAG, "Validating theme according to current time")

                /* Check what time it is */
                val currentHour = dateFormatter.format(Date())

                Log.i(TAG, "Current time is: $currentHour")

                val currentDate = dateFormatter.parse(currentHour)

                if(currentThemeID == R.style.DarkTheme) {

                    Log.i(TAG, "Current theme is DarkTheme")

                    if (currentDate.after(morningDate) && currentDate.before(afternoonDate)) {

                        /* App should be in LightTheme */

                        Log.i(TAG, "Switching to LightTheme")

                        changeCurrentTheme(R.style.LightTheme)

                        /* Terminate coroutine */
                        break
                    }

                    else { Log.i(TAG, "Staying in DarkTheme") }

                }

                else if(currentThemeID == R.style.LightTheme) {

                    Log.i(TAG, "Current theme is LightTheme")

                    if (currentDate.before(morningDate) || currentDate.after(afternoonDate)) {

                        /* App should be in DarkTheme */

                        Log.i(TAG, "Switching to DarkTheme")

                        changeCurrentTheme(R.style.DarkTheme)

                        /* Terminate coroutine */
                        break
                    }

                    else { Log.i(TAG, "Staying in LightTheme") }
                }

                /* Wait and repeat */
                delay(millisToSleep)
            }
        }
    }

    private fun getCurrentThemeID(): Int {

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: return R.style.LightTheme

        return sharedPreferences.getInt(EXTRA_THEME, R.style.LightTheme)
    }

    private suspend fun changeCurrentTheme(id: Int) {

        val sharedPreferences = getPreferences(Context.MODE_PRIVATE) ?: return

        withContext(Dispatchers.Main) {

            /* Save current theme ID */
            sharedPreferences.edit().putInt(EXTRA_THEME, id).apply()

            //setTheme(id)

            /* Refresh activity? */
            finish()
            startActivity(intent)
        }
    }

    private fun initListFragment() {

        val data = intent?.getParcelableArrayListExtra<ParkingLot>(EXTRA_PARKING_LOTS)
        val updated = intent?.getBooleanExtra(EXTRA_UPDATED, false)

        val args = Bundle()

        args.putParcelableArrayList(EXTRA_PARKING_LOTS, data)

        if (updated != null) {
            args.putBoolean(EXTRA_UPDATED, updated)
        }

        val fragment = ParkingLotsListFragment()
        fragment.arguments = args

        // Navigate to list fragment
        NavigationManager.goToFragment(supportFragmentManager,
            fragment
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val themeID = getCurrentThemeID()
        setTheme(themeID)

        setContentView(R.layout.activity_main)

        // Provides drawer access within toolbar
        setSupportActionBar(toolbar)

        // Removes title from toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupDrawerMenu()

        if(!screenRotated(savedInstanceState)) {

            /* Creates and starts ParkingLotsListFragment with previously fetched data */
            initListFragment()
        }
    }

    override fun onStart() {

        /* Launches thread that checks time. When time is between 18:00-8:00, sets App theme to DarkTheme.
            * When time is between 8:00-18:00, sets App Theme to Light Theme */
        launchThemeThread()

        super.onStart()
    }

    override fun onNavigateToFragment(fragment: Fragment?) {

        fragment?.let { NavigationManager.goToFragment(supportFragmentManager, it) }
    }
}
