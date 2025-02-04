package com.softylasa.smartengineer.xyz

import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

fun createInsecureHttpClient(): OkHttpClient {
    val trustAllCertificates = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

        override fun checkClientTrusted(certificates: Array<X509Certificate>, authType: String) {}

        override fun checkServerTrusted(certificates: Array<X509Certificate>, authType: String) {}
    })

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCertificates, java.security.SecureRandom())
    val sslSocketFactory = sslContext.socketFactory

    return OkHttpClient.Builder()
        .sslSocketFactory(sslSocketFactory, trustAllCertificates[0] as X509TrustManager)
        .hostnameVerifier { _, _ -> true } // Ignore hostname verification
        .build()
}
