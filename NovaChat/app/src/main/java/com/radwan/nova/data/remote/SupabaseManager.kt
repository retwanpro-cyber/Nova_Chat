package com.radwan.nova.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

object SupabaseManager {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://zvgcorxwppabdpclkolw.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp2Z2Nvcnh3cHBhYmRwY2xrb2x3Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODc0MzQyNjAsImV4cCI6MjEwMzAxMDI2MH0.UFqDrOTk2nL--ob8RN5Whuzkw2eN1-GkB6lri7XwB2o"
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }

    val auth: Auth = client.auth
    val postgrest: Postgrest = client.postgrest
    val realtime: Realtime = client.realtime
    val storage: Storage = client.storage
}
