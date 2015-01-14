package com.philsoft.metrotripper.database;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.noveogroup.android.log.Logger;
import com.noveogroup.android.log.LoggerManager;

import java.io.File;

public class DatabaseHelper extends SQLiteOpenHelper {

	private final Logger log = LoggerManager.getLogger(getClass());

	public static final String DATABASE_NAME = "metrotripper.db";
	public static final int DATABASE_VERSION = 1;

	public static File DB_FILE = null;
	private static DatabaseHelper instance = null;

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		setDbPath(db);
		log.d("Creating tables in database: " + DATABASE_NAME);
		for (String statement : SchemaBuilder.buildCreateTableSql().values()) {
			db.execSQL(statement);
		}

		log.d("Creating indexes in database:" + DATABASE_NAME);
		for (String statement : SchemaBuilder.buildCreateIndexSql()) {
			db.execSQL(statement);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		setDbPath(db);
	}

	@TargetApi(16)
	public void onConfigure(SQLiteDatabase db) {
		setWriteAheadLoggingEnabled(true);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		setDbPath(db);
		super.onOpen(db);
	}

	public static DatabaseHelper getInstance(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context.getApplicationContext());
		}
		return instance;
	}

	private void setDbPath(SQLiteDatabase db) {
		if (db.getPath() == null) {
			DB_FILE = new File(db.getPath());
		}
	}
}
