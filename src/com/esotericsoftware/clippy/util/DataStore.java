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
	private final String databasePath, connectionOptions;
	private boolean inMemory, socketLocking;
	private TraceLevel traceLevel = TraceLevel.OFF;
	private final ArrayList<DataStoreTable> tables = new ArrayList();

	private volatile boolean open;
	private final ArrayList<T> connections = new ArrayList();
	private final ThreadLocal<T> threadLocal = new ThreadLocal<T>() {
		protected T initialValue () {
			try {
				T conn = newConnection();
				synchronized (DataStore.this) {
					connections.add(conn);
				}
				return conn;
			} catch (SQLException ex) {
				throw new RuntimeException("Unable to obtain datastore thread connection.", ex);
			}
		}
	};

	public DataStore (String databasePath) {
		this(databasePath, "AUTO_RECONNECT=TRUE");
	}

	public DataStore (String databasePath, String connectionOptions) {
		if (databasePath == null) throw new IllegalArgumentException("databasePath cannot be null.");
		if (connectionOptions == null) throw new IllegalArgumentException("connectionOptions cannot be null.");
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException("Database driver could not be found.", ex);
		}
		this.databasePath = databasePath;
		this.connectionOptions = connectionOptions;
	}

	public void setInMemory (boolean inMemory) {
		checkClosed();
		this.inMemory = inMemory;
	}

	/** Doesn't require a watchdog thread to poll the lock file, but startup is slow. */
	public void setSocketLocking (boolean socketLocking) {
		checkClosed();
		this.socketLocking = socketLocking;
	}

	/** Sets the System.out trace level. */
	public void setTraceLevel (TraceLevel traceLevel) {
		if (traceLevel == null) throw new IllegalArgumentException("traceLevel cannot be null.");
		checkClosed();
		this.traceLevel = traceLevel;
	}

	public void addTable (DataStoreTable table) {
		if (table == null) throw new IllegalArgumentException("table cannot be null.");
		checkClosed();
		tables.add(table);
	}

	/** Opens the DataStore, creating the tables if they do not exist. */
	public synchronized void open () throws SQLException {
		checkClosed();
		try (Connection conn = openConnection()) {
			for (DataStoreTable table : tables)
				table.open(conn);
		}
		open = true;
	}

	/** Returns a new open connection to the database that backs this DataStore. */
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

	/** Releases all resources associated with this DataStore. Afterward, all connection objects will fail if an attempt is made to
	 * use them. */
	public synchronized void close () throws SQLException {
		if (!open) return;
		if (DEBUG) debug("Closing data store: " + this);
		open = false;
		for (T conn : connections)
			conn.close();
	}

	/** Returns a connection specifically for use only by the calling thread. The caller is responsible for closing the connection
	 * when no longer needed. */
	public T getThreadConnection () throws SQLException {
		checkOpen();
		return threadLocal.get();
	}

	void checkClosed () {
		if (open) throw new IllegalStateException("DataStore must be closed.");
	}

	void checkOpen () {
		if (!open) throw new IllegalStateException("DataStore must be open.");
	}

	public String getDatabasePath () {
		return databasePath;
	}

	public String toString () {
		return databasePath;
	}

	abstract protected T newConnection () throws SQLException;

	static public class DataStoreTable {
		private final String name;
		private final ArrayList<String> columns = new ArrayList();
		private final ArrayList<List<String>> indexes = new ArrayList();
		private final ArrayList<List<String>> fulltextIndexes = new ArrayList();
		private boolean lucene, isNew;
		private PreparedStatement getCount, clear;

		public DataStoreTable (String name) {
			if (name == null) throw new IllegalArgumentException("name cannot be null.");
			this.name = name;
		}

		public void addColumn (String column) {
			if (column == null) throw new IllegalArgumentException("column cannot be null.");
			columns.add(column);
		}

		public void addIndex (String... columnNames) {
			indexes.add(Arrays.asList(columnNames));
		}

		public void addFulltextIndex (String... columnNames) {
			fulltextIndexes.add(Arrays.asList(columnNames));
		}

		public void setFulltextLucene (boolean lucene) {
			this.lucene = lucene;
		}

		public String getName () {
			return name;
		}

		void open (Connection conn) throws SQLException {
			Statement stmt = conn.createStatement();
			try {
				stmt.execute(sql("SELECT 1 FROM :table:"));
				if (TRACE) trace("Data store exists: " + this);
			} catch (SQLException ex) {
				isNew = true;
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

			// Creates the indexes.
			StringBuffer buffer = new StringBuffer(100);
			if (indexes.size() > 0) {
				if (TRACE) trace("Creating indexes: " + this + ", " + indexes);
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
					FullTextLucene.init(conn);
				else
					FullText.init(conn);
				for (int i = 0, n = fulltextIndexes.size(); i < n; i++) {
					List<String> columnNames = fulltextIndexes.get(i);
					buffer.setLength(0);
					for (int ii = 0, nn = columnNames.size(); ii < nn; ii++) {
						if (ii > 0) buffer.append(',');
						buffer.append(columnNames.get(ii));
					}
					if (lucene)
						FullTextLucene.createIndex(conn, "PUBLIC", getName(), buffer.toString());
					else
						FullText.createIndex(conn, "PUBLIC", getName(), buffer.toString());
				}
				// Example fulltext query:
				// SELECT T.* FROM FT_SEARCH_DATA(?, 0, 0) FT, table T WHERE FT.TABLE='table' AND T.id=FT.KEYS[0]
				// FullText.searchData(conn, text, limit, offset);
			}

			stmt.close();
		}

		/** Returns true if the table did not exist before this DataStore was opened. */
		public boolean isNew () {
			return isNew;
		}

		/** Returns the specified SQL with special tokens in it replaced. Eg, ":table:" is replaced with the name of the database
		 * table backing this DataStore. */
		public String sql (String sql) {
			return sql.replace(":table:", getName());
		}

		/** Empties the data store. Can only be called after open is called. */
		public void clear (DataStoreConnection conn) throws SQLException {
			if (clear == null) clear = conn.prepareStatement("DELETE FROM :table:");
			conn.update(clear);
		}

		public int getCount (DataStoreConnection conn) throws SQLException {
			if (getCount == null) getCount = conn.prepareStatement("SELECT COUNT(*) FROM :table:");
			return conn.queryInt(getCount);
		}

		public String toString () {
			return name;
		}
	}

	/** Represents a connection to a DataStore. Each thread accessing a DataStore must do so through its own DataStoreConnection
	 * and it must be closed when no longer needed. */
	static public class DataStoreConnection implements AutoCloseable {
		public final DataStore store;
		public final Connection conn;
		private final Statement stmt;
		private final Thread thread;

		protected DataStoreConnection (DataStoreConnection delegate) throws SQLException {
			store = delegate.store;
			conn = delegate.conn;
			stmt = delegate.stmt;
			thread = delegate.thread;
		}

		public DataStoreConnection (DataStore store) throws SQLException {
			this.store = store;
			conn = store.openConnection();
			stmt = conn.createStatement();
			thread = Thread.currentThread();
		}

		public PreparedStatement prepareStatement (String sql) throws SQLException {
			return prepareStatement(sql, false);
		}

		public PreparedStatement prepareStatement (String sql, boolean generatedKeys) throws SQLException {
			checkThread();
			return conn.prepareStatement(sql, generatedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
		}

		public boolean execute (String sql) throws SQLException {
			checkThread();
			return stmt.execute(sql);
		}

		public ResultSet query (String sql) throws SQLException {
			checkThread();
			return stmt.executeQuery(sql);
		}

		public int getGeneratedKey (PreparedStatement stmt) throws SQLException {
			checkThread();
			try (ResultSet set = stmt.getGeneratedKeys()) {
				if (set.next()) return set.getInt(1);
				throw new SQLException("Failed to get generated key.");
			}
		}

		public int update (PreparedStatement stmt) throws SQLException {
			checkThread();
			return stmt.executeUpdate();
		}

		public ResultSet query (PreparedStatement stmt) throws SQLException {
			checkThread();
			return stmt.executeQuery();
		}

		public int queryInt (PreparedStatement stmt) throws SQLException {
			checkThread();
			try (ResultSet set = stmt.executeQuery()) {
				if (!set.next()) throw new SQLException("No rows returned.");
				return set.getInt(1);
			}
		}

		public int queryInt (PreparedStatement stmt, int defaultValue) throws SQLException {
			checkThread();
			try (ResultSet set = stmt.executeQuery()) {
				return set.next() ? set.getInt(1) : defaultValue;
			}
		}

		public String queryString (PreparedStatement stmt) throws SQLException {
			checkThread();
			try (ResultSet set = stmt.executeQuery()) {
				if (!set.next()) throw new SQLException("No rows returned.");
				return set.getString(1);
			}
		}

		public String queryString (PreparedStatement stmt, String defaultValue) throws SQLException {
			checkThread();
			try (ResultSet set = stmt.executeQuery()) {
				return set.next() ? set.getString(1) : defaultValue;
			}
		}

		/** Releases resources associated with this connection. */
		public void close () throws SQLException {
			checkThread();
			stmt.close();
			conn.close();
			synchronized (store) {
				store.connections.remove(this);
			}
		}

		/** Returns the connection to the database for this DataStoreConnection. */
		public Connection getConnection () {
			checkThread();
			return conn;
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
