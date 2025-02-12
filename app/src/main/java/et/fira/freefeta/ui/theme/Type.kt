package et.fira.freefeta.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import et.fira.freefeta.R

val BebasNeue = FontFamily(
    Font(R.font.bebas_neue_regular),
)

val Raleway = FontFamily(
    Font(R.font.raleway_regular),
    Font(R.font.raleway_bold, FontWeight.Bold),
)

val NotoSans = FontFamily(
    Font(R.font.noto_sans_regular),
    Font(R.font.noto_sans_bold, FontWeight.Bold),
)

val Lato = FontFamily(
    Font(R.font.lato_regular),
    Font(R.font.lato_bold, FontWeight.Bold),
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Light,
        fontSize = 28.sp
    ),
    displaySmall = TextStyle(
        fontFamily = BebasNeue,
        fontWeight = FontWeight.Thin,
        fontSize = 24.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Raleway,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Raleway,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Raleway,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,

    ),
    titleMedium = TextStyle(
        fontFamily = Raleway,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,

    ),
    bodyLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Lato,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
    )

)