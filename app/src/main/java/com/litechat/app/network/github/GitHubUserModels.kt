package com.litechat.app.network.github

import kotlinx.serialization.Serializable

@Serializable
data class GitHubUsersFile(
    val users: List<GitHubUserEntry> = emptyList()
)

@Serializable
data class GitHubUserEntry(
    val id: String,
    val username: String,
    val displayName: String,
    val statusMessage: String = "Hey, I'm using LiteChat",
    val avatarUrl: String? = null,
    val createdAt: String = "",
    val lastUpdated: String = ""
)
