package dev.braian.goalbit.constants

class DataBaseConstants private constructor() {

    object HABIT {
        const val ID = "id"
        const val NAME = "name"
    }

    object TAG {
        const val NOTIFICATION_HELPER = "NotificationHelper"
        const val HABIT_REMINDER_RECEIVE = "HabitReminderReceive"
        const val HABIT_VIEW_MODEL = "HabitViewModel"
        const val HABIT_DETAILS_ACTIVITY = "HabitDetailsActivity"
        const val HABIT_HISTORIC_VIEW_HOLDER = "HabitHistoricViewHolder"
        const val HABIT_VIEW_HOLDER = "HabitViewHolder"
        const val ADD_HABIT_FORM_ACTIVITY = "AddHabitFormActivity"
        const val HOME_FRAGMENT = "HomeFragment"
        const val MAIN_ACTIVITY = "MainActivity"
        const val ICON_DIALOG_TAG = "icon-dialog"
        const val FIREBASE_AUTH_MANAGER = "FirebaseAuthManager"
        const val ME_FRAGMENT = "MeFragment"
        const val LOGIN_FRAGMENT = "LoginFragment"
    }

    object NOTIFICATION {
        const val habitName = "habitName"
        const val habitId = "habitId"
        const val notificationID = 1
        const val channelID = "channel1"
        const val channelName = "channelName"
        const val titleExtra = "titleExtra"
        const val messageExtra = "messageExtra"
        const val HOUR = "hour"
        const val MINUTE = "minute"
    }

    object GOOGLE {
        const val RC_SIGN_IN = 20
    }
}