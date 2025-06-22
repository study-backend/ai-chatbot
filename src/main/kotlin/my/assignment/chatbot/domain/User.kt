package my.assignment.chatbot.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.OffsetDateTime

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    private val password: String,

    @Column(nullable = false)
    val name: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = Role.USER,

    val enabled: Boolean = true
) : UserDetails {

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val threads: MutableList<Threads> = mutableListOf()

    override fun getUsername(): String = email  // 이메일을 username으로 사용
    override fun getPassword(): String = password
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = enabled

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
    }

    fun getOrCreateActiveThread(): Threads {
        val activeThread = threads.find { !it.isExpired() }
        return if (activeThread != null) {
            activeThread.updateLastActivity()
            activeThread
        } else {
            val newThreads = Threads(user = this)
            threads.add(newThreads)
            newThreads
        }
    }

}

enum class Role {
    USER, ADMIN
}