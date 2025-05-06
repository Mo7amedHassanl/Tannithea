package com.m7md7sn.tannithea.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.m7md7sn.tannithea.data.model.FirebaseSensorData
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSensorRepository @Inject constructor() {
    private val database = FirebaseDatabase.getInstance()
    private val sensorDataRef = database.getReference("sensor_data")
    private val pumpsRef = database.getReference("pumps")
    private val scheduleControlRef = database.getReference("schedule_control")
    
    /**
     * Get a flow of the latest sensor data from Firebase
     */
    fun getLatestSensorData(): Flow<Map<String, FirebaseSensorData>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorDataMap = mutableMapOf<String, FirebaseSensorData>()
                
                for (childSnapshot in snapshot.children) {
                    val key = childSnapshot.key ?: continue
                    val sensorData = childSnapshot.getValue(FirebaseSensorData::class.java) ?: continue
                    sensorDataMap[key] = sensorData
                }
                
                trySend(sensorDataMap).isSuccess
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        sensorDataRef.addValueEventListener(valueEventListener)
        
        // Remove listener when flow collection ends
        awaitClose {
            sensorDataRef.removeEventListener(valueEventListener)
        }
    }
    
    /**
     * Get a flow of sensor data history for a specific sensor
     */
    fun getSensorHistory(): Flow<List<FirebaseSensorData>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sensorDataList = mutableListOf<FirebaseSensorData>()
                
                for (childSnapshot in snapshot.children) {
                    val sensorData = childSnapshot.getValue(FirebaseSensorData::class.java) ?: continue
                    sensorDataList.add(sensorData)
                }
                
                trySend(sensorDataList.sortedBy { it.timestamp }).isSuccess
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        // Limit to last 50 readings
        sensorDataRef.limitToLast(50).addValueEventListener(valueEventListener)
        
        // Remove listener when flow collection ends
        awaitClose {
            sensorDataRef.removeEventListener(valueEventListener)
        }
    }

    /**
     * Get a flow of the latest pump states from Firebase
     */
    fun getPumpStatesFlow(): Flow<List<Boolean>> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pumpStates = MutableList(7) { false }
                for (i in 0..6) {
                    val value = snapshot.child(i.toString()).getValue(Int::class.java) ?: 0
                    pumpStates[i] = value == 1
                }
                trySend(pumpStates).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        pumpsRef.addValueEventListener(valueEventListener)
        awaitClose { pumpsRef.removeEventListener(valueEventListener) }
    }

    /**
     * Set the state of a specific pump in Firebase
     */
    fun setPumpState(index: Int, state: Boolean) {
        pumpsRef.child(index.toString()).setValue(if (state) 1 else 0)
    }

    /**
     * Set all pumps to a specific state in Firebase
     */
    fun setAllPumps(state: Boolean) {
        val updates = (0..6).associate { it.toString() to if (state) 1 else 0 }
        pumpsRef.updateChildren(updates)
    }

    /**
     * Get a flow of the current schedule status from Firebase
     */
    fun getScheduleStatusFlow(): Flow<String> = callbackFlow {
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val status = snapshot.child("status").getValue(String::class.java) ?: "stopped"
                trySend(status).isSuccess
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        scheduleControlRef.addValueEventListener(valueEventListener)
        awaitClose { scheduleControlRef.removeEventListener(valueEventListener) }
    }

    /**
     * Send a schedule control command to Firebase
     */
    fun setScheduleCommand(command: String) {
        val timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        
        val status = when (command) {
            "start" -> "running"
            "stop" -> "stopped"
            "pause" -> "paused"
            "resume" -> "running"
            else -> "unknown"
        }
        
        val updates = mapOf(
            "command" to command,
            "status" to status,
            "last_updated" to timestamp
        )
        
        scheduleControlRef.updateChildren(updates)
    }
} 