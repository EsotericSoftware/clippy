# Clippy

Clippy is a clipboard history tool for Windows. It uses a database to store any text items that are copied to the clipboard. The stored text items can be easily retrieved by browsing or searching. Clippy can also take screenshots and upload them, as well as upload files, zips, and text/pastes. Uploading can be done via FTP, SFTP, [imgur.com](http://imgur.com/), and/or [pastebin.com](http://pastebin.com/).

![](http://i.imgur.com/4vEWhmX.png)

Clippy is a powerful tool, especially for programmers:

* It is very common to need to copy and paste a few different items. Since the operating system clipboard can only hold a single item, without Clippy this means finding and copying the same text multiple times.

* Clippy can be used simply to keep text safe, in case it is needed later. While writing an email or before refactoring a piece of code, you can select all the text and copy it, knowing that Clippy has stored the text should it ever be needed in the future.

* Previously copied text can be searched simply by typing any portion of the text. Any text copied can be easily found later, which can be useful for phone numbers, passwords, etc.

* Easily take a screenshot of the whole screen, the foreground application, or draw a rectangle with the mouse. Clippy will upload the image and paste the URL.

* Text pasted from Clippy's popup is always plain text. This means Clippy can be used to strip unwanted formatting from text.

* When files are copied, Clippy stores the file paths as text. This makes it easy to copy a file and paste its path to the command line, a file dialog, etc.

## Download

Download the latest version of Clippy here:

[Download Clippy](https://github.com/EsotericSoftware/clippy/releases)

There is no installation, instead Clippy is provided as two files. `Clippy.exe` is an executable that automatically finds Java and runs Clippy. Alternatively, `Clippy.jar` can be run directly either by double clicking or with `javaw -jar Clippy.jar`. You don't need both files, each file is the whole app.

It is highly recommended to run Clippy using 64-bit Java. Clippy may run much more slowly if 32-bit Java is used. To be sure of which Java installation Clippy uses, run it from the command line using the:

```
C:\path\to\64-bit\Java\javaw -jar C:\path\to\Clippy.jar
```

Clippy runs in the background as an icon in the system tray. It stores any text items that are placed on the clipboard and waits for hotkeys to show the clipboard history popup and take other actions. Clippy can be exited by clicking the icon in the system tray.

## Popup

The default hotkey to show the popup is `ctrl+shift+insert`. The popup shows the most recent clipboard items. Items can be selected by pressing `up` or `down` and are put on the clipboard and pasted by pressing `enter` or by clicking. Pressing 'home' or 'end' selects the first or last item. Pressing `page up` or `page down` shows additional pages of items. Pressing `ctrl+shift+delete` deletes the selected item. The popup is hidden by pasting an item, pressing `escape`, or clicking outside the popup. Pressing `ctrl+shift+enter` uploads the item as text and puts a link on the clipboard.

### Search

Items can be searched simply by typing while the popup is open. All items containing any of the typed text are shown. `%` can be used as a wildcard. Use `\%` to search for a percent sign.

### Menu

A menu is shown on the popup by pressing `alt`. The `Lock items` checkbox prevents the order of items from changing when an existing item is chosen. Numbers `0-9` can be pressed to choose an item.

## Screenshots

The default hotkey to take a region screenshot is `ctrl+alt+\`. Once activated, click and drag to specify the rectangular region to screenshot. The resulting image is uploaded to imgur and a link is placed on the clipboard when the upload is complete. The default hotkey to screenshot the foreground window is `ctrl+alt+shift+\`. The hotkey to screenshot the whole window is not mapped by default.

![](http://i.imgur.com/Ld05ys6.png)

While taking a region screenshot, hold `shift` to move more slowly and `ctrl` to lock to the X or Y axis.

## Configuration

Clippy stores its configuration file and the database in user folder, under the `.clippy` subfolder. The `config.json` file can be edited to configure Clippy. Eg, change the popup hotkey, the number of items shown on the popup, and more.

Hotkeys are described using [these constants](https://docs.oracle.com/javase/8/docs/api/java/awt/event/KeyEvent.html), just omit the "VK_" prefix.

### Uploading

Clippy has 3 settings for uploading:

* Text can be uploaded to FTP, SFTP, or [pastebin.com](http://pastebin.com/).
* Images can be uploaded to FTP, SFTP, or [imgur.com](http://imgur.com/).
* Files can be uploaded to FTP or SFTP.

The default settings use pastebin for text, imagur for images, and disallow file upload. FTP and SFTP must be configured in the `config.json` file.

## Development details

Clippy's codebase is clean and straightforward. It is written in Java and uses [JNA](https://github.com/twall/jna/) to access the Windows APIs necessary to monitor and interact with the [clipboard](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Clipboard.java), system-wide [hotkeys](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Keyboard.java), the [system tray](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/Tray.java), etc.

Text clips are stored in an [H2](http://www.h2database.com) relational database. A small [data store](https://github.com/EsotericSoftware/clippy/blob/master/src/com/esotericsoftware/clippy/ClipDataStore.java) API simplifies interaction with the database.

## License

Clippy is released as OSS under the [New BSD license](https://github.com/EsotericSoftware/clippy/blob/master/LICENSE).
