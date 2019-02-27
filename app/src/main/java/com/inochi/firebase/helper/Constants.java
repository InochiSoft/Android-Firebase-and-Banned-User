package com.inochi.firebase.helper;

public final class Constants {
    public static final class Default {
        static final String APP_SLUG = "com.inochi.firebase";
    }

    public static final class Permission {
        public static final int SIGN_IN = 1001;
        public static final int READ_STORAGE = 1002;
        public static final int WRITE_STORAGE = 1003;
    }

    public static final class User {
        public static final class Type {
            public static final int GENERAL = 0;
            public static final int MEMBER = 1;
            public static final int ADMINISTRATOR = 9;
            public static final int BAN = -1;
        }
    }

    static final class Setting {
        static final class Key {
            static final String LAST_USER = Default.APP_SLUG + ".LAST_USER";
        }
    }

    public static final class Firestore {
        public static final class Collection {
            public static final String USER = "user";
        }
        public static final class Data {
            public static final int CREATE_USER = 101;
            public static final int GET_USER = 102;
        }
    }
}
