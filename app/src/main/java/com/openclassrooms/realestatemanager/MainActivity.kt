package com.openclassrooms.realestatemanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import coil.compose.rememberImagePainter
import com.openclassrooms.realestatemanager.model.DataProvider
import com.openclassrooms.realestatemanager.model.Estate
import com.openclassrooms.realestatemanager.model.EstateType
import com.openclassrooms.realestatemanager.ui.theme.RealEstateManagerTheme
import java.text.NumberFormat
import java.util.*

@ExperimentalAnimationApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val configuration = LocalConfiguration.current
            var openDrawer by remember { mutableStateOf(true) }
            var currentEstateID by remember { mutableStateOf(0) }

            RealEstateManagerTheme {
                Box {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(top = 56.dp)
                    ) {
                        AnimatedVisibility(visible = openDrawer, enter = expandHorizontally(), exit = shrinkHorizontally()) {
                            RealEstateList(currentEstateID) {
                                currentEstateID = it
                                if (configuration.screenWidthDp <= 450)
                                    openDrawer = !openDrawer
                            }
                        }
                        RealEstateInfo(DataProvider.estateList[currentEstateID])
                    }
                    TopAppBar(content = {
                        Icon(
                            Icons.Default.Menu,
                            "LeftMenuOpen",
                            modifier = Modifier
                                .clickable(onClick = {
                                    openDrawer = !openDrawer
                                })
                                .padding(16.dp, 8.dp)
                        )
                        Text(text = getString(R.string.app_name),
                            style = MaterialTheme.typography.h6.copy(color = Color.White),
                            fontWeight = FontWeight.Bold)
                    })
                }
            }
        }
    }
}

fun openImage(context: Context, photo: String) {
    val intent = Intent(context, ImageViewActivity::class.java).apply {
        putExtra("img", photo)
    }
    startActivity(context, intent, null)
}

fun getRequest(
    width: Int = 400,
    height: Int = 400,
    localisation: String = "-74.005157,40.710785",
    apiKey: String = BuildConfig.GEOAPIFY_KEY,
): String {
    return "https://maps.geoapify.com/v1/staticmap" +
            "?style=osm-bright-grey" +
            "&width=$width&height=$height" +
            "&center=lonlat:$localisation" +
            "&zoom=16.4226&pitch=44" +
            "&marker=lonlat:$localisation;color:%23ff0000;size:medium" +
            "&apiKey=$apiKey"
}

@ExperimentalAnimationApi
@Composable
fun RealEstateList(currentEstateID: Int, selected: (Int) -> Unit = {}) {
    LazyColumn {
        itemsIndexed(DataProvider.estateList) { id, estate ->
            RealEstateListItem(estate, currentEstateID == id) {
                selected(id)
            }
        }
    }
}

@Composable
fun RealEstateListItem(estate: Estate, isSelected: Boolean, selected: () -> Unit = {}) {
    val configuration = LocalConfiguration.current
    val small = configuration.screenWidthDp <= 450
    Row(
        modifier = Modifier
            .width(if (small) configuration.screenWidthDp.dp else 250.dp)
            .background(if (isSelected) MaterialTheme.colors.secondary else MaterialTheme.colors.background)
            .drawBehind {
                val strokeWidth = 1 * density
                val y = size.height - strokeWidth / 2
                drawLine(
                    Color.LightGray,
                    Offset(0f, y),
                    Offset(size.width, y),
                    strokeWidth
                )
                drawLine(
                    Color.LightGray,
                    Offset(size.width, 0f),
                    Offset(size.width, y),
                    strokeWidth
                )
            }
            .clickable { selected() }
    )
    {
        Image(
            painter = rememberImagePainter(estate.pictures.first().second),
            contentDescription = "Main",
            modifier = Modifier.size(108.dp)
        )
        Column(
            Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(text = estate.type.toString())
            Text(text = estate.district)

            // Format value to USD formatting
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.maximumFractionDigits = 0
            format.currency = Currency.getInstance("USD")
            Text(text = format.format(estate.price))


        }
    }

}

@Composable
fun RealEstateInfo(estate: Estate) {
    Column(
        Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(text = "Media", style = MaterialTheme.typography.h5)
        LazyRow {
            items(estate.pictures) { photo ->
                RealEstatePhoto(photo)
            }
        }
        Text(text = "Description", style = MaterialTheme.typography.h5, modifier = Modifier.padding(top = 16.dp))
        Text(text = estate.description, style = MaterialTheme.typography.body2, modifier = Modifier.padding(top = 16.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.weight(1.0f)) {
                InfoDetailItem(Icons.Default.Face, "Surface", estate.surface.toString())
                InfoDetailItem(Icons.Default.Person, "Number of rooms", estate.numberOfRooms.toString())
                InfoDetailItem(Icons.Default.Info, "Number of bathrooms", estate.numberOfBathrooms.toString())
                InfoDetailItem(Icons.Default.AccountBox, "Number of bedrooms", estate.numberOfBedrooms.toString())
            }
            Column(Modifier.weight(1.0f)) {
                InfoDetailItem(Icons.Default.LocationOn, "Location", estate.address, leftSpacing = 24.dp)
            }
            Column(Modifier.weight(1.0f), verticalArrangement = Arrangement.Top) {
                MapPinView()
            }
        }
    }
}

@Composable
fun MapPinView(localisation: String = "-74.005157,40.710785") {
    val context = LocalContext.current
    Image(
        rememberImagePainter(
            data = getRequest(400, 400, localisation),
            builder = {
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_foreground)
            }
        ),
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(250.dp)
            .clickable {
                openImage(context, getRequest(1024, 1024, localisation))
            },
        contentDescription = "MapView",
    )
}

@Composable
fun InfoDetailItem(icon: ImageVector, title: String, value: String, leftSpacing: Dp = 64.dp, bottomSpacing: Dp = 8.dp) {
    Row {
        Image(icon, icon.name)
        Text(text = title, modifier = Modifier.padding(paddingValues = PaddingValues(start = 8.dp)))
    }
    Row {
        Spacer(Modifier.width(leftSpacing))
        Text(text = value, textAlign = TextAlign.Center)
    }
    Spacer(Modifier.height(bottomSpacing))
}

@Composable
fun RealEstatePhoto(photo: Pair<String, String>) {
    val image = rememberImagePainter(photo.second)
    val context = LocalContext.current
    Card(elevation = 4.dp, modifier = Modifier
        .padding(8.dp)
        .clickable {
            openImage(context, photo.second)
        }
    )
    {
        Box {
            Image(
                image,
                contentDescription = "A Photo",
                Modifier.size(108.dp)
            )
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(108.dp),
            ) {
                Text(
                    text = photo.first,
                    style = MaterialTheme.typography.subtitle1.copy(color = Color.White),
                    modifier = Modifier
                        .align(Center)
                        .padding(8.dp),
                )
            }
        }
    }
}

// ----------------------------------------------------------------------------
//
// PREVIEW
//
// ----------------------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun RealEstateItemPreview() {
    RealEstateManagerTheme {
        RealEstateListItem(
            Estate(district = "Manhattan", type = EstateType.Flat, description = DataProvider.loremIpsum[0], price = 17870000),
            false
        )
    }
}