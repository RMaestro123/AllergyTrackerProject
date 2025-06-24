package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME    = "Login.db";
    private static final int    DATABASE_VERSION = 3;

    // Users columns
    private static final String TBL_USERS    = "Users";
    private static final String COL_ID       = "ID";
    private static final String COL_USERNAME = "USERNAME";
    private static final String COL_PASSWORD = "PASSWORD";
    private static final String COL_NAME     = "NAME";
    private static final String COL_ALLERGY  = "ALLERGY";
    private static final String COL_IMAGE    = "IMAGE_URI";

    // Medications columns
    private static final String TBL_MEDS     = "Medications";
    private static final String COL_MED_ID   = "ID";
    private static final String COL_ALLERGEN = "ALLERGEN";
    private static final String COL_MED_NAME = "MED_NAME";
    private static final String COL_DOSAGE   = "DOSAGE";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE " + TBL_USERS + "(" +
                        COL_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_USERNAME + " TEXT, " +
                        COL_PASSWORD + " TEXT, " +
                        COL_NAME     + " TEXT, " +
                        COL_ALLERGY  + " TEXT, " +
                        COL_IMAGE    + " TEXT" +
                        ")"
        );

        db.execSQL(
                "CREATE TABLE " + TBL_MEDS + "(" +
                        COL_MED_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COL_ALLERGEN + " TEXT, " +
                        COL_MED_NAME + " TEXT, " +
                        COL_DOSAGE   + " TEXT" +
                        ")"
        );

        seedMedications(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        if (oldV < 3) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + TBL_MEDS + "(" +
                            COL_MED_ID   + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            COL_ALLERGEN + " TEXT, " +
                            COL_MED_NAME + " TEXT, " +
                            COL_DOSAGE   + " TEXT" +
                            ")"
            );
            seedMedications(db);
        }

    }

    /** Inserts one (allergen → medication) row into Medications */
    private void insertMedication(SQLiteDatabase db, String allergen, String medName, String dosage) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ALLERGEN, allergen);
        cv.put(COL_MED_NAME, medName);
        cv.put(COL_DOSAGE, dosage);
        db.insert(TBL_MEDS, null, cv);
    }

    private void seedMedications(SQLiteDatabase db) {
        insertMedication(db, "Pollen",       "Cetirizine",            "10 mg once daily");
        insertMedication(db, "Pollen",       "Loratadine",            "10 mg once daily");
        insertMedication(db, "Dust Mites",   "Fexofenadine",          "120 mg once daily");
        insertMedication(db, "Pet Dander",   "Cetirizine",            "10 mg once daily");
        insertMedication(db, "Mold",         "Loratadine",            "10 mg once daily");
        insertMedication(db, "Ragweed",      "Cetirizine",            "10 mg once daily");
        insertMedication(db, "Grass Pollen", "Loratadine",            "10 mg once daily");
        insertMedication(db, "Tree Pollen",  "Fexofenadine",          "120 mg once daily");
        insertMedication(db, "Shellfish",    "Diphenhydramine",       "25 mg every 4–6 hours as needed");
        insertMedication(db, "Peanuts",      "Epinephrine auto-injector", "0.3 mg intramuscular as needed");
        insertMedication(db, "Milk",         "Cetirizine",            "10 mg once daily");
        insertMedication(db, "Eggs",         "Loratadine",            "10 mg once daily");
        insertMedication(db, "Soy",          "Fexofenadine",          "120 mg once daily");
        insertMedication(db, "Wheat",        "Cetirizine",            "10 mg once daily");
        insertMedication(db, "Nickel",       "Diphenhydramine",       "25 mg every 4–6 hours as needed");
        insertMedication(db, "Fragrances",   "Loratadine",            "10 mg once daily");
        insertMedication(db, "Latex",        "Diphenhydramine",       "25 mg every 4–6 hours as needed");
        insertMedication(db, "Cold Air",     "Loratadine",            "10 mg once daily");
        insertMedication(db, "Humidity",     "Cetirizine",            "10 mg once daily");
        insertMedication(db, "Red Meat",     "Epinephrine auto-injector", "0.3 mg intramuscular as needed");
        insertMedication(db, "Rain",               "Cetirizine",   "10 mg once daily in the morning");
        insertMedication(db, "Wind",               "Loratadine",   "10 mg once daily in the morning");
        insertMedication(db, "Temperature Changes","Fexofenadine", "120 mg once daily in the morning");
        insertMedication(db, "Air Pollution",      "Cetirizine",   "10 mg once daily in the morning");

    }


    public boolean insertData(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username);
        cv.put(COL_PASSWORD, password);
        long result = db.insert(TBL_USERS, null, cv);
        return result != -1;
    }

    public boolean checkUsername(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM " + TBL_USERS + " WHERE " + COL_USERNAME + " = ?",
                new String[]{username}
        );
        boolean exists = c.getCount() > 0;
        c.close();
        return exists;
    }

    @SuppressLint("Range")
    public String checkLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(
                TBL_USERS,
                new String[]{COL_USERNAME},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null
        );
        String result = null;
        if (c.moveToFirst()) {
            result = c.getString(c.getColumnIndex(COL_USERNAME));
        }
        c.close();
        return result;
    }

    @SuppressLint("Range")
    public Cursor getProfile(String username) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COL_NAME + ", " + COL_ALLERGY + ", " + COL_IMAGE +
                        " FROM " + TBL_USERS +
                        " WHERE " + COL_USERNAME + " = ?",
                new String[]{username}
        );
    }

    public boolean updateProfile(
            String username,
            String name,
            String allergy,
            String imageUri
    ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, name);
        cv.put(COL_ALLERGY, allergy);
        cv.put(COL_IMAGE, imageUri);
        int rows = db.update(
                TBL_USERS,
                cv,
                COL_USERNAME + "=?",
                new String[]{username}
        );
        return rows > 0;
    }

    public Cursor getMedicationsForAllergy(String allergen) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COL_MED_NAME + ", " + COL_DOSAGE +
                        " FROM " + TBL_MEDS +
                        " WHERE " + COL_ALLERGEN + " = ?",
                new String[]{allergen}
        );
    }
}