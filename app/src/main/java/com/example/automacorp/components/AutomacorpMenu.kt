import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.automacorp.R
import com.example.automacorp.ui.theme.AutomacorpTheme
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AutomacorpTopAppBar(title: String? = null, returnAction: () -> Unit = {}) {
    val context = LocalContext.current
    val colors = TopAppBarDefaults.mediumTopAppBarColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor = MaterialTheme.colorScheme.primary,
    )
    val actions: @Composable RowScope.() -> Unit = {
        IconButton(onClick = { /* do something */ }) {
            Icon(
                painter = painterResource(R.drawable.ic_action_rooms),
                contentDescription = stringResource(R.string.app_go_room_description)
            )
        }
        IconButton(onClick = { openContactPage(context, "email")}) {
            Icon(
                painter = painterResource(R.drawable.ic_action_mail),
                contentDescription = stringResource(R.string.app_go_mail_description)
            )
        }
        IconButton(onClick = { openContactPage(context, "github") }) {
            Icon(
                painter = painterResource(R.drawable.ic_action_github),
                contentDescription = stringResource(R.string.app_go_github_description)
            )
        }
    }
    if(title == null) {
        TopAppBar(
            title = { Text("") },
            colors = colors,
            actions = actions
        )
    } else {
        MediumTopAppBar(
            title = { Text(title) },
            colors = colors,
            navigationIcon = {
                IconButton(onClick = returnAction) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.app_go_back_description)
                    )
                }
            },
            actions = actions
        )
    }
}

fun openContactPage(context: Context, type: String) {
    try {
        val intent = when(type) {
            "email" -> {
                Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:sajidbrong@gmail.com") // Email address
                    putExtra(Intent.EXTRA_SUBJECT, "Contact Us")
                    putExtra(Intent.EXTRA_TEXT, "Hello, I have an inquiry about your app.")
                }
            }
            "github" -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://github.com/bronglil")
                }
            }
            else -> null
        }

        intent?.let {
            context.startActivity(it)
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
}


@Preview(showBackground = true)
@Composable
fun AutomacorpTopAppBarHomePreview() {
    AutomacorpTheme {
        AutomacorpTopAppBar(null)
    }
}

@Preview(showBackground = true)
@Composable
fun AutomacorpTopAppBarPreview() {
    AutomacorpTheme {
        AutomacorpTopAppBar("A page")
    }
}