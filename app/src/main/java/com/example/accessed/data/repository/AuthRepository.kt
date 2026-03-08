package com.example.accessed.data.repository

import com.example.accessed.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun signup(name: String, email: String, pass: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val userId = result.user?.uid ?: throw Exception("Signup failed")
            val user = User(userId, name, email, isGuest = false)
            firestore.collection("Users").document(userId).set(user).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, pass: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val userId = result.user?.uid ?: throw Exception("Login failed")
            val doc = firestore.collection("Users").document(userId).get().await()
            val user = doc.toObject(User::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun continueAsGuest(): Result<User> {
        return try {
            val result = auth.signInAnonymously().await()
            val userId = result.user?.uid ?: throw Exception("Guest login failed")
            val user = User(userId, "Guest Student", "guest@accessed.local", isGuest = true)
            // Optional: Store guest data in Firestore or keep it local
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            // This is a simplified version; normally you'd fetch from Firestore if needed
            User(firebaseUser.uid, firebaseUser.displayName ?: "User", firebaseUser.email ?: "", isGuest = firebaseUser.isAnonymous)
        } else null
    }
}
