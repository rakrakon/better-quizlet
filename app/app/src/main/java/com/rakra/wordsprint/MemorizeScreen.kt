package com.rakra.wordsprint

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rakra.wordsprint.ui.theme.WordSprintTheme
import kotlinx.coroutines.delay

@Preview(showBackground = true)
@Composable
fun MemorizationPreview() {
    val wordList2 = listOf(
        WordMeaning("בְּצַוְותָּא", "ביחד"),
        WordMeaning("אבנט", "חגורה רחבה"),
        WordMeaning("בְּרַם", "אולם, אבל"),
        WordMeaning("גָלְמוּד", "בודד"),
        WordMeaning("דָּהוּי", "שצבעו נחלש והחוויר"),
        WordMeaning("גְּלָלִים", "צואת בעלי החיים"),

        WordMeaning("בְּעֶטְיוֹ", "בעצתו (הרעה), בשל מזימתו, באשמתו, (עטי = עצה רעה)"),
        WordMeaning("בָּצִיר", "הענבים עצמם"),
        WordMeaning("בְּרֹאשׁ חוּצוֹת", "בפומבי, בפרהסיה"),
        WordMeaning("בָּרִי", "ברור, ודאי, ידוע"),
        WordMeaning("בְּרֵישׁ גְּלֵי", "לעיני כל"),
        WordMeaning("בַּר-מִינָן", "מת, מנוח"),
        WordMeaning("בָּרַר", "בחר את מה שמתאים ביותר, סינן"),
        WordMeaning("גָּדַשׁ", "מִילא עד אפס מקום"),
        WordMeaning("גָּדַש אֶת הַסְּאָה", "הגזים, הפריז, עבר את הגבול (סאה - מידת נפח עתיקה)"),
        WordMeaning("גֵּז", "צמר שנגזז מהצאן"),
        WordMeaning("גָּז", "נעלם, נמוג, חלף"),
        WordMeaning("גְּזֵרָה", "פקודה או חוק של איסור כלשהו, פסק דין, תקנה, הוראה"),
        WordMeaning("גַּיְא", "שקע צר בין הרים"),
        WordMeaning("גִּיל / גִּילָה", "שמחה, ששון, אושר"),
        WordMeaning("גַּלְעִין", "גרעין הפרי, ליבה, חרצן"),
        WordMeaning("גְּמִילָה", "הבשלת הפרי"),
        WordMeaning("גַּנְזַך", "ארכיב, ארכיון, מקום לאכסון מסמכים (לרוב ממשלתי)"),
        WordMeaning("גַּפַּיִם", "שם כולל לרגלים והידיים של בני אדם ובעלי חיים"),
        WordMeaning("גָּרַע", "החסיר"),
        WordMeaning(
            "דָּלָה",
            "שאב, הוציא מהמים (דָּלָה פנינים). בהשאלה: הוציא משהו לאחר חיפוש (לדלות מידע)"
        ),
        WordMeaning("דָלַק", "בער"),
        WordMeaning("דָּרוּך", "מתוח, משוך"),
        WordMeaning("הֶאֱמִיר", "עלה, התייקר"),
        WordMeaning("הִבְלִיח", "הבהב באור חלש ובלתי יציב, נצנץ"),
        WordMeaning("הִגְדִּישׁ אֶת הַסְּאָה", "הגזים, הפריז, עבר את הגבול (סאה - מידת נפח עתיקה)"),
        WordMeaning("הֵגִיף", "סגר, נעל, הבריח"),
        WordMeaning("הִדְבִּיר", "ריסס נגד מזיקים"),
        WordMeaning("הִדִּיר רַגְלָיו", "נמנע מלבקר, הגיע לעתים רחוקות"),
        WordMeaning("הִדִּיר שֵׁינָה מֵעֵינָיו", "לא נרדם"),
        WordMeaning("הֵהִין", "העז"),
        WordMeaning("הוֹכִיחַ", "הטיף לו מוסר")
    )


    WordSprintTheme {
        MemorizationScreen(wordList2)
    }
}

// TODO: Add definition screen for each word

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorizationScreen(wordsWithMeanings: List<WordMeaning>) {
    val density = LocalDensity.current
    val visibleWords = remember { wordsWithMeanings.toMutableStateList() }

    val visibilityMap = remember { mutableStateMapOf<String, Boolean>().apply {
        wordsWithMeanings.forEach { this[it.word] = true }
    }}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BACKGROUND_COLOR)
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 12.dp)
    ) {
        Text(
            text = "שינון מילים",
            fontSize = 32.sp,
            fontFamily = RUBIK_FONT,
            color = Color.White,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        visibleWords.forEach { wordMeaning ->
            val dismissState = remember(wordMeaning.word) {
                SwipeToDismissBoxState(
                    initialValue = SwipeToDismissBoxValue.Settled,
                    density = density,
                    confirmValueChange = { true },
                    positionalThreshold = { it * 0.5f }
                )
            }

            val target = dismissState.targetValue
            val swiped = target != SwipeToDismissBoxValue.Settled

            if (swiped && visibilityMap[wordMeaning.word] == true) {
                LaunchedEffect(wordMeaning.word) {
                    visibilityMap[wordMeaning.word] = false
                    delay(300)
                    visibleWords.remove(wordMeaning)
                    visibilityMap.remove(wordMeaning.word)
                }
            }

            AnimatedVisibility(
                visible = visibilityMap[wordMeaning.word] ?: true,
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) +
                        fadeOut(animationSpec = tween(300)),
                modifier = Modifier.animateContentSize()
            ) {
                Column {
                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (target) {
                                SwipeToDismissBoxValue.StartToEnd -> Color(0xFF81C784) // Green
                                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE57373) // Red
                                else -> Color.Transparent
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                            )
                        },
                        content = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(Color(0xFF2C2733))
                                    .padding(vertical = 8.dp, horizontal = 12.dp)
                            ) {
                                Text(
                                    text = wordMeaning.word,
                                    fontSize = 24.sp,
                                    fontFamily = RUBIK_FONT,
                                    color = Color.White,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
