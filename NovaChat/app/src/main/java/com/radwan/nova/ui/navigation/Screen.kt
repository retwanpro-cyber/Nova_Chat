package com.radwan.nova.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Otp : Screen("otp/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "otp/$phoneNumber"
    }
    object ProfileSetup : Screen("profile_setup")
    object Home : Screen("home")
    object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    object Calls : Screen("calls")
    object ActiveCall : Screen("active_call/{contactId}/{isVideo}") {
        fun createRoute(contactId: String, isVideo: Boolean) = "active_call/$contactId/$isVideo"
    }
    object Spaces : Screen("spaces")
    object Moments : Screen("moments")
    object Saved : Screen("saved")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object QrCode : Screen("qr_code")
    object Settings : Screen("settings")
    object Appearance : Screen("appearance")
    object About : Screen("about")
}
