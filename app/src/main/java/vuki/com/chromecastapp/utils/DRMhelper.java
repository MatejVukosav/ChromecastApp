package vuki.com.chromecastapp.utils;

import vuki.com.chromecastapp.BuildConfig;

/**
 * Created by mvukosav on 4.8.2016..
 */
public class DRMhelper {

    public static String getMerchant() {
        String buildType = BuildConfig.BUILD_TYPE.toLowerCase();
        buildType = buildType.replace( "debug", "" );
        buildType = buildType.replace( "release", "" );

        switch( buildType ) {
            case "dev":
                return null;
            case "preprod":
                return "aviion2";
            case "":
            case "test":
            default:
                return "aviion";
        }
    }

    public static String getEnvironment() {
        String buildType = BuildConfig.BUILD_TYPE.toLowerCase();
        buildType = buildType.replace( "debug", "" );
        buildType = buildType.replace( "release", "" );

        switch( buildType ) {
            case "dev":
                return null;
            case "test":
            case "preprod":
                return "";//DrmTodayConfiguration.DRMTODAY_STAGING;
            case "":
            default:
                return "";//DrmTodayConfiguration.DRMTODAY_PRODUCTION;
        }
    }
}