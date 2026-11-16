package com.privatecloset.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.privatecloset.app.data.repository.LearningRepository
import com.privatecloset.app.di.ViewModelFactory
import com.privatecloset.app.ui.auth.AuthScreen
import com.privatecloset.app.ui.common.BackendConnectionDialog
import com.privatecloset.app.ui.community.CommunityScreen
import com.privatecloset.app.ui.community.CommunityViewModel
import com.privatecloset.app.ui.courses.CoursesScreen
import com.privatecloset.app.ui.courses.CoursesViewModel
import com.privatecloset.app.ui.dashboard.DashboardScreen
import com.privatecloset.app.ui.dashboard.DashboardViewModel
import com.privatecloset.app.ui.lesson.LessonScreen
import com.privatecloset.app.ui.lesson.LessonViewModel
import com.privatecloset.app.ui.marketplace.MarketplaceScreen
import com.privatecloset.app.ui.marketplace.MarketplaceViewModel
import com.privatecloset.app.ui.onboarding.OnboardingScreen
import com.privatecloset.app.ui.plans.PlansScreen
import com.privatecloset.app.ui.plans.PlansViewModel
import com.privatecloset.app.ui.practice.PracticeScreen
import com.privatecloset.app.ui.practice.PracticeViewModel
import com.privatecloset.app.ui.progress.ProgressScreen
import com.privatecloset.app.ui.progress.ProgressViewModel
import com.privatecloset.app.ui.splash.SplashScreen
import com.privatecloset.app.ui.theme.BgCard
import com.privatecloset.app.ui.theme.Gold
import com.privatecloset.app.ui.theme.TextSecondary
import com.privatecloset.app.ui.tutor.TutorStudioScreen
import com.privatecloset.app.ui.tutor.TutorStudioViewModel

private object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val AUTH = "auth"
    const val DASHBOARD = "dashboard"
    const val COURSES = "courses"
    const val PRACTICE = "practice"
    const val COMMUNITY = "community"
    const val MARKETPLACE = "marketplace"
    const val TUTOR_STUDIO = "tutor_studio"
    const val LESSON = "lesson"
    const val PROGRESS = "progress"
    const val PLANS = "plans"
}

private data class BottomTab(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTab(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    BottomTab(Routes.COURSES, "Courses", Icons.Filled.MenuBook),
    BottomTab(Routes.PRACTICE, "64 Keys", Icons.Filled.Piano),
    BottomTab(Routes.COMMUNITY, "Community", Icons.Filled.Chat),
    BottomTab(Routes.MARKETPLACE, "Market", Icons.Filled.ShoppingBag),
)

@Composable
fun PrivateClosetApp(repository: LearningRepository) {
    val navController = rememberNavController()
    val factory = remember(repository) { ViewModelFactory(repository) }
    var showBackendDialog by remember { mutableStateOf(false) }
    val connectionState by repository.getConnectionState().collectAsState()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomTabs.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BgCard) {
                    bottomTabs.forEach { tab ->
                        val selected = backStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { androidx.compose.material3.Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Gold,
                                selectedTextColor = Gold,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary,
                                indicatorColor = BgCard,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .background(androidx.compose.ui.graphics.Color.Transparent)
                .padding(if (showBottomBar) padding else androidx.compose.foundation.layout.PaddingValues(0.dp)),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(onFinished = {
                    navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } }
                })
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onContinue = { navController.navigate(Routes.AUTH) })
            }
            composable(Routes.AUTH) {
                AuthScreen(
                    repository = repository,
                    onGetStarted = {
                        navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.AUTH) { inclusive = true } }
                    },
                    onLogIn = {
                        navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.AUTH) { inclusive = true } }
                    },
                )
            }
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                DashboardScreen(
                    uiState = state,
                    connectionState = connectionState,
                    onConfigureBackend = { showBackendDialog = true },
                    onOpenCourses = { navController.navigate(Routes.COURSES) },
                    onOpenLessons = { navController.navigate(Routes.LESSON) },
                    onOpenPractice = { navController.navigate(Routes.PRACTICE) },
                    onOpenSchedule = { navController.navigate(Routes.COURSES) },
                    onOpenProgress = { navController.navigate(Routes.PROGRESS) },
                    onOpenCommunity = { navController.navigate(Routes.COMMUNITY) },
                    onOpenMarketplace = { navController.navigate(Routes.MARKETPLACE) },
                    onOpenTutorStudio = { navController.navigate(Routes.TUTOR_STUDIO) },
                )
            }
            composable(Routes.COURSES) {
                val vm: CoursesViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                CoursesScreen(
                    uiState = state,
                    onCourseClick = { navController.navigate(Routes.LESSON) },
                )
            }
            composable(Routes.LESSON) {
                val vm: LessonViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                androidx.compose.runtime.LaunchedEffect(Unit) { vm.load("l1") }
                LessonScreen(
                    uiState = state,
                    onBack = { navController.popBackStack() },
                    onMarkComplete = { vm.markComplete("l1") },
                )
            }
            composable(Routes.PRACTICE) {
                val vm: PracticeViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                PracticeScreen(uiState = state)
            }
            composable(Routes.COMMUNITY) {
                val vm: CommunityViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                CommunityScreen(
                    uiState = state,
                    onSelectChannel = { channel -> vm.selectChannel(channel) },
                    onSendMessage = { text -> vm.sendMessage(text) },
                )
            }
            composable(Routes.MARKETPLACE) {
                val vm: MarketplaceViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                MarketplaceScreen(
                    uiState = state,
                    onSelectCategory = { cat -> vm.setCategory(cat) },
                    onSearch = { query -> vm.setSearchQuery(query) },
                    onPurchase = { id, method, details, cb -> vm.purchaseItem(id, method, details, cb) },
                    onPublish = { item, cb -> vm.publishItem(item, cb) },
                )
            }
            composable(Routes.TUTOR_STUDIO) {
                val vm: TutorStudioViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                TutorStudioScreen(
                    uiState = state,
                    onUploadCourse = { draft, cb -> vm.uploadCourse(draft, cb) },
                    onVerifyInviteCode = { code, cb -> vm.verifyTutorWithCode(code, cb) },
                    onSwitchRole = { role ->
                        repository.setUserRole(role)
                        vm.loadTutorStudio()
                    }
                )
            }
            composable(Routes.PROGRESS) {
                val vm: ProgressViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                ProgressScreen(uiState = state)
            }
            composable(Routes.PLANS) {
                val vm: PlansViewModel = viewModel(factory = factory)
                val state by vm.uiState.collectAsState()
                PlansScreen(uiState = state)
            }
        }

        if (showBackendDialog) {
            BackendConnectionDialog(
                repository = repository,
                currentState = connectionState,
                onDismiss = { showBackendDialog = false },
            )
        }
    }
}
