package com.example.data.repository

import com.example.model.User
import com.example.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserSessionManager {
    val predefinedUsers = listOf(
        User(
            userId = "user_student",
            name = "Anand Kumar",
            email = "anand.k@college.edu",
            role = UserRole.STUDENT,
            department = "Computer Science & Engineering"
        ),
        User(
            userId = "user_club_president",
            name = "Rahul Verma",
            email = "rahul.v@college.edu",
            role = UserRole.CLUB_PRESIDENT,
            department = "Computer Science & Engineering",
            clubAffiliation = "club_coding",
            clubRole = UserRole.CLUB_PRESIDENT
        ),
        User(
            userId = "user_club_admin",
            name = "Neha Gupta",
            email = "neha.g@college.edu",
            role = UserRole.CLUB_ADMIN,
            department = "Computer Science & Engineering",
            clubAffiliation = "club_coding",
            clubRole = UserRole.CLUB_ADMIN
        ),
        User(
            userId = "user_club_member",
            name = "Arjun Patel",
            email = "arjun.p@college.edu",
            role = UserRole.CLUB_MEMBER,
            department = "Computer Science & Engineering",
            clubAffiliation = "club_coding",
            clubRole = UserRole.CLUB_MEMBER
        ),
        User(
            userId = "user_cultural_member",
            name = "Rohan Mehta",
            email = "rohan.m@college.edu",
            role = UserRole.CLUB_MEMBER,
            department = "Campus Student Affairs",
            clubAffiliation = "club_cultural",
            clubRole = UserRole.CLUB_MEMBER
        ),
        User(
            userId = "user_faculty",
            name = "Dr. Ramanathan",
            email = "ramanathan.cse@college.edu",
            role = UserRole.FACULTY,
            department = "Computer Science & Engineering"
        ),
        User(
            userId = "user_admin",
            name = "Prof. S. Swaminathan",
            email = "admin.events@college.edu",
            role = UserRole.PLATFORM_ADMIN,
            department = "Central Administration & IT"
        ),
        User(
            userId = "user_community",
            name = "Pooja M.",
            email = "pooja.alumni@college.edu",
            role = UserRole.COMMUNITY_MEMBER,
            department = "Campus Alumni & Community"
        )
    )

    private val _currentUser = MutableStateFlow(predefinedUsers[0]) // Starts as STUDENT
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    fun switchUser(user: User) {
        _currentUser.value = user
    }

    fun switchRole(role: UserRole) {
        val current = _currentUser.value
        // If current user is one of predefined users, pick matching predefined user
        // otherwise preserve their auth credentials and just change the role!
        val isCustomAuth = !predefinedUsers.any { it.userId == current.userId }
        if (isCustomAuth) {
            _currentUser.value = current.copy(role = role)
        } else {
            val matching = predefinedUsers.find { it.role == role } ?: predefinedUsers[0]
            _currentUser.value = matching
        }
    }

    fun updateUserProfile(
        name: String? = null,
        role: UserRole? = null,
        department: String? = null,
        clubAffiliation: String? = null
    ) {
        val current = _currentUser.value
        _currentUser.value = current.copy(
            name = name ?: current.name,
            role = role ?: current.role,
            department = department ?: current.department,
            clubAffiliation = clubAffiliation ?: current.clubAffiliation
        )
    }

    fun signOut() {
        _currentUser.value = predefinedUsers[0]
    }
}
