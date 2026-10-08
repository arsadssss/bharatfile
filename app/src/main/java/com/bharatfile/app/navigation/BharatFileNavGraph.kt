package com.bharatfile.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bharatfile.app.model.DocumentType
import com.bharatfile.app.ui.components.BharatFileBottomNav
import com.bharatfile.app.ui.components.NavigationDrawerContent
import com.bharatfile.app.ui.screens.DocumentScannerScreen
import com.bharatfile.app.ui.screens.HistoryScreen
import com.bharatfile.app.ui.screens.HomeScreen
import com.bharatfile.app.ui.screens.ImageConverterScreen
import com.bharatfile.app.ui.screens.ImageResizerScreen
import com.bharatfile.app.ui.screens.ImageToPdfScreen
import com.bharatfile.app.ui.screens.InfoDetailScreen
import com.bharatfile.app.ui.screens.PassportPhotoScreen
import com.bharatfile.app.ui.screens.PdfMergeScreen
import com.bharatfile.app.ui.screens.PdfOrganizeScreen
import com.bharatfile.app.ui.screens.PdfSplitScreen
import com.bharatfile.app.ui.screens.PdfToImageScreen
import com.bharatfile.app.ui.screens.ProfileScreen
import com.bharatfile.app.ui.screens.SettingsScreen
import com.bharatfile.app.ui.screens.SignatureMakerScreen
import com.bharatfile.app.ui.screens.ToolsGridScreen
import com.bharatfile.app.ui.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@Composable
fun BharatFileNavGraph(
    homeViewModel: HomeViewModel,
    navController: NavHostController = rememberNavController()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val isDarkMode by homeViewModel.isDarkMode.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val navigateToRoute: (String) -> Unit = { route ->
        when (route) {
            "compress_pdf", "home_pdf" -> {
                homeViewModel.setDocumentType(DocumentType.PDF)
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            "compress_image", "home_image" -> {
                homeViewModel.setDocumentType(DocumentType.IMAGE)
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            Screen.Home.route -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            else -> {
                navController.navigate(route) {
                    launchSingleTop = true
                }
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(
                    currentRoute = currentRoute,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { homeViewModel.toggleDarkMode() },
                    onNavigate = { route ->
                        scope.launch { drawerState.close() }
                        navigateToRoute(route)
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                // Persistent modern Bottom Navigation Bar
                BharatFileBottomNav(
                    currentRoute = currentRoute,
                    onNavigateToRoute = navigateToRoute
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToImages = {
                            homeViewModel.setDocumentType(DocumentType.IMAGE)
                        },
                        onNavigateToResize = {
                            navController.navigate(Screen.ResizeImage.route)
                        },
                        onNavigateToConvert = {
                            navController.navigate(Screen.ConvertImage.route)
                        },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.Tools.route) {
                    ToolsGridScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute
                    )
                }

                composable(Screen.History.route) {
                    HistoryScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToHome = { navigateToRoute(Screen.Home.route) }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.Settings.route) {
                    SettingsScreen(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { homeViewModel.toggleDarkMode() },
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.ResizeImage.route) {
                    ImageResizerScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.ConvertImage.route) {
                    ImageConverterScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.ImageToPdf.route) {
                    ImageToPdfScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.PdfToImage.route) {
                    PdfToImageScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.PdfMerge.route) {
                    PdfMergeScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.PdfSplit.route) {
                    PdfSplitScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.PdfOrganize.route) {
                    PdfOrganizeScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.Scanner.route) {
                    DocumentScannerScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.SignatureMaker.route) {
                    SignatureMakerScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }

                composable(Screen.PassportPhoto.route) {
                    PassportPhotoScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToRoute = navigateToRoute,
                        snackbarHostState = snackbarHostState
                    )
                }


                composable(Screen.About.route) {
                    InfoDetailScreen(
                        title = "About BharatFile",
                        contentParagraphs = listOf(
                            "BharatFile is an all-in-one, privacy-focused document utility application engineered specifically for Indian students, teachers, job aspirants, and office professionals.",
                            "Our mission is simple: provide powerful, lightning-fast file utilities that work 100% on your device without ever uploading your confidential personal documents, certificates, or photos to any cloud server.",
                            "Whether you are compressing a PDF for UPSC or SSC online application forms, creating signature PNGs, merging marks sheets, or resizing passport photos to strict portal specifications, BharatFile delivers instant results with zero hassle."
                        ),
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Privacy.route) {
                    InfoDetailScreen(
                        title = "Privacy Policy",
                        contentParagraphs = listOf(
                            "100% On-Device Processing: Your documents never leave your phone. All PDF compression, image resizing, conversions, and scanning algorithms run entirely offline using local Android processors.",
                            "Zero Data Tracking: BharatFile does not require any account creation, login, phone number, or email address.",
                            "Permissions: We only request camera access when you actively use the Document Scanner tool, and we use modern Android system photo pickers that grant access strictly to the single file you select."
                        ),
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Terms.route) {
                    InfoDetailScreen(
                        title = "Terms of Service",
                        contentParagraphs = listOf(
                            "By using BharatFile, you agree to utilize its document tools in compliance with applicable local and national laws.",
                            "BharatFile is provided free of charge for students, job applicants, and professionals. The app and its developers do not store, view, or retain any user files generated on the device.",
                            "All output files are saved directly into your device's Downloads/BharatFile directory."
                        ),
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Contact.route) {
                    InfoDetailScreen(
                        title = "Contact Us",
                        contentParagraphs = listOf(
                            "Need help, have a suggestion, or want a new tool added for Indian student applications?",
                            "Reach out to our team at: support@bharatfile.app",
                            "We actively review student requests for regional examination formats and document specifications."
                        ),
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
