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

package com.esotericsoftware.clippy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.esotericsoftware.clippy.util.DataStore;

/** @author Nathan Sweet */
public class ClipDataStore extends DataStore<ClipDataStore.ClipConnection> {
	static public final int maxSnipSize = 2048;

	public ClipDataStore () throws SQLException {
		super("~/.clippy/db/db");
		// setTraceLevel(TraceLevel.DEBUG);
		if (System.getProperty("dev") != null)
			setInMemory(true);
		else
			setSocketLocking(true);

		DataStoreTable table = new DataStoreTable("clips");
		table.addColumn("id INTEGER AUTO_INCREMENT");
		table.addColumn("text VARCHAR_IGNORECASE NOT NULL");
		table.addColumn("snip VARCHAR_IGNORECASE(" + maxSnipSize + ") NOT NULL");
		table.addIndex("id DESC");
		table.addIndex("snip");
		table.addIndex("text");
		addTable(table);

		open();
	}

	protected Connection openConnection () throws SQLException {
		try {
			return super.openConnection();
		} catch (Throwable ex1) {
			setSocketLocking(false); // Can cause DB open failure if computer name is Unicode.
			try {
				return super.openConnection();
			} catch (Throwable ex2) {
				throw ex1;
			}
		}
	}

	protected ClipConnection newConnection () throws SQLException {
		return new ClipConnection();
	}

	public final class ClipConnection extends DataStore.DataStoreConnection {
		private final PreparedStatement add, removeText, removeID, searchRecent, search, last, getText, getID;

		ClipConnection () throws SQLException {
			super(ClipDataStore.this);
			add = prepareStatement("INSERT INTO clips SET text=?, snip=?", true);
			removeText = prepareStatement("DELETE FROM clips WHERE text=?");
			removeID = prepareStatement("DELETE FROM clips WHERE id=?");
			last = prepareStatement("SELECT id, snip FROM clips ORDER BY id DESC LIMIT ? OFFSET ?");
			searchRecent = prepareStatement(
				"SELECT id, snip FROM (SELECT id, snip FROM clips ORDER BY id DESC LIMIT ?) WHERE snip LIKE ? LIMIT ?");
			search = prepareStatement("SELECT id, snip FROM clips WHERE snip LIKE ? ORDER BY id DESC LIMIT ?");
			getText = prepareStatement("SELECT text FROM clips WHERE id=? LIMIT 1");
			getID = prepareStatement("SELECT id FROM clips WHERE text=? LIMIT 1");
		}

		public int add (String text) throws SQLException {
			add.setString(1, text);
			add.setString(2, text.substring(0, Math.min(text.length(), maxSnipSize)));
			update(add);
			try (ResultSet set = add.getGeneratedKeys()) {
				return set.next() ? set.getInt(1) : 0;
			}
		}

		public void removeText (String text) throws SQLException {
			removeText.setString(1, text);
			update(removeText);
		}

		public void removeID (int id) throws SQLException {
			removeID.setInt(1, id);
			update(removeID);
		}

		public void searchRecent (ArrayList<Integer> ids, ArrayList<String> snips, String text, int first, int max)
			throws SQLException {
			ids.clear();
			snips.clear();
			searchRecent.setInt(1, first);
			searchRecent.setString(2, text);
			searchRecent.setInt(3, max);
			try (ResultSet set = query(searchRecent)) {
				while (set.next()) {
					ids.add(set.getInt(1));
					snips.add(set.getString(2));
				}
			}
		}

		public void search (ArrayList<Integer> ids, ArrayList<String> snips, String text, int max) throws SQLException {
			ids.clear();
			snips.clear();
			search.setString(1, text);
			search.setInt(2, max);
			try (ResultSet set = query(search)) {
				while (set.next()) {
					ids.add(set.getInt(1));
					snips.add(set.getString(2));
				}
			}
		}

		public void last (ArrayList<Integer> ids, ArrayList<String> snips, int max, int start) throws SQLException {
			ids.clear();
			snips.clear();
			last.setInt(1, max);
			last.setInt(2, start);
			try (ResultSet set = query(last)) {
				while (set.next()) {
					ids.add(set.getInt(1));
					snips.add(set.getString(2));
				}
			}
		}

		/** @return May be null. */
		public String getText (int id) throws SQLException {
			getText.setInt(1, id);
			return queryString(getText, null);
		}

		/** @return May be -1. */
		public int getID (String text) throws SQLException {
			getID.setString(1, text);
			return queryInt(getID, -1);
		}
	}
}
