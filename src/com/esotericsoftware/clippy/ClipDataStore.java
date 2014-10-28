
package com.esotericsoftware.clippy;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.esotericsoftware.clippy.util.DataStore;

public class ClipDataStore extends DataStore<ClipDataStore.ClipConnection> {
	public ClipDataStore () throws SQLException {
		super("~/.clippy/db/db", "clips");
		if (System.getProperty("dev") != null)
			setInMemory(true);
		else
			setSocketLocking(true);
		addColumn("id INTEGER IDENTITY");
		addColumn("text VARCHAR_IGNORECASE");
		open();
		addIndex("text");
		createIndexes();
		getThreadConnection().execute("SET LOG 0"); // Disable transaction log.
	}

	public ClipConnection newConnection () throws SQLException {
		return new ClipConnection();
	}

	public final class ClipConnection extends DataStore.DataStoreConnection {
		private final PreparedStatement addClip, removeClip, contains, makeLast, search, last;

		ClipConnection () throws SQLException {
			addClip = prepareStatement("INSERT INTO :table: SET text=?");
			removeClip = prepareStatement("DELETE FROM :table: WHERE text=?");
			contains = prepareStatement("SELECT COUNT(*) FROM :table: WHERE text=? LIMIT 1");
			makeLast = prepareStatement("UPDATE :table: SET id=(SELECT MAX(id) + 1 FROM :table:) WHERE text=? LIMIT 1");
			last = prepareStatement("SELECT text FROM clips ORDER BY id DESC LIMIT ? OFFSET ?");
			search = prepareStatement("SELECT text FROM clips WHERE text LIKE ? ORDER BY id DESC LIMIT ?");
		}

		public void addClip (String text) throws SQLException {
			addClip.setString(1, text);
			addClip.executeUpdate();
		}

		public void removeClip (String text) throws SQLException {
			removeClip.setString(1, text);
			removeClip.executeUpdate();
		}

		public boolean contains (String text) throws SQLException {
			contains.setString(1, text);
			ResultSet set = contains.executeQuery();
			if (!set.next()) return false;
			return set.getInt(1) != 0;
		}

		public void makeLast (String text) throws SQLException {
			makeLast.setString(1, text);
			makeLast.executeUpdate();
		}

		public ArrayList<String> search (ArrayList<String> results, String text, int max) throws SQLException {
			search.setString(1, text);
			search.setInt(2, max);
			ResultSet set = search.executeQuery();
			results.clear();
			while (set.next())
				results.add(set.getString(1));
			return results;
		}

		public ArrayList<String> last (ArrayList<String> results, int max, int start) throws SQLException {
			last.setInt(1, max);
			last.setInt(2, start);
			ResultSet set = last.executeQuery();
			results.clear();
			while (set.next())
				results.add(set.getString(1));
			return results;
		}
	}
}
