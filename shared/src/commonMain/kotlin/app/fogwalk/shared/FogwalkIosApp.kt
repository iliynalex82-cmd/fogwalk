package app.fogwalk.shared

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.fogwalk.content.Content
import app.fogwalk.domain.Relics
import app.fogwalk.domain.Shop
import app.fogwalk.geo.Grid
import app.fogwalk.platform.LocalNotifier
import app.fogwalk.platform.LocationTracker
import app.fogwalk.platform.StepCounter
import app.fogwalk.platform.SystemSharer
import app.fogwalk.ui.map.FogMapView
import app.fogwalk.ui.map.MapThemeStyle
import app.fogwalk.ui.theme.FogwalkTheme

expect fun platformName(): String

private enum class Tab(val label: String) {
    MAP("Map"),
    QUESTS("Quests"),
    RELICS("Relics"),
    JOURNAL("Chronicle"),
    SHOP("Shop"),
    SETTINGS("Settings"),
}

@Composable
fun FogwalkIosApp() {
    var currentTab by remember { mutableStateOf(Tab.MAP) }
    var darkTheme by remember { mutableStateOf(false) }
    var latitude by remember { mutableStateOf(53.1959) }
    var longitude by remember { mutableStateOf(50.1002) }
    var revealedCells by remember { mutableStateOf(setOf(Grid.cellAt(53.1959, 50.1002))) }
    var steps by remember { mutableStateOf(0) }
    var showOnboarding by remember { mutableStateOf(false) }
    val content = remember { Content.of("en") }

    val tracker = remember { LocationTracker() }
    val stepCounter = remember { StepCounter() }

    DisposableEffect(Unit) {
        tracker.start()
        stepCounter.start { steps = it }
        onDispose {
            tracker.stop()
            stepCounter.stop()
        }
    }

    LaunchedEffect(tracker) {
        tracker.locations.collect { point ->
            latitude = point.latitude
            longitude = point.longitude
            val cell = Grid.cellAt(point.latitude, point.longitude)
            if (cell !in revealedCells) {
                revealedCells = revealedCells + cell
            }
        }
    }

    FogwalkTheme(darkTheme = darkTheme) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    Tab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = { Text(tab.label.take(2)) },
                            label = { Text(tab.label) },
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (currentTab) {
                    Tab.MAP -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            FogMapView(
                                modifier = Modifier.fillMaxSize(),
                                latitude = latitude,
                                longitude = longitude,
                                revealedCells = revealedCells,
                                themeStyle = if (darkTheme) MapThemeStyle.NIGHT else MapThemeStyle.DAY_SEPIA,
                            )
                            Card(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Fogwalk (${platformName()})", style = MaterialTheme.typography.titleMedium)
                                    Text("Cells: ${revealedCells.size} • Steps: $steps")
                                }
                            }
                        }
                    }

                    Tab.QUESTS -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item { Text("Quests", style = MaterialTheme.typography.headlineMedium) }
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Daily Walk", style = MaterialTheme.typography.titleMedium)
                                        Text("Explore new fog cells around you.")
                                    }
                                }
                            }
                        }
                    }

                    Tab.RELICS -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item { Text("Treasury", style = MaterialTheme.typography.headlineMedium) }
                            items(Relics.all.take(12)) { relic ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(content.relicName(relic.id), style = MaterialTheme.typography.titleMedium)
                                        Text(content.relicLore(relic.id), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    Tab.JOURNAL -> {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            Text("Chronicle", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.height(12.dp))
                            Text("Explored cells: ${revealedCells.size}")
                            Text("Steps today: $steps")
                        }
                    }

                    Tab.SHOP -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            item { Text("Shop", style = MaterialTheme.typography.headlineMedium) }
                            items(Shop.styles) { style ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(content.styleName(style.id), style = MaterialTheme.typography.titleMedium)
                                        Text(content.styleText(style.id))
                                    }
                                }
                            }
                        }
                    }

                    Tab.SETTINGS -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text("Settings", style = MaterialTheme.typography.headlineMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Night Map Theme")
                                Switch(checked = darkTheme, onCheckedChange = { darkTheme = it })
                            }
                            Button(onClick = {
                                LocalNotifier.requestPermission()
                                LocalNotifier.notify("Fogwalk", "Evening walk reminder enabled.")
                            }) {
                                Text("Test Notification")
                            }
                            OutlinedButton(onClick = {
                                SystemSharer.shareText(
                                    title = "Fogwalk",
                                    text = "I've revealed ${revealedCells.size} cells in Fogwalk!",
                                )
                            }) {
                                Text("Share Progress")
                            }
                            OutlinedButton(onClick = { showOnboarding = !showOnboarding }) {
                                Text(if (showOnboarding) "Hide Onboarding" else "Show Onboarding")
                            }
                        }
                    }
                }
            }
        }
    }
}
