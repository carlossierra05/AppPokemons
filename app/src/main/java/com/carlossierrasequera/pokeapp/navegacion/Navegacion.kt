package com.carlossierrasequera.pokeapp.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.carlossierrasequera.pokeapp.data.AuthManager
import com.carlossierrasequera.pokeapp.screen.*

@Composable
fun Navegacion(auth: AuthManager) {
    val navController = rememberNavController()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                auth,
                { navController.navigate(SignUp) },
                {
                    navController.navigate(Home) {
                        popUpTo(Login) { inclusive = true }
                    }
                },
                { navController.navigate(ForgotPassword) }
            )
        }

        composable<SignUp> {
            SignUpScreen(auth) {
                navController.popBackStack()
            }
        }

        composable<Home> {
            HomeScreen(
                auth,
                {
                    navController.navigate(Login) {
                        popUpTo(Home) { inclusive = true }
                    }
                },
                { navController.navigate(Entrenadores) },
                { navController.navigate(Batallas) }
            )
        }

        composable<ForgotPassword> {
            ForgotPasswordScreen(auth) {
                navController.navigate(Login) {
                    popUpTo(Login) { inclusive = true }
                }
            }
        }

        // 🔹 NO PASAMOS auth aquí porque EntrenadoresScreen usa ViewModel
        composable<Entrenadores> {
            EntrenadoresScreen(
                { navController.navigate(Home) },
                { navController.navigate(Batallas) },
                {
                    navController.navigate(Login) {
                        popUpTo(Home) { inclusive = true }
                    }
                }
            )
        }

        // 🔹 Si BatallasScreen también usa ViewModel, NO PASAMOS auth
        composable<Batallas> {
            BatallasScreen(
                { navController.navigate(Home) },
                { navController.navigate(Entrenadores) },
                {
                    navController.navigate(Login) {
                        popUpTo(Home) { inclusive = true }
                    }
                }
            )
        }
    }
}
