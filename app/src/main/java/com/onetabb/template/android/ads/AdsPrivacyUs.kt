package com.onetabb.template.android.ads

import android.content.Context
import com.adshub.monetization.AdLogger
import com.applovin.sdk.AppLovinPrivacySettings
import com.facebook.ads.AdSettings
import com.google.ads.mediation.pangle.PangleMediationAdapter
import com.iab.gpp.encoder.GppModel
import com.mbridge.msdk.out.MBridgeSDKFactory
import com.unity3d.ads.metadata.MetaData
import com.unity3d.ironsourceads.IronSourceAds

/**
 * CCPA / CPRA + luat privacy cac bang Hoa Ky — forward tin hieu "do not sell" sang mediation.
 *
 * Tai sao phai co file nay: AdsHub 0.0.1 co san hook
 * `CmpManager.forwardConsentToMediationNetworks(Context, Boolean)` nhung THAN HAM RONG
 * (javap: `0: return`), nen khong mang nao nhan duoc tin hieu. Khi nao 1Tabb implement hook
 * do thi xoa file nay + xoa dong goi trong AdsBootstrap.
 *
 * Khong mang nao trong 6 mang tu doc IABGPP_* / IABUSPrivacy_* tu SharedPreferences
 * (da grep bytecode: chi co IABTCF_* cho GDPR) -> bat buoc goi API rieng tung mang.
 * Rieng COPPA thi KHONG can file nay: 5/6 adapter tu doc TagForChildDirectedTreatment
 * cua GMA (tru Meta).
 *
 * BAT BUOC goi TRUOC `AdNetworkInitializer.initNetworks()` — Unity / Mintegral / ironSource
 * chi doc metadata tai thoi diem init, set sau la mat tac dung.
 */
object AdsPrivacyUs {

    private const val TAG = "AdsPrivacyUs"

    /** Key duy nhat UMP 4.0.0 ghi ra sau khi user dong form "US states" (kem IABGPP_GppSID). */
    private const val KEY_GPP_STRING = "IABGPP_HDR_GppString"

    /** Gia tri field opt-out theo GPP spec: 0 = N/A, 1 = Opted Out, 2 = Did Not Opt Out. */
    private const val VALUE_OPTED_OUT = 1

    /** 16 section US cua GPP spec (usnat = quoc gia, con lai la tung bang). */
    private val US_SECTIONS = listOf(
        "usnat", "usca", "usva", "usco", "usut", "usct", "usfl", "usmt",
        "usor", "ustx", "usde", "usia", "usne", "usnh", "usnj", "ustn",
    )

    /**
     * Ba field deu la ly do phai han che ad ca nhan hoa. Moi section co bo field khac nhau
     * (vd usca khong co TargetedAdvertisingOptOut) nen phai guard bang hasField().
     */
    private val OPT_OUT_FIELDS = listOf(
        "SaleOptOut",
        "SharingOptOut",
        "TargetedAdvertisingOptOut",
    )

    fun apply(context: Context) {
        if (!isUsSaleOptOut(context)) return

        // Tung mang boc runCatching rieng: mot mang nem exception khong duoc keo do ca chuoi.
        runCatching { AppLovinPrivacySettings.setDoNotSell(true) }
            .onFailure { AdLogger.i(TAG, "AppLovin setDoNotSell that bai: ${it.message}") }

        // LDU = Limited Data Use. (0, 0) = de Meta tu suy ra bang tu IP.
        runCatching { AdSettings.setDataProcessingOptions(arrayOf("LDU"), 0, 0) }
            .onFailure { AdLogger.i(TAG, "Meta setDataProcessingOptions that bai: ${it.message}") }

        runCatching { MBridgeSDKFactory.getMBridgeSDK().setDoNotTrackStatus(context, true) }
            .onFailure { AdLogger.i(TAG, "Mintegral setDoNotTrackStatus that bai: ${it.message}") }

        // 0 = khong dong y quang cao ca nhan hoa. Pangle gop GDPR/CCPA/COPPA vao mot co nay.
        runCatching { PangleMediationAdapter.setPAConsent(0) }
            .onFailure { AdLogger.i(TAG, "Pangle setPAConsent that bai: ${it.message}") }

        // Adapter AdMob 9.6.0.1 init qua com.unity3d.ironsourceads.IronSourceAds,
        // KHONG phai com.ironsource.mediationsdk.IronSource (class do khong co setMetaData).
        runCatching { IronSourceAds.setMetaData("do_not_sell", "true") }
            .onFailure { AdLogger.i(TAG, "ironSource setMetaData that bai: ${it.message}") }
        AdLogger.i(TAG, "Da forward do-not-sell sang 6 mang mediation")
    }

    /**
     * Doc lua chon cua user tu chuoi GPP (Global Privacy Platform) do UMP ghi ra.
     *
     * Doc Google (developers.google.com/admob/android/privacy/us-iab-support):
     * > "the UMP SDK only populates the IABGPP_GppSID and IABGPP_HDR_GppString keys"
     *
     * Tuc la UMP KHONG BAO GIO ghi IABUSPrivacy_String (chuoi "1YNN" doi cu cua CCPA
     * framework — IAB da thay bang GPP). Doc key do la luon nhan null -> code chet.
     *
     * GPP string = header + cac section, moi section base64url, noi bang '~'. Tu parse bang tay
     * rat de sai vi 16 section US co thu tu field khac nhau, nen dung thang decoder chinh chu
     * cua IAB Tech Lab (com.iabgpp:iabgpp-encoder, Apache 2.0, github.com/IABTechLab/iabgpp-java).
     *
     * Khong can doc IABGPP_GppSID: ban than GPP string da chua cac section, [GppModel.hasSection]
     * tra loi duoc roi.
     */
    private fun isUsSaleOptOut(context: Context): Boolean {
        // UMP ghi vao default SharedPreferences cua app, khong phai file rieng.
        val prefs = context.getSharedPreferences(
            "${context.packageName}_preferences",
            Context.MODE_PRIVATE,
        )
        val gppString = runCatching { prefs.getString(KEY_GPP_STRING, null) }.getOrNull()
        if (gppString.isNullOrBlank()) return false

        val gpp = runCatching { GppModel(gppString) }.getOrElse {
            AdLogger.i(TAG, "Khong parse duoc GPP string: ${it.message}")
            return false
        }

        return US_SECTIONS.any { section ->
            gpp.hasSection(section) && OPT_OUT_FIELDS.any { field ->
                gpp.hasField(section, field) &&
                    (gpp.getFieldValue(section, field) as? Int) == VALUE_OPTED_OUT
            }
        }
    }
}
