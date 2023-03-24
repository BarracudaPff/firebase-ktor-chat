package ru.otus.kotlin.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserRecord
import com.google.firebase.database.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.otus.kotlin.models.*
import ru.otus.kotlin.ws.Disposable
import ru.otus.kotlin.ws.gson
import kotlin.coroutines.resume

/**
 * Top-level service to handle all "firebase-related" code.
 * Contains setValue, update values and subscribe functions
 */
object FirebaseService {
    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usersRef = database.getReference("users")
    private val chatRef = database.getReference("chat")
    private val msgRef = chatRef.child("messages")

    /**
     * Create user based on his email and password
     */
    private fun signUpUser(req: AuthRequest): UserRecord {
        val request = UserRecord.CreateRequest().apply {
            setEmail(req.email)
            setDisplayName(req.name)
            setPassword(req.password)
        }

        return auth.createUser(request)
    }

    /**
     * Create user based on his email and password in accounts and database
     */
    suspend fun createUser(req: AuthRequest): AuthResponse {
        val authRecord = signUpUser(req)
        val user = User(authRecord.uid, req.name, req.email)

        val (error, ref) = suspendCancellableCoroutine {
            usersRef.child(authRecord.uid).setValue(user, it.continuationCompletionListener())
        }

        if ((error?.code ?: 0) < 0) {
            throw error!!.toException()
        }

        val token = auth.createCustomToken(authRecord.uid)
        return AuthResponse(user, token)
    }

    /**
     * Send simple text message to all users
     */
    suspend fun sendTextMessage(user: User, req: SendTextMessageRequest): Message {
        val msg = Message(
            req.text, user.id,
            System.currentTimeMillis(),
            Message.Type.Message, emptyMap()
        )

        val (error, ref) = suspendCancellableCoroutine {
            msgRef.push().setValue(msg, it.continuationCompletionListener())
        }

        if ((error?.code ?: 0) < 0) {
            throw error!!.toException()
        }

        return msg.withKey(ref.key)
    }

    /**
     * Change reaction status for specific message (only one emoji per-user allowed)
     */
    suspend fun setReaction(user: User, req: SetReactionRequest) {
        val reactionsRef = msgRef.child(req.messageId).child("reactions")

        val snapshot = suspendCancellableCoroutine {
            reactionsRef.addListenerForSingleValueEvent(it.continuationValueEventListener())
        }

        val data = snapshot.getValue(reactionsTypeIndicator) ?: hashMapOf()
        val list = data[req.reaction]
        if (req.isEnabled) {
            when (list) {
                null -> data[req.reaction] = arrayListOf(user.id)
                else -> {
                    list.add(user.id)
                    data[req.reaction] = ArrayList(list.toSet())
                }
            }
        } else {
            data[req.reaction]?.remove(user.id)
        }

        val (error, _) = suspendCancellableCoroutine {
            reactionsRef.setValue(data, it.continuationCompletionListener())
        }

        if ((error?.code ?: 0) < 0) {
            throw error!!.toException()
        }
    }

    /**
     * Get user by its id
     */
    suspend fun userById(uid: String): User {
        val snapshot = suspendCancellableCoroutine {
            usersRef.child(uid).addListenerForSingleValueEvent(it.continuationValueEventListener())
        }

        return snapshot.parse<User>().withKey(uid)
    }

    /**
     * Subscribe to all events based on messages (added, changed, etc.)
     * @return disposable to clean up listener
     */
    fun messages(onMessageReceive: suspend (MessageReceive) -> Unit): Disposable {
        val listener = msgRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) = runBlocking {
                val msg = snapshot.parse<Message>().withKey(snapshot.key)
                onMessageReceive(MessageReceive(msg, FirebaseEvent.ADD, previousChildName))
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = runBlocking {
                val msg = snapshot.parse<Message>().withKey(snapshot.key)
                onMessageReceive(MessageReceive(msg, FirebaseEvent.CHANGE, previousChildName))
            }

            override fun onChildRemoved(snapshot: DataSnapshot) = runBlocking {
                val msg = snapshot.parse<Message>().withKey(snapshot.key)
                onMessageReceive(MessageReceive(msg, FirebaseEvent.REMOVE, null))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        })

        return Disposable {
            msgRef.removeEventListener(listener)
        }
    }

    /**
     * Subscribe to all events based on users (added, changed, etc.)
     * @return disposable to clean up listener
     */
    fun users(onUserReceive: suspend (UserReceive) -> Unit): Disposable {
        val listener = usersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) = runBlocking {
                val msg = snapshot.parse<User>().withKey(snapshot.key)
                onUserReceive(UserReceive(msg, FirebaseEvent.ADD, previousChildName))
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) = runBlocking {
                val msg = snapshot.parse<User>().withKey(snapshot.key)
                onUserReceive(UserReceive(msg, FirebaseEvent.CHANGE, previousChildName))
            }

            override fun onChildRemoved(snapshot: DataSnapshot) = runBlocking {
                val msg = snapshot.parse<User>().withKey(snapshot.key)
                onUserReceive(UserReceive(msg, FirebaseEvent.REMOVE, null))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) = Unit
            override fun onCancelled(error: DatabaseError) = Unit
        })

        return Disposable {
            usersRef.removeEventListener(listener)
        }
    }

    /**
     * Below are methods to simplify logic with coroutines for java-like library
     */

    enum class FirebaseEvent {
        ADD,
        CHANGE,
        REMOVE,
    }

    private fun CancellableContinuation<DataSnapshot>.continuationValueEventListener(): ValueEventListener {
        return object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) = resume(snapshot)
            override fun onCancelled(error: DatabaseError) {
                cancel(error.toException())
            }
        }
    }

    private fun CancellableContinuation<Pair<DatabaseError?, DatabaseReference>>.continuationCompletionListener(): DatabaseReference.CompletionListener {
        return DatabaseReference.CompletionListener { error, ref -> resume(error to ref) }
    }

    private inline fun <reified T> DataSnapshot.parse(): T {
        return gson.fromJson(gson.toJson(value), T::class.java)
    }

    private val reactionsTypeIndicator = object : GenericTypeIndicator<HashMap<String, ArrayList<String>>?>() {}
}
