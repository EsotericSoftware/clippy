/* Copyright (c) 2014, Esoteric Software
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

import com.esotericsoftware.clippy.util.DataStore;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/** @author Nathan Sweet */
public class ClipDataStore extends DataStore<ClipDataStore.ClipConnection> {
	static public final int maxSnipSize = 2048;

	public ClipDataStore () throws SQLException {
		super("~/.clippy/db/db", "clips", //
			"LOG=0"); // Disable transaction log.
		// setTraceLevel(TraceLevel.DEBUG);
		if (System.getProperty("dev") != null)
			setInMemory(true);
		else
			setSocketLocking(true);
		addColumn("id INTEGER IDENTITY");
		addColumn("text VARCHAR_IGNORECASE NOT NULL");
		addColumn("snip VARCHAR_IGNORECASE(" + maxSnipSize + ") NOT NULL");
		open();
		addIndex("id DESC");
		addIndex("snip");
		addIndex("text");
		createIndexes();
	}

	public ClipConnection newConnection () throws SQLException {
		return new ClipConnection();
	}

	public final class ClipConnection extends DataStore.DataStoreConnection {
		private final PreparedStatement add, removeText, removeID, searchRecent, search, last, getText;

		ClipConnection () throws SQLException {
			add = prepareStatement("INSERT INTO :table: SET text=?, snip=?", true);
			removeText = prepareStatement("DELETE FROM :table: WHERE text=?");
			removeID = prepareStatement("DELETE FROM :table: WHERE id=?");
			last = prepareStatement("SELECT id, snip FROM :table: ORDER BY id DESC LIMIT ? OFFSET ?");
			searchRecent = prepareStatement(
				"SELECT id, snip FROM (SELECT id, snip FROM :table: ORDER BY id DESC LIMIT ?) WHERE snip LIKE ? LIMIT ?");
			search = prepareStatement("SELECT id, snip FROM :table: WHERE snip LIKE ? ORDER BY id DESC LIMIT ?");
			getText = prepareStatement("SELECT text FROM :table: WHERE id=? LIMIT 1");
		}

		public int add (String text) throws SQLException {
			add.setString(1, text);
			add.setString(2, text.substring(0, Math.min(text.length(), maxSnipSize)));
			add.executeUpdate();
			ResultSet set = add.getGeneratedKeys();
			return set.next() ? set.getInt(1) : 0;
		}

		public void removeText (String text) throws SQLException {
			removeText.setString(1, text);
			removeText.executeUpdate();
		}

		public void removeID (int id) throws SQLException {
			removeID.setInt(1, id);
			removeID.executeUpdate();
		}

		public void searchRecent (ArrayList<Integer> ids, ArrayList<String> snips, String text, int first, int max)
			throws SQLException {
			searchRecent.setInt(1, first);
			searchRecent.setString(2, text);
			searchRecent.setInt(3, max);
			ResultSet set = searchRecent.executeQuery();
			ids.clear();
			snips.clear();
			while (set.next()) {
				ids.add(set.getInt(1));
				snips.add(set.getString(2));
			}
		}

		public void search (ArrayList<Integer> ids, ArrayList<String> snips, String text, int max) throws SQLException {
			search.setString(1, text);
			search.setInt(2, max);
			ResultSet set = search.executeQuery();
			ids.clear();
			snips.clear();
			while (set.next()) {
				ids.add(set.getInt(1));
				snips.add(set.getString(2));
			}
		}

		public void last (ArrayList<Integer> ids, ArrayList<String> snips, int max, int start) throws SQLException {
			last.setInt(1, max);
			last.setInt(2, start);
			ResultSet set = last.executeQuery();
			ids.clear();
			snips.clear();
			while (set.next()) {
				ids.add(set.getInt(1));
				snips.add(set.getString(2));
			}
		}

		/** @return May be null. */
		public String getText (int id) throws SQLException {
			getText.setInt(1, id);
			ResultSet set = getText.executeQuery();
			if (set.next()) return set.getString(1);
			return null;
		}
	}
}
