package com.masum.lantern.data.permission

import android.content.Intent

class VpnConsentRequiredException(val consentIntent: Intent) : Exception("VPN consent required")
