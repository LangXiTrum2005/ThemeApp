package com.wavez.controllerwidget.data.local

object ThemeStorageConfig {
    const val DATABASE_NAME = "controller_widget.db"
    const val DOWNLOADED_THEME_TABLE = "downloaded_controller"
    const val DOWNLOADED_P67_THEME_TABLE = "downloaded_theme"
    const val THEME_BASE_DIR = "theme"
    const val THEME_ARCHIVE_DIR = "archives"
    const val THEME_EXTRACTED_DIR = "extracted"
    const val THEME_TEMP_SUFFIX = ".tmp"
    const val DEFAULT_THEME_ARCHIVE_EXTENSION = "zip"
    const val DEFAULT_THEME_NAME = "theme_1"
}

object WallpaperStorageConfig {
    const val DOWNLOADED_WALLPAPER_TABLE = "downloaded_wallpaper"
    const val WALLPAPER_BASE_DIR = "wallpaper"
    const val WALLPAPER_IMAGES_DIR = "images"
    const val WALLPAPER_TEMP_SUFFIX = ".tmp"
    const val DEFAULT_WALLPAPER_EXTENSION = "png"
}
