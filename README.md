# Clippy

Clippy is a clipboard history tool for Windows. It uses a database to store any text items that are copied to the clipboard. The stored text items can be easily retrieved by browsing or searching.

![](http://i.imgur.com/4vEWhmX.png)

Clippy is a powerful tool, especially for programmers:

* It is very common to need to copy and paste a few different items. Since the operating system clipboard can only hold a single item, this is a tedious task without Clippy.

* Clippy can be used simply to keep text safe, just in case it is needed later. While writing an email or before refactoring a large piece of code, you can select all the text and copy it, knowing that Clippy has stored the text should it ever be needed in the future.

* Text pasted from Clippy's popup is always plain text. This means Clippy can be used to strip unwanted formatting from text.

* When files are copied, Clippy stores the paths to the files as text. This makes it easy to copy a file and paste its path to the command line, a file dialog, etc.

## Download

Download the latest version of Clippy here:

[Download Clippy](https://github.com/EsotericSoftware/clippy/releases)

There is no installation, only a `Clippy.exe` file. Place this file anywhere you like and run it. Clippy runs in the backgruond and stores any text items that are placed on the clipboard. These items can be retrieved by pressing the hotkey to show Clippy's popup (see below). Clippy can be exited by clicking the icon in the system tray.

## Popup

The default hotkey to show the popup is `ctrl+shift+insert`. The popup shows the most recent clipboard items. Items can be selected by pressing `up` or `down` and are put on the clipboard and pasted by pressing `enter` or by clicking. Pressing 'home' or 'end' selects the first or last item. Pressing `page up` or `page down` shows additional pages of items. Pressing `ctrl+shift+delete` deletes the selected item. The popup is hidden by pasting an item, pressing `escape`, or clicking outside the popup.

### Search

Items can be searched simply by typing while the popup is open. All items containing any of the typed text are shown.

### Menu

The popup menu is shown by pressing `alt`. The `Lock items` checkbox prevents the order of items from changing when an existing item is chosen.

## Configuration

Clippy stores its configuration file and the database in user folder, under the `.clippy` subfolder. The `config.json` file can be edited to configure Clippy. Eg, change the popup hotkey, the number of items shown on the popup, and more.

## Development details

Clippy's codebase is clean and straightforward. It is written in Java and uses [JNA](https://github.com/twall/jna/) to access the Windows APIs necessary to monitor and interact with the [clipboard](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Clipboard.java), system-wide [hotkeys](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Keyboard.java), the [system tray](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Tray.java), etc.

Text clips are stored in an [H2](http://www.h2database.com) relational database. A small [data store](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/ClipDataStore.java) API simplifies interaction with the database.

## License

Clippy is released as OSS under the [New BSD license](https://github.com/EsotericSoftware/clippy/blob/master/LICENSE).
