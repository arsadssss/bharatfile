package com.bharatfile.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bharatfile.app.R
import com.bharatfile.app.model.DocumentType
import com.bharatfile.app.theme.BharatFileThemeCustom
import com.bharatfile.app.theme.InnerCardShape

@Composable
fun SizeComparisonCard(
    originalSize: String,
    compressedSize: String,
    modifier: Modifier = Modifier,
    documentType: DocumentType = DocumentType.PDF
) {
    val colors = BharatFileThemeCustom.colors

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = InnerCardShape,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Original side
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = if (documentType == DocumentType.PDF) R.drawable.ic_pdf_badge else R.drawable.ic_pdf_green_badge),
                    contentDescription = "Original",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = originalSize,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Original",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = colors.textSecondary
                    )
                }
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "to",
                tint = colors.textSecondary,
                modifier = Modifier.size(18.dp)
            )

            // Compressed side
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_pdf_green_badge),
                    contentDescription = "Compressed",
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = compressedSize,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Compressed",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp
                        ),
                        color = colors.textSecondary
                    )
                }
            }
        }
    }
}
