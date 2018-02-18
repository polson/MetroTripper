package com.philsoft.metrotripper.database;

import com.philsoft.metrotripper.database.contracts.StopContract;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SchemaBuilder {

	public static LinkedHashMap<String, String> buildCreateTableSql() {

		LinkedHashMap<String, String> tableNamesToCreateStatements = new LinkedHashMap<String, String>();

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE " + StopContract.TABLE_NAME + " (");
		sqlBuilder.append(StopContract.STOP_ID + " INTEGER NOT NULL PRIMARY KEY, ");
		sqlBuilder.append(StopContract.STOP_NAME + " VARCHAR(49), ");
		sqlBuilder.append(StopContract.STOP_DESC + " VARCHAR(13), ");
		sqlBuilder.append(StopContract.STOP_LAT + " NUMERIC(11,6), ");
		sqlBuilder.append(StopContract.STOP_LON + " NUMERIC(11,6), ");
		sqlBuilder.append(StopContract.WHEELCHAIR_BOARDING + " INTEGER(1), ");
		sqlBuilder.append(StopContract.STOP_URL + " VARCHAR(62) ");
		sqlBuilder.append(");\n");
		tableNamesToCreateStatements.put(StopContract.TABLE_NAME, sqlBuilder.toString());
		return tableNamesToCreateStatements;
	}

	public static List<String> buildCreateIndexSql() {

		List<String> indexCreateStatements = new ArrayList<String>();
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE INDEX S1 ON " + StopContract.TABLE_NAME + " (");
		sqlBuilder.append(StopContract.STOP_ID + ")");
		indexCreateStatements.add(sqlBuilder.toString());
		return indexCreateStatements;
	}
}
