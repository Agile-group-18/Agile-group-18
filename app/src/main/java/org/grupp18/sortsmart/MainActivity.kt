package org.grupp18.sortsmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import org.grupp18.sortsmart.ui.theme.SortSmartTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SortSmartTheme {
                SortSmartApp()
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun SortSmartApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    // Removed NavigationSuiteScaffold because you have a custom BottomBar now!
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Header() // The custom header you added earlier
        },
        bottomBar = {
            CustomBottomBar() // Your new custom bottom navigation!
        }
    ) { innerPadding ->

        // Switch the screen based on the currently selected destination
        // (You will need to hook up the clicks in CustomBottomBar to change currentDestination later)
        Box(modifier = Modifier.padding(innerPadding)) {
            Greeting(
                name = "Android",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SortSmartTheme {
        Greeting("Android")
    }
}