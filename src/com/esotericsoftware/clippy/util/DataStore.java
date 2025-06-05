/* Copyright (c) 2014-2025, Esoteric Software
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.esotericsoftware.clippy.util;

import static com.esotericsoftware.minlog.Log.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.h2.fulltext.FullText;
import org.h2.fulltext.FullTextLucene;

/** Create a table in a local database and access it in a thread safe manner.
 * @author Nathan Sweet */
public abstract class DataStore<T extends DataStore.DataStoreConnection> {
	private final String databasePath, tableName, connectionOptions;
	private boolean inMemory, socketLocking;
	private TraceLevel traceLevel = TraceLevel.OFF;
	private final ArrayList<String> columns = new ArrayList();
	private final ArrayList<List<String>> indexes = new ArrayList();
	private final ArrayList<List<String>> fulltextIndexes = new ArrayList();
	private boolean lucene;
	private Connection defaultConn;
	private boolean newTable, indexesCreated;
	private ThreadLocal<T> threadConnections;

	public DataStore (String databasePath, String tableName) {
		this(databasePath, tableName, "AUTO_RECONNECT=TRUE");
	}

	public DataStore (String databasePath, String tableName, String connectionOptions) {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Database driver could not be found.", ex);
		}
		this.databasePath = databasePath;
		this.tableName = tableName;
		this.connectionOptions = connectionOptions;
	}

	public void setInMemory (boolean inMemory) {
		this.inMemory = inMemory;
	}

	/** Doesn't require a watchdog thread to poll the lock file, but startup is slow. */
	public void setSocketLocking (boolean socketLocking) {
		this.socketLocking = socketLocking;
	}

	/** Sets the System.out trace level. */
	public void setTraceLevel (TraceLevel traceLevel) {
		this.traceLevel = traceLevel;
	}

	/** Adds a column to the database table backing this data store. Can only be called before open is called. */
	public void addColumn (String column) {
		if (column == null) throw new IllegalArgumentException("column cannot be null.");
		if (defaultConn != null) throw new IllegalStateException("DataStore has already been opened.");
		columns.add(column);
	}

	public void addIndex (String... columnNames) {
		if (indexesCreated) throw new IllegalStateException("Indexes have already been created.");
		indexes.add(Arrays.asList(columnNames));
	}

	public void addFulltextIndex (String... columnNames) {
		if (indexesCreated) throw new IllegalStateException("Indexes have already been created.");
		fulltextIndexes.add(Arrays.asList(columnNames));
	}

	public void setFulltextLucene (boolean lucene) {
		this.lucene = lucene;
	}

	/** Gets the name of the database table backing this DataStore. */
	public String getTableName () {
		return tableName.toUpperCase();
	}

	/** Returns the specified SQL with special tokens in it replaced. Eg, ":table:" is replaced with the name of the database table
	 * backing this DataStore. */
	public String sql (String sql) {
		return sql.replace(":table:", getTableName());
	}

	/** Opens the DataStore, creating the table and indexes if they do not exist. */
	public void open () throws SQLException {
		if (defaultConn != null) throw new IllegalStateException("DataStore has already been opened.");
		defaultConn = openConnection();

		Statement stmt = defaultConn.createStatement();
		try {
			stmt.execute(sql("SELECT 1 FROM :table:"));
			if (TRACE) trace("Data store exists: " + this);
		} catch (SQLException ex) {
			newTable = true;
			if (DEBUG) debug("Creating data store: " + this + ", " + columns);

			StringBuffer buffer = new StringBuffer(100);
			buffer.append("CREATE TABLE IF NOT EXISTS :table: (");
			int i = 0;
			for (String column : columns) {
				if (i++ > 0) buffer.append(',');
				buffer.append(column);
			}
			buffer.append(')');
			stmt.execute(sql(buffer.toString()));
		}
		stmt.close();

		threadConnections = new ThreadLocal<T>() {
			protected T initialValue () {
				try {
					return newConnection();
				} catch (SQLException ex) {
					throw new RuntimeException("Unable to obtain datastore thread connection.", ex);
				}
			}
		};
	}

	/** Returns a new open connection to the database that backs all DataStores. */
	protected Connection openConnection () throws SQLException {
		String url;
		File lockFile = null;
		if (inMemory)
			url = "jdbc:h2:mem:" + databasePath;
		else {
			url = "jdbc:h2:file:" + databasePath;
			if (socketLocking) url += ";FILE_LOCK=SOCKET";

			if (databasePath.startsWith("~"))
				lockFile = new File(System.getProperty("user.home"), databasePath.substring(1) + ".lock.db");
			else
				lockFile = new File(databasePath + ".lock.db");
			lockFile.delete(); // Try to delete orphaned database lock file.
		}
		if (traceLevel != TraceLevel.OFF) url += ";TRACE_LEVEL_SYSTEM_OUT=" + traceLevel.ordinal();
		url += ";" + connectionOptions;
		if (TRACE) trace("Opening data store connection: " + url);
		try {
			return DriverManager.getConnection(url);
		} catch (Throwable ex) {
			String message = "";
			if (lockFile != null && !lockFile.exists()) message = "\nTry deleting lock file: " + lockFile.getAbsolutePath();
			throw new RuntimeException("Error opening datastore: " + url + message, ex);
		}
	}

	/** Creates the indexes for the database table. Can only be called after open is called. */
	public void createIndexes () throws SQLException {
		if (defaultConn == null) throw new IllegalStateException("DataStore has not been opened.");
		if (indexesCreated) throw new IllegalStateException("Indexes have already been created.");

		StringBuffer buffer = new StringBuffer(100);

		if (indexes.size() > 0) {
			if (TRACE) trace("Creating indexes: " + this + ", " + indexes);
			Statement stmt = defaultConn.createStatement();
			for (int i = 0, n = indexes.size(); i < n; i++) {
				List<String> columnNames = indexes.get(i);
				buffer.setLength(0);
				buffer.append("CREATE INDEX IF NOT EXISTS ix_:table:");
				for (int ii = 0, nn = columnNames.size(); ii < nn; ii++) {
					buffer.append('_');
					buffer.append(columnNames.get(ii).replace(" ", "_"));
				}
				buffer.append(" ON :table: (");
				for (int ii = 0, nn = columnNames.size(); ii < nn; ii++) {
					if (ii > 0) buffer.append(',');
					buffer.append(columnNames.get(ii));
				}
				buffer.append(")");
				stmt.executeUpdate(sql(buffer.toString()));
			}
			stmt.close();
		}

		if (fulltextIndexes.size() > 0) {
			if (TRACE) trace("Creating fulltext indexes: " + this + ", " + fulltextIndexes);
			if (lucene)
				FullTextLucene.init(defaultConn);
			else
				FullText.init(defaultConn);
			for (int i = 0, n = fulltextIndexes.size(); i < n; i++) {
				List<String> columnNames = fulltextIndexes.get(i);
				buffer.setLength(0);
				for (int ii = 0, nn = columnNames.size(); ii < nn; ii++) {
					if (ii > 0) buffer.append(',');
					buffer.append(columnNames.get(ii).toUpperCase());
				}
				if (lucene)
					FullTextLucene.createIndex(defaultConn, "PUBLIC", getTableName(), buffer.toString());
				else
					FullText.createIndex(defaultConn, "PUBLIC", getTableName(), buffer.toString());
			}
			// Example fulltext query:
			// SELECT T.* FROM FT_SEARCH_DATA(?, 0, 0) FT, table T WHERE FT.TABLE='table' AND T.id=FT.KEYS[0]
			// FullText.searchData(conn, text, limit, offset);
		}

		indexesCreated = true;
	}

	/** Releases all resources associated with this DataStore. Afterward, all connection objects will fail if an attempt is made to
	 * use them. */
	public synchronized void close () throws SQLException {
		if (defaultConn == null) return;
		if (DEBUG) debug("Closing data store: " + this);
		if (!defaultConn.isClosed()) defaultConn.close();
		defaultConn = null;
		if (threadConnections != null) {
			threadConnections.remove();
			threadConnections = null;
		}
	}

	/** Returns a connection specifically for use only by the calling thread. The caller is responsible for closing the connection
	 * when no longer needed. */
	public T getThreadConnection () throws SQLException {
		if (defaultConn == null) throw new IllegalStateException("DataStore has not been opened.");
		System.out.println(Thread.currentThread().getName());
		return threadConnections.get();
	}

	/** Returns true if the table did not existed before this DataStore was opened. */
	public boolean isNewTable () {
		return newTable;
	}

	public String toString () {
		return databasePath + "/" + tableName;
	}

	abstract protected T newConnection () throws SQLException;

	/** Represents a connection to a DataStore. Each thread accessing a DataStore must do so through its own DataStoreConnection
	 * and it must be closed when no longer needed. */
	public class DataStoreConnection {
		private final Thread thread = Thread.currentThread();
		public final Connection conn;
		private final Statement stmt;
		private PreparedStatement getCount, clear;

		public DataStoreConnection () throws SQLException {
			conn = openConnection();
			stmt = conn.createStatement();
		}

		/** Tokens in the SQL are replaced using {@link DataStore#sql(String)}. */
		public PreparedStatement prepareStatement (String sql) throws SQLException {
			return prepareStatement(sql, false);
		}

		/** Tokens in the SQL are replaced using {@link DataStore#sql(String)}. */
		public PreparedStatement prepareStatement (String sql, boolean generatedKeys) throws SQLException {
			checkThread();
			return conn.prepareStatement(sql(sql), generatedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
		}

		/** Tokens in the SQL are replaced using {@link DataStore#sql(String)}. */
		public boolean execute (String sql) throws SQLException {
			checkThread();
			return stmt.execute(sql(sql));
		}

		/** Tokens in the SQL are replaced using {@link DataStore#sql(String)}. */
		public ResultSet executeQuery (String sql) throws SQLException {
			checkThread();
			return stmt.executeQuery(sql(sql));
		}

		public void executeUpdate (PreparedStatement stmt) throws SQLException {
			checkThread();
			stmt.executeUpdate();
		}

		public ResultSet executeQuery (PreparedStatement stmt) throws SQLException {
			checkThread();
			return stmt.executeQuery();
		}

		/** Releases resources associated with this connection. */
		public void close () throws SQLException {
			checkThread();
			stmt.close();
			conn.close();
			threadConnections.remove();
		}

		/** Returns the connection to the database for this DataStoreConnection. */
		public Connection getConnection () {
			checkThread();
			return conn;
		}

		/** Empties the data store. Can only be called after open is called. */
		public void clear () throws SQLException {
			checkThread();
			if (clear == null) clear = prepareStatement("DELETE FROM :table:");
			clear.execute();
		}

		public int getCount () throws SQLException {
			checkThread();
			if (getCount == null) getCount = prepareStatement("SELECT COUNT(*) FROM :table:");
			ResultSet set = getCount.executeQuery();
			if (!set.next()) return 0;
			return set.getInt(1);
		}

		private void checkThread () {
			if (Thread.currentThread() != thread)
				throw new RuntimeException("Wrong thread: " + Thread.currentThread().getName() + " != " + thread.getName());
		}
	}

	public enum TraceLevel {
		OFF, ERROR, INFO, DEBUG, SLF4J
	}
}
