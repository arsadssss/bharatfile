package com.bharatfile.app.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Tools : Screen("tools")
    object CompressPdf : Screen("compress_pdf")
    object CompressImage : Screen("compress_image")
    object ResizeImage : Screen("resize_image")
    object ConvertImage : Screen("convert_image")
    object ImageToPdf : Screen("image_to_pdf")
    object PdfToImage : Screen("pdf_to_image")
    object PdfMerge : Screen("merge_pdf")
    object PdfSplit : Screen("split_pdf")
    object PdfOrganize : Screen("organize_pdf")
    object Scanner : Screen("scanner")
    object SignatureMaker : Screen("signature_maker")
    object PassportPhoto : Screen("passport_photo")
    object History : Screen("history")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object About : Screen("about")
    object Privacy : Screen("privacy")
    object Terms : Screen("terms")
    object Contact : Screen("contact")
}

